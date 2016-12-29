/*
 * Copyright 2012-2015 the original author or authors.
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

package com.gwidgets.gwtprojectgenerator.web.mapper

import groovy.json.JsonSlurper
import com.gwidgets.gwtprojectgenerator.metadata.Dependency
import com.gwidgets.gwtprojectgenerator.metadata.DependencyMetadata
import com.gwidgets.gwtprojectgenerator.util.Version
import org.junit.Test

/**
 * @author Stephane Nicoll
 */
class DependencyMetadataJsonMapperTests {

	private final DependencyMetadataJsonMapper mapper = new DependencyMetadataV21JsonMapper()
	private final JsonSlurper slurper = new JsonSlurper()

	@Test
	void mapDependency() {
		Dependency d = new Dependency(id: 'foo', groupId: 'org.foo', artifactId: 'foo')
		DependencyMetadata metadata = new DependencyMetadata(Version.parse('1.2.0.RELEASE'))
		def content = slurper.parseText(mapper.write(metadata))
		println  content
	}

}
