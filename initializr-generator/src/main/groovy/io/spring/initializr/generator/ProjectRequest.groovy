/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.spring.initializr.metadata.Dependency
import io.spring.initializr.metadata.InitializrMetadata
import io.spring.initializr.metadata.Type
import io.spring.initializr.util.Version
import io.spring.initializr.util.VersionRange

/**
 * A request to generate a project.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Zakaria Amine
 * @since 1.0
 */
@Slf4j
@ToString(ignoreNulls = true, includePackage = false, includeNames = true)
class ProjectRequest extends BasicProjectRequest {

	/**
	 * The id of the starter to use if no dependency is defined.
	 */
	static final DEFAULT_STARTER = 'root_starter'

	/**
	 * Additional parameters that can be used to further identify the request.
	 */
	final Map<String, Object> parameters = [:]

	// Resolved dependencies based on the ids provided by either "style" or "dependencies"
	List<Dependency> resolvedDependencies

	/**
	 * Build properties.
	 */
	final BuildProperties buildProperties = new BuildProperties()

	def facets = []
	def build

	/**
	 * Initializes this instance with the defaults defined in the specified {@link InitializrMetadata}.
	 */
	void initialize(InitializrMetadata metadata) {
		metadata.defaults().each { key, value ->
			if (owner.hasProperty(key)) {
				owner.setProperty(key, value)
			}
		}
	}

	/**
	 * Resolve this instance against the specified {@link InitializrMetadata}
	 */
	void resolve(InitializrMetadata metadata) {
		List<String> depIds = style ? style : dependencies
		String actualGwtVersion = gwtVersion ?: metadata.gwtVersions.default.id
		Version requestedVersion = Version.parse(actualGwtVersion)
		resolvedDependencies = depIds.collect {
			def dependency = metadata.dependencies.get(it)
			if (dependency == null) {
				throw new InvalidProjectRequestException("Unknown dependency '$it' check project metadata")
			}
			dependency.resolve(requestedVersion)
		}
		resolvedDependencies.each {
			it.facets.each {
				if (!facets.contains(it)) {
					facets.add(it)
				}
			}
			if (it.versionRange) {
				def range = VersionRange.parse(it.versionRange)
				if (!range.match(requestedVersion)) {
					throw new InvalidProjectRequestException("Dependency '$it.id' is not compatible " +
							"with GWT $gwtVersion")
				}
			}

		}

		if (this.type) {
			Type type = metadata.types.get(this.type)
			if (!type) {
				throw new InvalidProjectRequestException("Unknown type '${this.type}' check project metadata")
			}
			String buildTag = type.tags['build']
			if (buildTag) {
				this.build = buildTag
			}
		}
		if (this.packaging) {
			def packaging = metadata.packagings.get(this.packaging)
			if (!packaging) {
				throw new InvalidProjectRequestException("Unknown packaging '${this.packaging}' check project metadata")
			}
		}


		if (!applicationName) {
			this.applicationName = metadata.configuration.generateApplicationName(this.name)
		}
		packageName = metadata.configuration.cleanPackageName(this.packageName, metadata.packageName.content)


		initializeProperties(metadata)

		afterResolution(metadata)
	}


	protected void initializeProperties(InitializrMetadata metadata) {
		if ('gradle'.equals(build)) {
			buildProperties.gradle['gwtVersion'] = { getGwtVersion() }
		} else {
			buildProperties.maven['project.build.sourceEncoding'] = { 'UTF-8' }
			buildProperties.maven['project.reporting.outputEncoding'] = { 'UTF-8' }
			buildProperties.versions['java.version'] = { getJavaVersion() }
		}
	}


	/**
	 * Update this request once it has been resolved with the specified {@link InitializrMetadata}.
	 */
	protected afterResolution(InitializrMetadata metadata) {
		if (packaging == 'war') {
			if (!hasWebFacet()) {
				// Need to be able to bootstrap the web app
				resolvedDependencies << metadata.dependencies.get('web')
				facets << 'web'
			}
			// Add the tomcat starter in provided scope
			def tomcat = new Dependency().asSpringBootStarter('tomcat')
			tomcat.scope = Dependency.SCOPE_PROVIDED
			resolvedDependencies << tomcat
		}
	}


	/**
	 * Specify if this request has the web facet enabled.
	 */
	boolean hasWebFacet() {
		hasFacet('web')
	}

	/**
	 * Specify if this request has the specified facet enabled
	 */
	boolean hasFacet(String facet) {
		facets.contains(facet)
	}

}
