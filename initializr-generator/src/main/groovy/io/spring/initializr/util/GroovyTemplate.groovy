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

package io.spring.initializr.util

import groovy.util.logging.Slf4j

import java.util.concurrent.ConcurrentMap

import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import org.codehaus.groovy.control.CompilationFailedException

import org.springframework.util.ConcurrentReferenceHashMap

/**
 * @author Dave Syer
 * @since 1.0
 */
@Slf4j
class GroovyTemplate {

	boolean cache = true

	private final TemplateEngine engine
	private final ConcurrentMap<String, Template> templateCaches = new ConcurrentReferenceHashMap<>()

	GroovyTemplate(TemplateEngine engine) {
		this.engine = engine
	}

	GroovyTemplate() {
		this(new GStringTemplateEngine())
	}

	String process(String name, Map<String, ?> model)
			throws IOException, CompilationFailedException, ClassNotFoundException {
		def template = getTemplate(name)
		def writable = template.make(model)
		log.info("writable " + writable.toString())
		def result = new StringWriter()
		writable.writeTo(result)
		result.toString()
	}

	Template getTemplate(String name)
			throws CompilationFailedException, ClassNotFoundException, IOException {
		if (cache) {
			return this.templateCaches.computeIfAbsent(name, { n -> loadTemplate(n) })
		}
		return loadTemplate(name)
	}

	protected Template loadTemplate(String name) {
		def file = new File("templates", name)
		if (file.exists()) {
			return engine.createTemplate(file)
		}

		def classLoader = GroovyTemplate.class.classLoader
		def resource = classLoader.getResource("templates/" + name)
		if (resource) {
			return engine.createTemplate(resource.getText('utf-8'))
		}

		return engine.createTemplate(name)
	}

}
