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

package com.gwidgets.gwtprojectgenerator.web

import java.nio.charset.Charset

import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadata
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadataBuilder
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadataProvider
import com.gwidgets.gwtprojectgenerator.metadata.InitializrProperties
import com.gwidgets.gwtprojectgenerator.test.generator.ProjectAssert
import com.gwidgets.gwtprojectgenerator.web.mapper.InitializrMetadataVersion
import com.gwidgets.gwtprojectgenerator.web.support.DefaultInitializrMetadataProvider
import org.json.JSONObject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestTemplate

import static org.junit.Assert.assertTrue
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * @author Stephane Nicoll
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Config.class, webEnvironment = RANDOM_PORT)
abstract class AbstractInitializrControllerIntegrationTests {

	static final MediaType CURRENT_METADATA_MEDIA_TYPE = InitializrMetadataVersion.V2_1.mediaType


	@Rule
	public final TemporaryFolder folder = new TemporaryFolder()

	@LocalServerPort
	protected int port

	final RestTemplate restTemplate = new RestTemplate()

	String createUrl(String context) {
		"http://localhost:$port$context"
	}

	String htmlHome() {
		def headers = new HttpHeaders()
		headers.setAccept([MediaType.TEXT_HTML])
		restTemplate.exchange(createUrl('/'), HttpMethod.GET, new HttpEntity<Void>(headers), String).body
	}

	/**
	 * Validate the 'Content-Type' header of the specified response.
	 */
	protected void validateContentType(ResponseEntity<String> response, MediaType expected) {
		def actual = response.headers.getContentType()
		assertTrue "Non compatible media-type, expected $expected, got $actual",
				actual.isCompatibleWith(expected)
	}


	protected void validateMetadata(ResponseEntity<String> response, MediaType mediaType,
									String version, JSONCompareMode compareMode) {
		validateContentType(response, mediaType)
		def json = new JSONObject(response.body)
		def expected = readMetadataJson(version)
		JSONAssert.assertEquals(expected, json, compareMode)
	}

	protected void validateCurrentMetadata(ResponseEntity<String> response) {
		validateContentType(response, CURRENT_METADATA_MEDIA_TYPE)
		validateCurrentMetadata(new JSONObject(response.body))
	}

	protected void validateCurrentMetadata(JSONObject json) {
		def expected = readMetadataJson('2.1.0')
		JSONAssert.assertEquals(expected, json, JSONCompareMode.STRICT)
	}

	private JSONObject readMetadataJson(String version) {
		readJsonFrom("metadata/test-default-$version" + ".json")
	}

	/**
	 * Return a {@link ProjectAssert} for the following archive content.
	 */
	protected ProjectAssert zipProjectAssert(byte[] content) {
		projectAssert(content, ArchiveType.ZIP)
	}

	/**
	 * Return a {@link ProjectAssert} for the following TGZ archive.
	 */
	protected ProjectAssert tgzProjectAssert(byte[] content) {
		projectAssert(content, ArchiveType.TGZ)
	}

	protected ProjectAssert downloadZip(String context) {
		def body = downloadArchive(context)
		zipProjectAssert(body)
	}

	protected ProjectAssert downloadTgz(String context) {
		def body = downloadArchive(context)
		tgzProjectAssert(body)
	}

	protected byte[] downloadArchive(String context) {
		restTemplate.getForObject(createUrl(context), byte[])
	}

	protected ResponseEntity<String> invokeHome(String userAgentHeader, String... acceptHeaders) {
		execute('/', String, userAgentHeader, acceptHeaders)
	}

	protected <T> ResponseEntity<T> execute(String contextPath, Class<T> responseType,
											String userAgentHeader, String... acceptHeaders) {
		HttpHeaders headers = new HttpHeaders();
		if (userAgentHeader) {
			headers.set("User-Agent", userAgentHeader);
		}
		if (acceptHeaders) {
			List<MediaType> mediaTypes = new ArrayList<>()
			for (String acceptHeader : acceptHeaders) {
				mediaTypes.add(MediaType.parseMediaType(acceptHeader))
			}
			headers.setAccept(mediaTypes)
		} else {
			headers.setAccept(Collections.emptyList())
		}
		return restTemplate.exchange(createUrl(contextPath),
				HttpMethod.GET, new HttpEntity<Void>(headers), responseType)
	}

	protected ProjectAssert projectAssert(byte[] content, ArchiveType archiveType) {
		def archiveFile = writeArchive(content)

		def project = folder.newFolder()
		switch (archiveType) {
			case ArchiveType.ZIP:
				new AntBuilder().unzip(dest: project, src: archiveFile)
				break
			case ArchiveType.TGZ:
				new AntBuilder().untar(dest: project, src: archiveFile, compression: 'gzip')
				break
		}
		new ProjectAssert(project)
	}

	protected File writeArchive(byte[] body) {
		def archiveFile = folder.newFile()
		def stream = new FileOutputStream(archiveFile)
		try {
			stream.write(body)
		} finally {
			stream.close()
		}
		archiveFile
	}

	protected JSONObject readJsonFrom(String path) {
		def resource = new ClassPathResource(path)
		def stream = resource.inputStream
		try {
			def json = StreamUtils.copyToString(stream, Charset.forName('UTF-8'))

			// Let's parse the port as it is random
			def content = json.replaceAll('@port@', String.valueOf(this.port))
			new JSONObject(content)
		} finally {
			stream.close()
		}
	}


	private enum ArchiveType {
		ZIP,

		TGZ
	}

	@EnableAutoConfiguration
	static class Config {

		@Bean
		InitializrMetadataProvider initializrMetadataProvider(InitializrProperties properties) {
			new DefaultInitializrMetadataProvider(
					InitializrMetadataBuilder.fromInitializrProperties(properties).build(),
					new RestTemplate()) {

				protected void updateInitializrMetadata(InitializrMetadata metadata) {
					null // Disable metadata fetching from spring.io
				}
			}
		}

	}
}
