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

package com.gwidgets.gwtprojectgenerator.actuate.metric

import com.gwidgets.gwtprojectgenerator.actuate.test.RedisRunning
import com.gwidgets.gwtprojectgenerator.generator.ProjectGeneratedEvent
import com.gwidgets.gwtprojectgenerator.generator.ProjectRequest
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadataBuilder
import com.gwidgets.gwtprojectgenerator.metadata.InitializrMetadataProvider
import com.gwidgets.gwtprojectgenerator.metadata.InitializrProperties
import com.gwidgets.gwtprojectgenerator.metadata.SimpleInitializrMetadataProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository
import org.springframework.boot.actuate.metrics.writer.MetricWriter
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner

import static org.junit.Assert.assertTrue

/**
 * @author Dave Syer
 */
@RunWith(SpringRunner)
@SpringBootTest(classes = Config, properties = ['spring.metrics.export.delayMillis:500',
		'spring.metrics.export.enabled:true',
		'initializr.metrics.prefix:test.prefix', 'initializr.metrics.key:key.test'])
class MetricsExportTests {

	@Rule
	public RedisRunning running = new RedisRunning()

	@Autowired
	ProjectGenerationMetricsListener listener

	@Autowired
	@Qualifier("writer")
	MetricWriter writer

	RedisMetricRepository repository

	@Before
	void init() {
		repository = (RedisMetricRepository) writer
		repository.findAll().each {
			repository.reset(it.name)
		}
		assertTrue("Metrics not empty", repository.findAll().size() == 0)
	}

	@Test
	void exportAndCheckMetricsExist() {
		listener.onGeneratedProject(new ProjectGeneratedEvent(new ProjectRequest()))
		Thread.sleep(1000L)
		assertTrue("No metrics exported", repository.findAll().size() > 0)
	}

	@EnableAutoConfiguration
	@EnableConfigurationProperties(InitializrProperties)
	static class Config {

		@Bean
		InitializrMetadataProvider initializrMetadataProvider(InitializrProperties properties) {
			def metadata = InitializrMetadataBuilder.fromInitializrProperties(properties).build()
			new SimpleInitializrMetadataProvider(metadata)
		}
	}
}
