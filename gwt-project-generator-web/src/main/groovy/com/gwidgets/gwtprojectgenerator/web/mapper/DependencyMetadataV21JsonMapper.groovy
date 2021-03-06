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

import groovy.json.JsonBuilder
import com.gwidgets.gwtprojectgenerator.metadata.Dependency
import com.gwidgets.gwtprojectgenerator.metadata.DependencyMetadata


/**
 * A {@link DependencyMetadataJsonMapper} handling the metadata format for v2.1.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
class DependencyMetadataV21JsonMapper implements DependencyMetadataJsonMapper {

	@Override
	String write(DependencyMetadata metadata) {
		JsonBuilder json = new JsonBuilder()
		json {
			gwtVersion metadata.gwtVersion.toString()
			dependencies metadata.dependencies.collectEntries { id, d ->
				[id, mapDependency(d)]
			}
		}
		json.toString()
	}

	private static mapDependency(Dependency dep) {
		def result = [:]
		result.groupId = dep.groupId
		result.artifactId = dep.artifactId
		if (dep.version) {
			result.version = dep.version
		}
		result.scope = dep.scope

		result
	}


}
