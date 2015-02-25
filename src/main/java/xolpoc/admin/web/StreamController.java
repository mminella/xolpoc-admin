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

import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.ActualLRPResponse;
import io.pivotal.receptor.commands.DesiredLRPCreateRequest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mark Fisher
 */
@RestController
public class StreamController {

	private final ReceptorClient receptorClient = new ReceptorClient();

	@RequestMapping(value="/")
	public List<ActualLRPResponse> listModules() {
		List<ActualLRPResponse> modules = new ArrayList<ActualLRPResponse>();
		 List<ActualLRPResponse> responses = receptorClient.findAllLongRunningProcesses();
		 for (ActualLRPResponse lrp: responses) {
			 // TODO: aggregate module state for each stream
			 String guid = lrp.getProcessGuid();
			 if (guid.startsWith("xd-") && !"xd-admin".equals(guid)) {
				 modules.add(lrp);
			 }
		 }
		 return modules;
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.POST)
	public void createStream(@PathVariable("name") String name, @RequestBody String definition) {
		String dockerPath = "docker:///pperalta/xd";
		String jarPath = "/opt/xd/lib/xolpoc-0.0.1-SNAPSHOT.jar";
		String[] modules = StringUtils.tokenizeToStringArray(definition, "|");
		for (int i = modules.length - 1; i >= 0; i--) {
			String moduleName = modules[i];
			String moduleType = (i == 0) ? "source" : (i == modules.length - 1) ? "sink" : "processor"; 
			String modulePath = name + "." + moduleType + "." + moduleName + "." + i;
			String guid = "xd-" + name + "-" + moduleName;
			DesiredLRPCreateRequest request = new DesiredLRPCreateRequest();
			request.setProcessGuid(guid);
			request.setRootfs(dockerPath);
			request.runAction.setPath("java");
			request.runAction.addArg("-Dmodule=" + modulePath);
			request.runAction.addArg("-Dspring.redis.host=" + System.getProperty("spring.redis.host"));
			request.runAction.addArg("-Dserver.port=500" + i);
			request.runAction.addArg("-jar");
			request.runAction.addArg(jarPath);
			request.addRoute(8080, new String[] {guid + ".192.168.11.11.xip.io", guid + "-8080.192.168.11.11.xip.io"});
			receptorClient.createLongRunningProcess(request);
		}
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void destroyStream(@PathVariable("name") String name) {
		List<ActualLRPResponse> responses = receptorClient.findAllLongRunningProcesses();
		for (ActualLRPResponse app : responses) {
			if (app.getProcessGuid().startsWith("xd-" + name + "-")) {
				// TODO: capture all and destroy in order of index
				receptorClient.destroyLongRunningProcess(app.getProcessGuid());
			}
		}
	}

}
