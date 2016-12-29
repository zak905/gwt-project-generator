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

package com.gwidgets.gwtprojectgenerator.generator

import com.gwidgets.gwtprojectgenerator.metadata.Dependency
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadata
import com.gwidgets.gwtprojectgenerator.metadata.SimpleInitializrMetadataProvider
import com.gwidgets.gwtprojectgenerator.test.generator.GradleBuildAssert
import com.gwidgets.gwtprojectgenerator.test.generator.PomAssert
import com.gwidgets.gwtprojectgenerator.test.generator.ProjectAssert
import com.gwidgets.gwtprojectgenerator.test.metadata.InitializrMetadataTestBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatcher

import org.springframework.context.ApplicationEventPublisher

import static org.mockito.Matchers.argThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

/**
 *
 * @author Stephane Nicoll
 */
abstract class AbstractProjectGeneratorTests {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder()

	protected final ProjectGenerator projectGenerator = new ProjectGenerator()

	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher)

	@Before
	void setup() {
		def web = new Dependency(id: 'web')
		web.facets << 'web'
		def metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup('web', web)
				.addDependencyGroup('test', 'security', 'data-jpa', 'aop', 'batch', 'integration').build()
		applyMetadata(metadata)
		projectGenerator.eventPublisher = eventPublisher
		projectGenerator.requestResolver = new ProjectRequestResolver([])
		projectGenerator.tmpdir = folder.newFolder().absolutePath
	}


	protected PomAssert generateMavenPom(ProjectRequest request) {
		request.type = 'maven-build'
		def content = new String(projectGenerator.generateMavenPom(request))
		new PomAssert(content).validateProjectRequest(request)
	}

	protected GradleBuildAssert generateGradleBuild(ProjectRequest request) {
		request.type = 'gradle-build'
		def content = new String(projectGenerator.generateGradleBuild(request))
		new GradleBuildAssert(content).validateProjectRequest(request)
	}

	protected ProjectAssert generateProject(ProjectRequest request) {
		def dir = projectGenerator.generateProjectStructure(request)
		new ProjectAssert(dir)
	}

	ProjectRequest createProjectRequest(String... styles) {
		def request = new ProjectRequest()
		request.initialize(projectGenerator.metadataProvider.get())
		request.style.addAll Arrays.asList(styles)
		request
	}

	protected void applyMetadata(InitializrMetadata metadata) {
		projectGenerator.metadataProvider = new SimpleInitializrMetadataProvider(metadata)
	}

	protected verifyProjectSuccessfulEventFor(ProjectRequest request) {
		verify(eventPublisher, times(1)).publishEvent(argThat(new ProjectGeneratedEventMatcher(request)))
	}

	protected verifyProjectFailedEventFor(ProjectRequest request, Exception ex) {
		verify(eventPublisher, times(1)).publishEvent(argThat(new ProjectFailedEventMatcher(request, ex)))
	}

	private static class ProjectGeneratedEventMatcher extends ArgumentMatcher<ProjectGeneratedEvent> {

		private final ProjectRequest request

		ProjectGeneratedEventMatcher(ProjectRequest request) {
			this.request = request
		}

		@Override
		boolean matches(Object argument) {
			ProjectGeneratedEvent event = (ProjectGeneratedEvent) argument
			return request.equals(event.projectRequest)
		}
	}

	private static class ProjectFailedEventMatcher extends ArgumentMatcher<ProjectFailedEvent> {

		private final ProjectRequest request
		private final Exception cause

		ProjectFailedEventMatcher(ProjectRequest request, Exception cause) {
			this.request = request
			this.cause = cause
		}

		@Override
		boolean matches(Object argument) {
			ProjectFailedEvent event = (ProjectFailedEvent) argument
			return request.equals(event.projectRequest) && cause.equals(event.cause)
		}
	}


}
