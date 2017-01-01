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

package com.gwidgets.gwtprojectgenerator.web.project

import org.springframework.web.servlet.resource.ResourceUrlProvider

import javax.servlet.http.HttpServletResponse

import com.gwidgets.gwtprojectgenerator.generator.InvalidProjectRequestException
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadataProvider
import com.gwidgets.gwtprojectgenerator.util.GroovyTemplate

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/**
 * A base controller that uses a {@link InitializrMetadataProvider}
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
abstract class AbstractInitializrController {

	protected final InitializrMetadataProvider metadataProvider
	private final GroovyTemplate groovyTemplate
	private final Closure<String> linkTo
	private Boolean forceSsl

	protected AbstractInitializrController(InitializrMetadataProvider metadataProvider,
										   ResourceUrlProvider resourceUrlProvider,
										   GroovyTemplate groovyTemplate) {
		this.metadataProvider = metadataProvider
		this.groovyTemplate = groovyTemplate
		this.linkTo = { link -> resourceUrlProvider.getForLookupPath(link)?:link }
	}

	boolean isForceSsl() {
		if (this.forceSsl == null) {
			this.forceSsl = metadataProvider.get().configuration.env.forceSsl
		}
		return this.forceSsl;

	}

/*	@ExceptionHandler
	public void invalidProjectRequest(HttpServletResponse response, InvalidProjectRequestException ex) {
		response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
	}*/

	/**
	 * Render the home page with the specified template.
	 */
	protected String renderHome(String templatePath, boolean hasErrors) {
		def metadata = metadataProvider.get()

		def model = [:]
		model['serviceUrl'] = generateAppUrl()
		metadata.properties.each {
			if (it.key.equals('types')) {
				model['types'] = it.value.clone()
			} else {
				model[it.key] = it.value
			}
		}

		// Only keep project type
		model['types'].content.removeAll { t -> !'project'.equals(t.tags['format']) }

		// Google analytics support
		model['trackingCode'] = metadata.configuration.env.googleAnalyticsTrackingCode

		// if there are for submissions errors
		model['hasErrors'] = hasErrors

		// Linking to static resources
		model['linkTo'] = this.linkTo

		groovyTemplate.process templatePath, model
	}

	/**
	 * Generate a full URL of the service, mostly for use in templates.
	 * @see io.spring.initializr.metadata.InitializrConfiguration.Env#forceSsl
	 */
	protected String generateAppUrl() {
		def builder = ServletUriComponentsBuilder.fromCurrentServletMapping()
		if (isForceSsl()) {
			builder.scheme('https')
		}
		builder.build()
	}

	// Total control - setup a model and return the view name yourself. Or
	// consider subclassing ExceptionHandlerExceptionResolver (see below).
	@ExceptionHandler
	public void InvalidProjectRequestException(HttpServletResponse response, InvalidProjectRequestException ex) {

        response.sendRedirect("/?error=1")
       // response.
       // response.getWriter().write(groovyTemplate.process ('home.html', model))

	}
}


