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

package com.gwidgets.gwtprojectgenerator.web.support

import com.gwidgets.gwtprojectgenerator.metadata.Dependency
import com.gwidgets.gwtprojectgenerator.metadata.DependencyMetadataProvider
import com.gwidgets.gwtprojectgenerator.test.metadata.InitializrMetadataTestBuilder
import com.gwidgets.gwtprojectgenerator.util.Version
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertSame;

/**
 * @author Stephane Nicoll
 */
class DefaultDependencyMetadataProviderTests {

	private final DependencyMetadataProvider provider = new DefaultDependencyMetadataProvider()

	@Test
	void filterDependencies() {
		def first = new Dependency(id: 'first', groupId: 'org.foo', artifactId: 'first',
				versionRange: '1.1.4.RELEASE')
		def second = new Dependency(id: 'second', groupId: 'org.foo', artifactId: 'second')
		def third = new Dependency(id: 'third', groupId: 'org.foo', artifactId: 'third',
				versionRange: '1.1.8.RELEASE')
		def metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup('test', first, second, third).build()
		def dependencyMetadata = provider.get(metadata, Version.parse('1.1.5.RELEASE'))
		assertEquals 2, dependencyMetadata.dependencies.size()
		assertSame first, dependencyMetadata.dependencies['first']
		assertSame second, dependencyMetadata.dependencies['second']
	}

	@Test
	void resolveDependencies() {
		def first = new Dependency(id: 'first', groupId: 'org.foo', artifactId: 'first')
		first.mappings << new Dependency.Mapping(versionRange: '[1.0.0.RELEASE, 1.1.0.RELEASE)',
				version: '0.1.0.RELEASE', groupId: 'org.bar', artifactId: 'second')
		first.mappings << new Dependency.Mapping(versionRange: '1.1.0.RELEASE',
				version: '0.2.0.RELEASE', groupId: 'org.biz', artifactId: 'third')
		def second = new Dependency(id: 'second', groupId: 'org.foo', artifactId: 'second')
		def metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup('test', first, second).build()

		def dependencyMetadata = provider.get(metadata, Version.parse('1.0.5.RELEASE'))
		assertEquals 2, dependencyMetadata.dependencies.size()
		assertEquals('org.bar', dependencyMetadata.dependencies['first'].groupId)
		assertEquals('second', dependencyMetadata.dependencies['first'].artifactId)
		assertEquals('0.1.0.RELEASE', dependencyMetadata.dependencies['first'].version)

		def anotherDependencyMetadata = provider.get(metadata, Version.parse('1.1.0.RELEASE'))
		assertEquals 2, anotherDependencyMetadata.dependencies.size()
		assertEquals('org.biz', anotherDependencyMetadata.dependencies['first'].groupId)
		assertEquals('third', anotherDependencyMetadata.dependencies['first'].artifactId)
		assertEquals('0.2.0.RELEASE', anotherDependencyMetadata.dependencies['first'].version)
	}








}
