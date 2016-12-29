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

package com.gwidgets.gwtprojectgenerator.web.support

import com.gwidgets.gwtprojectgenerator.metadata.Dependency
import com.gwidgets.gwtprojectgenerator.metadata.DependencyMetadata
import com.gwidgets.gwtprojectgenerator.metadata.DependencyMetadataProvider
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadata
import com.gwidgets.gwtprojectgenerator.util.Version

import org.springframework.cache.annotation.Cacheable

/**
 * A default {@link DependencyMetadataProvider} implementation.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
class DefaultDependencyMetadataProvider implements DependencyMetadataProvider {

	@Override
	@Cacheable(cacheNames = "dependency-metadata", key = "#p1")
	DependencyMetadata get(InitializrMetadata metadata, Version gwtVersion) {
		Map<String, Dependency> dependencies = [:]
		for (Dependency d : metadata.dependencies.getAll()) {
			if (d.match(gwtVersion)) {
				dependencies[d.id] = d.resolve(gwtVersion)
			}
		}




		return new DependencyMetadata(gwtVersion, dependencies)
	}

}
