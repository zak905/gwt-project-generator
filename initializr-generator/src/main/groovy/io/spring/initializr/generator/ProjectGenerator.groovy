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

import groovy.transform.Synchronized
import groovy.util.logging.Slf4j
import io.spring.initializr.InitializrException
import io.spring.initializr.metadata.Dependency
import io.spring.initializr.metadata.InitializrMetadataProvider
import io.spring.initializr.util.GroovyTemplate
import io.spring.initializr.util.Version

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.util.Assert

import static io.spring.initializr.metadata.InitializrConfiguration.Env.Maven.ParentPom

/**
 * Generate a project based on the configured metadata.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @since 1.0
 */
@Slf4j
class ProjectGenerator {

	@Autowired
	ApplicationEventPublisher eventPublisher

	@Autowired
	InitializrMetadataProvider metadataProvider

	@Autowired
	ProjectRequestResolver requestResolver

	@Autowired
	GroovyTemplate groovyTemplate = new GroovyTemplate()

	@Autowired
	ProjectResourceLocator projectResourceLocator = new ProjectResourceLocator()

	@Value('${TMPDIR:.}')
	String tmpdir

	private transient Map<String, List<File>> temporaryFiles = [:]

	/**
	 * Generate a Maven pom for the specified {@link ProjectRequest}.
	 */
	byte[] generateMavenPom(ProjectRequest request) {
		try {
			def model = resolveModel(request)
			if (!isMavenBuild(request)) {
				throw new InvalidProjectRequestException("Could not generate Maven pom, " +
						"invalid project type $request.type")
			}
			def content = doGenerateMavenPom(model)
			publishProjectGeneratedEvent(request)
			content
		} catch (InitializrException ex) {
			publishProjectFailedEvent(request, ex)
			throw ex
		}
	}

	/**
	 * Generate a Gradle build file for the specified {@link ProjectRequest}.
	 */
	byte[] generateGradleBuild(ProjectRequest request) {
		try {
			def model = resolveModel(request)
			if (!isGradleBuild(request)) {
				throw new InvalidProjectRequestException("Could not generate Gradle build, " +
						"invalid project type $request.type")
			}
			def content = doGenerateGradleBuild(model)
			publishProjectGeneratedEvent(request)
			content
		} catch (InitializrException ex) {
			publishProjectFailedEvent(request, ex)
			throw ex
		}
	}

	/**
	 * Generate a project structure for the specified {@link ProjectRequest}. Returns
	 * a directory containing the project.
	 */
	File generateProjectStructure(ProjectRequest request) {
		try {
			doGenerateProjectStructure(request)
		} catch (InitializrException ex) {
			publishProjectFailedEvent(request, ex)
			throw ex
		}
	}

	protected File doGenerateProjectStructure(ProjectRequest request) {
		def model = resolveModel(request)

		def rootDir = File.createTempFile('tmp', '', new File(tmpdir))
		addTempFile(rootDir.name, rootDir)
		rootDir.delete()
		rootDir.mkdirs()

		def dir = initializerProjectDir(rootDir, request)

		if (isGradleBuild(request)) {
			def gradle = new String(doGenerateGradleBuild(model))
			new File(dir, 'build.gradle').write(gradle)
		} else {
			log.info("pom model " + model.toMapString())
			def pom = new String(doGenerateMavenPom(model))
			new File(dir, 'pom.xml').write(pom)
		}

		generateGitIgnore(dir, request)

        def src = new File(dir, "src");
		src.mkdir();

		def main = new File(src, "main");
		main.mkdir()

		def java = new File(new File(main, "java"), request.packageName.replace('.', '/'))
		java.mkdirs()
		write(new File(java, "App.java"), "EntryPoint.java", model)

		def test = new File(new File(src, "test/java"), request.packageName.replace('.', '/'))
		test.mkdirs()
		setupTestModel(request, model)
		write(new File(test, "AppTests.java"), "GwtTesttemplate.java", model)

		def resources = new File(main, 'resources')
		resources.mkdir()


		     write(new File(main, "module.gwt.xml"), "module.gwt.xml", model)


		publishProjectGeneratedEvent(request)
		rootDir

	}

	/**
	 * Create a distribution file for the specified project structure
	 * directory and extension
	 */
	File createDistributionFile(File dir, String extension) {
		def download = new File(tmpdir, dir.name + extension)
		addTempFile(dir.name, download)
		download
	}

	/**
	 * Clean all the temporary files that are related to this root
	 * directory.
	 * @see #createDistributionFile
	 */
	void cleanTempFiles(File dir) {
		def tempFiles = temporaryFiles.remove(dir.name)
		if (tempFiles) {
			tempFiles.each { File file ->
				if (file.directory) {
					file.deleteDir()
				} else {
					file.delete()
				}
			}
		}
	}

	private void publishProjectGeneratedEvent(ProjectRequest request) {
		ProjectGeneratedEvent event = new ProjectGeneratedEvent(request)
		eventPublisher.publishEvent(event)
	}

	private void publishProjectFailedEvent(ProjectRequest request, Exception cause) {
		ProjectFailedEvent event = new ProjectFailedEvent(request, cause)
		eventPublisher.publishEvent(event)
	}

	/**
	 * Generate a {@code .gitignore} file for the specified {@link ProjectRequest}
	 * @param dir the root directory of the project
	 * @param request the request to handle
	 */
	protected void generateGitIgnore(File dir, ProjectRequest request) {
		def model = [:]
		model['build'] = isGradleBuild(request) ? 'gradle' : 'maven'
		write(new File(dir, '.gitignore'), 'gitignore.tmpl', model)
	}

	/**
	 * Resolve the specified {@link ProjectRequest} and return the model to use
	 * to generate the project
	 * @param request the request to handle
	 * @return a model for that request
	 */
	protected Map resolveModel(ProjectRequest originalRequest) {
		Assert.notNull originalRequest.gwtVersion, 'gwt version must not be null'
		def model = [:]
		def metadata = metadataProvider.get()

		ProjectRequest request = requestResolver.resolve(originalRequest, metadata)

		// request resolved so we can log what has been requested
		def dependencies = request.resolvedDependencies
		def dependencyIds = dependencies.collect { it.id }
		log.info("Processing request{type=$request.type, dependencies=$dependencyIds}")


		model['compileDependencies'] = filterDependencies(dependencies, Dependency.SCOPE_COMPILE)
		model['runtimeDependencies'] = filterDependencies(dependencies, Dependency.SCOPE_RUNTIME)
		model['compileOnlyDependencies'] = filterDependencies(dependencies, Dependency.SCOPE_COMPILE_ONLY)
		model['providedDependencies'] = filterDependencies(dependencies, Dependency.SCOPE_PROVIDED)
		model['testDependencies'] = filterDependencies(dependencies, Dependency.SCOPE_TEST)

		request.boms.each { k, v ->
			if (v.versionProperty) {
				request.buildProperties.versions[v.versionProperty] = { v.version }
			}
		}

		// Append the project request to the model
		request.properties.each { model[it.key] = it.value }

		model
	}

	protected void setupTestModel(ProjectRequest request, Map<String, Object> model) {
		String imports = ''
		String testAnnotations = ''

		model.testImports = imports
		model.testAnnotations = testAnnotations
	}

	protected String generateImport(String type, String language) {
		String end = (language.equals("groovy") || language.equals("kotlin")) ? '' : ';'
		"import $type$end"
	}

	private static isGradleBuild(ProjectRequest request) {
		return 'gradle'.equals(request.build)
	}

	private static isMavenBuild(ProjectRequest request) {
		return 'maven'.equals(request.build)
	}



	private byte[] doGenerateMavenPom(Map model) {

		groovyTemplate.process 'starter-pom.xml', model
	}


	private byte[] doGenerateGradleBuild(Map model) {
		groovyTemplate.process 'starter-build.gradle', model
	}




	private File initializerProjectDir(File rootDir, ProjectRequest request) {
		if (request.baseDir) {
			File dir = new File(rootDir, request.baseDir)
			dir.mkdirs()
			return dir
		} else {
			return rootDir
		}
	}

	def write(File target, String templateName, def model) {
		def tmpl = templateName.endsWith('.groovy') ? templateName + '.tmpl' : templateName
		def body = groovyTemplate.process tmpl, model
		target.write(body, 'utf-8')


	}

	private void addTempFile(String group, File file) {
		def content = temporaryFiles[group]
		if (!content) {
			content = []
			temporaryFiles[group] = content
		}
		content << file
	}

	private static def filterDependencies(def dependencies, String scope) {
		dependencies.findAll { dep -> scope.equals(dep.scope) }.sort { a, b -> a.id <=> b.id }
	}

}
