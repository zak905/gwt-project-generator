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

package io.spring.initializr.test.metadata

import io.spring.initializr.metadata.DefaultMetadataElement
import io.spring.initializr.metadata.Dependency
import io.spring.initializr.metadata.DependencyGroup
import io.spring.initializr.metadata.InitializrMetadata
import io.spring.initializr.metadata.InitializrMetadataBuilder
import io.spring.initializr.metadata.Type

/**
 * Easily create a {@link InitializrMetadata} instance for testing purposes.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
class InitializrMetadataTestBuilder {

	private final InitializrMetadataBuilder builder = InitializrMetadataBuilder.create()

	static InitializrMetadataTestBuilder withDefaults() {
		new InitializrMetadataTestBuilder().addDefaults()
	}

	InitializrMetadata build() {
		builder.build()
	}

	InitializrMetadataTestBuilder addDependencyGroup(String name, String... ids) {
		builder.withCustomizer {
			def group = new DependencyGroup(name: name)
			for (String id : ids) {
				def dependency = new Dependency(id: id)
				group.content << dependency
			}
			it.dependencies.content << group
		}
		this
	}

	InitializrMetadataTestBuilder addDependencyGroup(String name, Dependency... dependencies) {
		builder.withCustomizer {
			def group = new DependencyGroup(name: name)
			group.content.addAll(dependencies)
			it.dependencies.content << group
		}
		this
	}

	InitializrMetadataTestBuilder addDefaults() {
		addDefaultTypes().addDefaultPackagings().addDefaultJavaVersions()
				.addDefaultBootVersions()
				.setGradleEnv('0.5.1.RELEASE').setKotlinEnv('1.0.1')
	}

	InitializrMetadataTestBuilder addDefaultTypes() {
		addType('maven-build', false, '/pom.xml', 'maven', 'build')
				.addType('maven-project', true, '/starter.zip', 'maven', 'project')
				.addType('gradle-build', false, '/build.gradle', 'gradle', 'build')
				.addType('gradle-project', false, '/starter.zip', 'gradle', 'project')
	}

	InitializrMetadataTestBuilder addType(String id, boolean defaultValue, String action, String build, String format) {
		def type = new Type(id: id, name: id, default: defaultValue, action: action)
		if (build) {
			type.tags['build'] = build
		}
		if (format) {
			type.tags['format'] = format
		}
		addType(type)
	}

	InitializrMetadataTestBuilder addType(Type type) {
		builder.withCustomizer { it.types.content << type }
		this
	}

	InitializrMetadataTestBuilder addDefaultPackagings() {
		addPackaging('jar', true).addPackaging('war', false)
	}

	InitializrMetadataTestBuilder addPackaging(String id, boolean defaultValue) {
		builder.withCustomizer {
			def packaging = new DefaultMetadataElement(id: id, name: id, default: defaultValue)
			it.packagings.content << packaging
		}
		this
	}

	InitializrMetadataTestBuilder addDefaultJavaVersions() {
		addJavaVersion('1.6', false).addJavaVersion('1.7', false).addJavaVersion('1.8', true)
	}

	InitializrMetadataTestBuilder addJavaVersion(String version, boolean defaultValue) {
		builder.withCustomizer {
			def javaVersion = new DefaultMetadataElement(id: version, name: version, default: defaultValue)
			it.javaVersions.content << javaVersion
		}
		this
	}



	InitializrMetadataTestBuilder addDefaultBootVersions() {
		addBootVersion('1.1.2.RELEASE', false).addBootVersion('1.2.3.RELEASE', true)
				.addBootVersion('1.3.0.BUILD-SNAPSHOT', false)
	}

	InitializrMetadataTestBuilder addBootVersion(String id, boolean defaultValue) {
		builder.withCustomizer {
			def bootVersion = new DefaultMetadataElement(id: id, name: id, default: defaultValue)
			it.bootVersions.content << bootVersion
		}
		this
	}




	InitializrMetadataTestBuilder setGradleEnv(String dependencyManagementPluginVersion) {
		builder.withCustomizer {
			it.configuration.env.gradle.dependencyManagementPluginVersion = dependencyManagementPluginVersion
		}
		this
	}

	InitializrMetadataTestBuilder setKotlinEnv(String kotlinVersion) {
		builder.withCustomizer {
			it.configuration.env.kotlin.version = kotlinVersion
		}
		this
	}

	InitializrMetadataTestBuilder setMavenParent(String groupId, String artifactId,
												 String version, boolean includeSpringBootBom) {
		builder.withCustomizer {
			def parent = it.configuration.env.maven.parent
			parent.groupId = groupId
			parent.artifactId = artifactId
			parent.version = version
			parent.includeSpringBootBom = includeSpringBootBom
		}
		this
	}



}