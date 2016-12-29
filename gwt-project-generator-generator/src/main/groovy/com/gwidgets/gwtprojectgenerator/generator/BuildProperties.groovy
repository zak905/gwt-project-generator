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

/**
 * Build properties associated to a project request.
 *
 * @author Stephane Nicoll
 */
class BuildProperties {

	/**
	 * Maven-specific build properties, added to the regular {@code properties} element.
	 */
	final TreeMap<String, Closure<String>> maven = new TreeMap<>()

	/**
	 * Gradle-specific build properties, added to the {@code buildscript} section
	 * of the gradle build.
	 */
	final TreeMap<String, Closure<String>> gradle = new TreeMap<>()

	/**
	 * Version properties. Shared between the two build systems.
	 */
	final TreeMap<String, Closure<String>> versions = new TreeMap<>()

}
