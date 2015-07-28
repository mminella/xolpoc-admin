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
package xolpoc.spi.defaults;

import java.util.HashMap;
import java.util.Map;

import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;
import xolpoc.spi.TaskDeployer;
import xolpoc.spi.TaskDescriptorRepository;
import xolpoc.spi.TaskLauncher;

import org.springframework.util.Assert;

/**
 * @author Michael Minella
 */
public class SimpleTaskDeployer implements TaskDeployer {

	private TaskDescriptorRepository repository;

	private TaskLauncher launcher;

	public SimpleTaskDeployer(TaskDescriptorRepository repository, TaskLauncher launcher) {
		Assert.notNull(repository, "A TaskDescriptorRepository is required");
		Assert.notNull(launcher, "A TaskLauncher is required");

		this.repository = repository;
		this.launcher = launcher;
	}

	@Override
	public void deploy(String name, String dsl) {
		repository.create(name, dsl);
	}

	@Override
	public Map<String, TaskStatus> list() {
		Map<String, TaskStatus> results = new HashMap<>();

		for (Map.Entry<String, TaskDescriptor> entry : repository.findAll().entrySet()) {
			TaskStatus status = launcher.getStatus(entry.getValue());

			results.put(entry.getKey(), status);
		}

		return results;
	}

	@Override
	public void undeploy(String name) {
		TaskDescriptor definition = repository.find(name);
		if (definition == null) {
			throw new IllegalArgumentException("unable to find definition for: " + name);
		}

		repository.delete(name);
	}
}
