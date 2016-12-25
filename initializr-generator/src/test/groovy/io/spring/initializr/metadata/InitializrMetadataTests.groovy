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

package io.spring.initializr.metadata

import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * @author Stephane Nicoll
 */
class InitializrMetadataTests {

	@Rule
	public final ExpectedException thrown = ExpectedException.none()





	@Test
	void invalidParentMissingVersion() {
		InitializrMetadataTestBuilder builder = InitializrMetadataTestBuilder
				.withDefaults()
				.setMavenParent('org.foo', 'foo-parent', null, false)

		thrown.expect(InvalidInitializrMetadataException)
		thrown.expectMessage("Custom maven pom requires groupId, artifactId and version")
		builder.build()
	}

}
