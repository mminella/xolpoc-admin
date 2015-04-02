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

package xolpoc.admin.app;

import static xolpoc.spi.receptor.ReceptorModuleDeployer.*;
import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.DesiredLRPCreateRequest;

import java.net.Inet4Address;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

import xolpoc.admin.web.StreamController;

/**
 * @author Mark Fisher
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@Import(StreamController.class)
public class Admin {

	private static final String ADMIN_JAR_PATH = "/opt/xd/lib/xolpoc-admin-0.0.1-SNAPSHOT.jar";

	public static void main(String[] args) throws Exception {
		String busHost = System.getProperty("spring.redis.host");
		if (busHost == null) { // bootstrap
			busHost = Inet4Address.getLocalHost().getHostAddress();
			DesiredLRPCreateRequest admin = new DesiredLRPCreateRequest();
			admin.setProcessGuid(ADMIN_GUID);
			admin.setRootfs(DOCKER_PATH);
			admin.setInstances(2);
			admin.runAction().setPath("java");
			admin.runAction().addArg("-Dspring.redis.host=" + busHost);
			admin.runAction().addArg("-jar");
			admin.runAction().addArg(ADMIN_JAR_PATH);
			admin.addRoute(8080, new String[] { ADMIN_GUID + "." + BASE_ADDRESS, ADMIN_GUID + "-8080." + BASE_ADDRESS});
			ReceptorClient client = new ReceptorClient();
			client.createDesiredLRP(admin);
		}
		else {
			SpringApplication.run(Admin.class, args);
		}
	}

}
