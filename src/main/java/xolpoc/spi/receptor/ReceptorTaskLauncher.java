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
package xolpoc.spi.receptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pivotal.receptor.actions.RunAction;
import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.TaskCreateRequest;
import io.pivotal.receptor.commands.TaskResponse;
import io.pivotal.receptor.support.EnvironmentVariable;
import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;
import xolpoc.spi.TaskDescriptorRepository;
import xolpoc.spi.TaskLauncher;

import org.springframework.util.Assert;

/**
 * @author Michael Minella
 */
public class ReceptorTaskLauncher implements TaskLauncher {

	public static final String DOCKER_PATH = "docker://192.168.59.103:5000/module-launcher";

	private final ReceptorClient receptorClient = new ReceptorClient();

	private TaskDescriptorRepository repository;

	public ReceptorTaskLauncher(TaskDescriptorRepository repository) {
		Assert.notNull(repository, "A TaskDescriptorRepository is required");

		this.repository = repository;
	}

	@Override
	public void launch(String taskName, String[] args) {

		TaskDescriptor taskDescriptor = repository.find(taskName);

		Map<String, RunAction> action = new HashMap<>();
		RunAction runAction = new RunAction();
		runAction.setPath("java");
		runAction.setArgs(new String[] {"-Djava.security.egd=file:/dev/./urandom", "-jar",
				String.format("/opt/spring/modules/%s.jar", taskDescriptor.getModuleName())});
		action.put("run", runAction);

		TaskCreateRequest request = new TaskCreateRequest();
		request.setTaskGuid(taskDescriptor.getGuid());
		request.setRootfs(DOCKER_PATH);
		request.setLogGuid(request.getTaskGuid());
		request.setAction(action);

		List<EnvironmentVariable> environmentVariables = new ArrayList<>();

		Collections.addAll(environmentVariables, request.getEnv());

		environmentVariables.add(new EnvironmentVariable("SPRING_PROFILES_ACTIVE", "cloud"));
		Map<String, String> parameters = taskDescriptor.getParameters();
		if (parameters != null && parameters.size() > 0) {
			for (Map.Entry<String, String> option : parameters.entrySet()) {
				environmentVariables.add(
						new EnvironmentVariable("OPTION_" + option.getKey(), option.getValue()));
			}
		}
		request.setEnv(environmentVariables.toArray(new EnvironmentVariable[environmentVariables.size()]));

		receptorClient.createTask(request);
	}

	@Override
	public TaskStatus getStatus(TaskDescriptor descriptor) {
		TaskResponse task = receptorClient.getTask(descriptor.getGuid());
		return new TaskStatus(descriptor,
				descriptor.getGuid(),
				task.getState(),
				task.isFailed(),
				task.getFailureReason());
	}
}
