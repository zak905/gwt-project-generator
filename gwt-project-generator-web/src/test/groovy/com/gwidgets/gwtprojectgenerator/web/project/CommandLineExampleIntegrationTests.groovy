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

package com.gwidgets.gwtprojectgenerator.web.project

import com.gwidgets.gwtprojectgenerator.test.generator.PomAssert
import com.gwidgets.gwtprojectgenerator.web.AbstractInitializrControllerIntegrationTests
import org.junit.Test

import org.springframework.test.context.ActiveProfiles

/**
 * Validate that the "raw" HTTP commands that are described in the
 * command-line help works. If anything needs to be updated here, please
 * double check the "curl-examples.txt" as it may need an update
 * as well. This is also a good indicator of a non backward compatible
 * change.
 *
 * @author Stephane Nicoll
 */
@ActiveProfiles('test-default')
class CommandLineExampleIntegrationTests extends AbstractInitializrControllerIntegrationTests {

	@Test
	void generateDefaultProject() {
		downloadZip('/starter.zip').isJavaProject()
				.isMavenProject().hasStaticAndTemplatesResources(false).pomAssert()
				.hasSpringBootStarterRootDependency()
				.hasSpringBootStarterTest()
				.hasDependenciesCount(2)
	}

	@Test
	void generateWebProjectWithJava8() {
		downloadZip('/starter.zip?dependencies=web&javaVersion=1.8').isJavaProject()
				.isMavenProject().hasStaticAndTemplatesResources(true).pomAssert()
				.hasJavaVersion('1.8')
				.hasSpringBootStarterDependency('web')
				.hasSpringBootStarterTest()
				.hasDependenciesCount(2)
	}

	@Test
	void generateWebDataJpaGradleProject() {
		downloadTgz('/starter.tgz?dependencies=web,data-jpa&type=gradle-project&baseDir=my-dir')
				.hasBaseDir('my-dir')
				.isJavaProject()
				.isGradleProject().hasStaticAndTemplatesResources(true)
				.gradleBuildAssert()
				.contains('spring-boot-starter-web')
				.contains('spring-boot-starter-data-jpa')
	}

	@Test
	void generateMavenPomWithWarPackaging() {
		def response = restTemplate.getForEntity(createUrl('/pom.xml?packaging=war'), String)
		PomAssert pomAssert = new PomAssert(response.body)
		pomAssert.hasPackaging('war')
	}

}
