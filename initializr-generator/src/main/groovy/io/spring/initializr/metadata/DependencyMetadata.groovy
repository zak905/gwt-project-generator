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

package io.spring.initializr.metadata

import io.spring.initializr.util.Version

/**
 * Dependency metadata for a given spring boot {@link Version}.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
class DependencyMetadata {

	final Version gwtVersion

	final Map<String, Dependency> dependencies

	final Map<String, Repository> repositories

	final Map<String, BillOfMaterials> boms

	DependencyMetadata(Version gwtVersion, Map<String, Dependency> dependencies,
					   Map<String, Repository> repositories, Map<String, BillOfMaterials> boms) {
		this.gwtVersion = gwtVersion
		this.dependencies = dependencies
		this.repositories = repositories
		this.boms = boms
	}

}
