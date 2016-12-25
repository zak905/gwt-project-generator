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

package io.spring.initializr.test.generator

import io.spring.initializr.generator.ProjectRequest
import io.spring.initializr.metadata.Dependency
import org.custommonkey.xmlunit.SimpleNamespaceContext
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.XpathEngine
import org.junit.Assert
import org.w3c.dom.Document
import org.w3c.dom.Element

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * XPath assertions that are specific to a standard Maven POM.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
class PomAssert {

	final XpathEngine eng
	final Document doc
	final Map<String, String> properties = [:]
	final Map<String, Dependency> dependencies = [:]

	PomAssert(String content) {
		eng = XMLUnit.newXpathEngine()
		def context = [:]
		context['pom'] = 'http://maven.apache.org/POM/4.0.0'
		def namespaceContext = new SimpleNamespaceContext(context)
		eng.namespaceContext = namespaceContext
		doc = XMLUnit.buildControlDocument(content)
		parseProperties()
		parseDependencies()
	}

	/**
	 * Validate that this generated pom validates against its request.
	 */
	PomAssert validateProjectRequest(ProjectRequest request) {
		hasGroupId(request.groupId).hasArtifactId(request.artifactId).hasVersion(request.version).
				hasPackaging(request.packaging).hasName(request.name).hasDescription(request.description).
				hasJavaVersion(request.javaVersion)
	}

	PomAssert hasGroupId(String groupId) {
		assertEquals groupId, eng.evaluate(createRootNodeXPath('groupId'), doc)
		this
	}

	PomAssert hasArtifactId(String artifactId) {
		assertEquals artifactId, eng.evaluate(createRootNodeXPath('artifactId'), doc)
		this
	}

	PomAssert hasVersion(String version) {
		assertEquals version, eng.evaluate(createRootNodeXPath('version'), doc)
		this
	}

	PomAssert hasPackaging(String packaging) {
		assertEquals packaging, eng.evaluate(createRootNodeXPath('packaging'), doc)
		this
	}

	PomAssert hasName(String name) {
		assertEquals name, eng.evaluate(createRootNodeXPath('name'), doc)
		this
	}

	PomAssert hasDescription(String description) {
		assertEquals description, eng.evaluate(createRootNodeXPath('description'), doc)
		this
	}

	PomAssert hasJavaVersion(String javaVersion) {
		assertEquals javaVersion, eng.evaluate(createPropertyNodeXpath('java.version'), doc)
		this
	}

	PomAssert hasProperty(String name, String value) {
		assertTrue "No property $name found", properties.containsKey(name)
		assertEquals "Wrong value for property $name", value, properties[name]
		this
	}

	PomAssert hasNoProperty(String name) {
		assertFalse "No property $name should have been found", properties.containsKey(name)
		this
	}

	PomAssert hasDependenciesCount(int count) {
		assertEquals "Wrong number of declared dependencies -->'${dependencies.keySet()}",
				count, dependencies.size()
		this
	}

	PomAssert hasSpringBootStarterTomcat() {
		hasDependency(new Dependency(id: 'tomcat', scope: 'provided').asSpringBootStarter('tomcat'))
	}

	PomAssert hasSpringBootStarterTest() {
		hasDependency(new Dependency(id: 'test', scope: 'test').asSpringBootStarter('test'))
	}

	PomAssert hasSpringBootStarterDependency(String dependency) {
		hasDependency('org.springframework.boot', "spring-boot-starter-$dependency")
	}

	PomAssert hasSpringBootStarterRootDependency() {
		hasDependency('org.springframework.boot', 'spring-boot-starter')
	}

	PomAssert hasDependency(String groupId, String artifactId) {
		hasDependency(groupId, artifactId, null)
	}

	PomAssert hasDependency(String groupId, String artifactId, String version) {
		hasDependency(new Dependency(groupId: groupId, artifactId: artifactId, version: version))
	}


	PomAssert hasSpringBootParent(String version) {
		hasParent('org.springframework.boot', 'spring-boot-starter-parent', version)
	}

	PomAssert hasDependency(Dependency expected) {
		def id = generateDependencyId(expected.groupId, expected.artifactId)
		def dependency = dependencies[id]
		assertNotNull "No dependency found with '$id' --> ${dependencies.keySet()}", dependency
		if (expected.version) {
			assertEquals "Wrong version for $dependency", expected.version, dependency.version
		}
		if (expected.scope) {
			assertEquals "Wrong scope for $dependency", expected.scope, dependency.scope
		}
		if (expected.type) {
			assertEquals "Wrong type for $dependency", expected.type, dependency.type
		}
		this
	}


	static String createPropertyNodeXpath(String propertyName) {
		createRootNodeXPath("properties/pom:$propertyName")
	}

	static String createRootNodeXPath(String node) {
		"/pom:project/pom:$node"
	}


	private def parseProperties() {
		def nodes = eng.getMatchingNodes(createRootNodeXPath('properties/*'), doc)
		for (int i = 0; i < nodes.length; i++) {
			def item = nodes.item(i)
			if (item instanceof Element) {
				def element = (Element) item
				properties[element.tagName] = element.textContent
			}
		}
	}

	private def parseDependencies() {
		def nodes = eng.getMatchingNodes(createRootNodeXPath('dependencies/pom:dependency'), doc)
		for (int i = 0; i < nodes.length; i++) {
			def item = nodes.item(i)
			if (item instanceof Element) {
				def dependency = new Dependency()
				def element = (Element) item
				def groupId = element.getElementsByTagName('groupId')
				if (groupId.length > 0) {
					dependency.groupId = groupId.item(0).textContent
				}
				def artifactId = element.getElementsByTagName('artifactId')
				if (artifactId.length > 0) {
					dependency.artifactId = artifactId.item(0).textContent
				}
				def version = element.getElementsByTagName('version')
				if (version.length > 0) {
					dependency.version = version.item(0).textContent
				}
				def scope = element.getElementsByTagName('scope')
				if (scope.length > 0) {
					dependency.scope = scope.item(0).textContent
				}
				def type = element.getElementsByTagName('type')
				if (type.length > 0) {
					dependency.type = type.item(0).textContent
				}
				def id = dependency.generateId()
				assertFalse("Duplicate dependency with id $id", dependencies.containsKey(id))
				dependencies[id] = dependency
			}
		}
	}


	private static String generateDependencyId(String groupId, String artifactId) {
		def dependency = new Dependency()
		dependency.groupId = groupId
		dependency.artifactId = artifactId
		dependency.generateId()
	}

}
