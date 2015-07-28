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
package xolpoc.admin.web;

import java.util.Map;

import xolpoc.model.TaskStatus;
import xolpoc.spi.TaskDeployer;
import xolpoc.spi.TaskLauncher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael Minella
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

	@Autowired
	private TaskDeployer deployer;

	@Autowired
	private TaskLauncher launcher;

	@RequestMapping
	public Map<String, TaskStatus> listTasks() {
		System.out.println("About to list " + deployer.list().size() + " tasks");
		return deployer.list();
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.POST)
	public void createTask(@PathVariable("name") String name, @RequestBody String dsl) {
		System.out.println("About to deploy " + name + " with dsl " + dsl);
		deployer.deploy(name, dsl);
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void destroyTask(@PathVariable("name") String name) {
		deployer.undeploy(name);
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void launchTask(@PathVariable("name") String name,
			@RequestParam(value = "args", required = false) String argString) {
		String [] args;

		if(StringUtils.hasText(argString)) {
			args = argString.split(",");
		}
		else {
			args = new String [0];
		}

		//TODO: Replace with call to Spring Cloud Streams?
		launcher.launch(name, args);
	}
//
//	private TaskDescriptor createTaskDescriptor(String name, String dsl) {
//		String[] nameAndOptions = dsl.split("\\s", 2);
//		Map<String, String> parameters = new HashMap<>();
//
//		if (nameAndOptions.length == 2) {
//			String optionsString = nameAndOptions[1];
//			String[] optionTokens = optionsString.split("\\s");
//			for (String s : optionTokens) {
//				String[] kv = s.split("=", 2);
//				parameters.put(kv[0].replaceFirst("--", ""), kv[1]);
//			}
//		}
//
//		return new TaskDescriptor(name, null, name, parameters, nameAndOptions[0]);
//	}
}
