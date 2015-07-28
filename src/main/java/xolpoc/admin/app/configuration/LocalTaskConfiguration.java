/*
 * Copyright 2015 the original author or authors.
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
package xolpoc.admin.app.configuration;

import xolpoc.spi.TaskDeployer;
import xolpoc.spi.TaskDescriptorRepository;
import xolpoc.spi.TaskLauncher;
import xolpoc.spi.defaults.InMemoryTaskDescriptorRepository;
import xolpoc.spi.defaults.SimpleTaskDeployer;
import xolpoc.spi.local.LocalTaskLauncher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Michael Minella
 */
@Configuration
@Profile("default")
public class LocalTaskConfiguration {

	@Bean
	public TaskDeployer deployer() {
		return new SimpleTaskDeployer(repository(), launcher());
	}

	@Bean
	public TaskDescriptorRepository repository() {
		return new InMemoryTaskDescriptorRepository();
	}

	@Bean
	public TaskLauncher launcher() {
		return new LocalTaskLauncher(repository());
	}

}
