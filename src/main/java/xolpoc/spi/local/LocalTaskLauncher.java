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
package xolpoc.spi.local;

import java.io.File;

import xolpoc.model.NoTaskFoundException;
import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;
import xolpoc.spi.TaskDescriptorRepository;
import xolpoc.spi.TaskLauncher;

import org.springframework.cloud.stream.module.launcher.ModuleLauncher;
import org.springframework.util.Assert;

/**
 * @author Michael Minella
 */
public class LocalTaskLauncher implements TaskLauncher {

	private TaskDescriptorRepository repository;
	private ModuleLauncher launcher = new ModuleLauncher(new File("/opt/spring/modules"));

	public LocalTaskLauncher(TaskDescriptorRepository repository) {
		Assert.notNull(repository, "A TaskDescriptorRepository is required");

		this.repository = repository;
	}

	@Override
	public void launch(String taskName, String[] args) throws NoTaskFoundException {
		TaskDescriptor taskDescriptor = repository.find(taskName);

		if(taskDescriptor == null) {
			throw new NoTaskFoundException(String.format("Unable to find task %s to launch", taskName));
		}

		launcher.launch(new String[] {taskDescriptor.getModuleName()}, args);
	}

	@Override
	public TaskStatus getStatus(TaskDescriptor descriptor) {
		return null;
//		throw new UnsupportedOperationException();
	}
}
