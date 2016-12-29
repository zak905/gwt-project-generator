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

package com.gwidgets.gwtprojectgenerator.web.ui

import java.nio.charset.StandardCharsets

import groovy.json.JsonBuilder
import com.gwidgets.gwtprojectgenerator.metadata.Dependency
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadataProvider
import com.gwidgets.gwtprojectgenerator.util.Version
import com.gwidgets.gwtprojectgenerator.util.VersionRange

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * UI specific controller providing dedicated endpoints for the Web UI.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
@RestController
class UiController {

	@Autowired
	protected InitializrMetadataProvider metadataProvider

	@RequestMapping(value = "/ui/dependencies", produces = ["application/json"])
	ResponseEntity<String> dependencies(@RequestParam(required = false) String version) {
		def dependencyGroups = metadataProvider.get().dependencies.content
		def content = []
		Version v = version ? Version.parseWithoutQualifier(version) : null
		dependencyGroups.each { g ->
			g.content.each { d ->
				if (v && d.versionRange) {
					if (VersionRange.parseWithoutQualifier(d.versionRange).match(v)) {
						content << new DependencyItem(g.name, d)
					}
				} else {
					content << new DependencyItem(g.name, d)
				}
			}
		}
		def json = writeDependencies(content)
		ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
				eTag(createUniqueId(json)).body(json)
	}

	private static String writeDependencies(List<DependencyItem> items) {
		JsonBuilder json = new JsonBuilder();
		json {
			dependencies items.collect { d ->
				mapDependency(d)
			}
		}
		json.toString()
	}

	private static mapDependency(DependencyItem item) {
		def result = [:]
		Dependency d = item.dependency
		result.id = d.id
		result.name = d.name
		result.group = item.group
		if (d.description) {
			result.description = d.description
		}
		if (d.weight) {
			result.weight = d.weight
		}
		if (d.keywords || d.aliases) {
			def all = d.keywords + d.aliases
			result.keywords = all.join(',')
		}
		result
	}

	private static class DependencyItem {
		private final String group
		private final Dependency dependency

		DependencyItem(String group, Dependency dependency) {
			this.group = group
			this.dependency = dependency
		}
	}

	private String createUniqueId(String content) {
		StringBuilder builder = new StringBuilder()
		DigestUtils.appendMd5DigestAsHex(content.getBytes(StandardCharsets.UTF_8), builder)
		builder.toString()
	}

}
