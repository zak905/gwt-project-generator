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

import com.gwidgets.gwtprojectgenerator.test.generator.ProjectAssert
import com.gwidgets.gwtprojectgenerator.web.AbstractInitializrControllerIntegrationTests
import org.junit.Test


import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

import static org.junit.Assert.*

/**
 * @author Stephane Nicoll
 */
@ActiveProfiles(['test-default', 'test-custom-env'])
class MainControllerEnvIntegrationTests extends AbstractInitializrControllerIntegrationTests {



	@Test
	void doNotForceSsl() {
		ResponseEntity<String> response = invokeHome('curl/1.2.4', "*/*")
		String body = response.getBody()
		assertTrue "Must not force https", body.contains("http://localhost:$port/")
		assertFalse "Must not force https", body.contains('https://')
	}

	@Test
	void generateProjectWithInvalidName() {
		downloadZip('/starter.zip?style=data-jpa&name=Invalid')
				.isJavaProject(ProjectAssert.DEFAULT_PACKAGE_NAME, 'FooBarApplication')
				.isMavenProject()
				.hasStaticAndTemplatesResources(false).pomAssert()
				.hasDependenciesCount(2)
				.hasSpringBootStarterDependency('data-jpa')
				.hasSpringBootStarterTest()
	}

	@Test
	void googleAnalytics() {
		def body = htmlHome()
		assertTrue 'google analytics should be enabled', body.contains("ga('create', 'UA-1234567-89', 'auto');")
	}

}
