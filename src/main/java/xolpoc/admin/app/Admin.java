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

import static xolpoc.spi.receptor.ReceptorModuleDeployer.ADMIN_GUID;
import static xolpoc.spi.receptor.ReceptorModuleDeployer.BASE_ADDRESS;
import static xolpoc.spi.receptor.ReceptorModuleDeployer.DOCKER_PATH;
import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.DesiredLRPCreateRequest;

import java.net.Inet4Address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.xd.analytics.metrics.core.AggregateCounterRepository;
import org.springframework.xd.analytics.metrics.core.FieldValueCounterRepository;
import org.springframework.xd.dirt.rest.TempAccessControlInterceptor;
import org.springframework.xd.dirt.rest.metrics.AggregateCountersController;
import org.springframework.xd.dirt.rest.metrics.FieldValueCountersController;

import xolpoc.admin.web.AdminController;
import xolpoc.admin.web.StreamController;

/**
 * @author Mark Fisher
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ImportResource("classpath*:/META-INF/spring-xd/analytics/redis-analytics.xml")
@Import({AdminController.class, StreamController.class})
public class Admin {

	private static final String ADMIN_JAR_PATH = "/opt/xd/lib/xolpoc-admin-0.0.1-SNAPSHOT.jar";

	public static void main(String[] args) throws Exception {
		String busHost = System.getProperty("spring.redis.host");
		if (busHost == null) { // bootstrap
			busHost = Inet4Address.getLocalHost().getHostAddress();
			DesiredLRPCreateRequest admin = new DesiredLRPCreateRequest();
			admin.setProcessGuid(ADMIN_GUID);
			admin.setRootfs(DOCKER_PATH);
			admin.setInstances(1);
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

	@Autowired
	FieldValueCounterRepository fieldValueCounterRepository;

	@Autowired
	AggregateCounterRepository aggregateCounterRepository;

	@Bean
	public FieldValueCountersController fieldValueCountersController() {
		return new FieldValueCountersController(fieldValueCounterRepository);
	}

	@Bean
	public AggregateCountersController aggregateCountersController() {
		return new AggregateCountersController(aggregateCounterRepository);
	}

	@Bean
	public WebMvcConfigurer configurer() {
		return new WebMvcConfigurerAdapter() {

			@Value("${xd.ui.allow_origin:http://localhost:9889}")
			private String allowedOrigin;

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(new TempAccessControlInterceptor(allowedOrigin));
			}
		};
	}

	@Configuration
	@ConditionalOnProperty("PROCESS_GUID")
	protected static class LatticeConfig extends AbstractCloudConfig {

		@Bean
		RedisConnectionFactory redisConnectionFactory() {
			return connectionFactory().redisConnectionFactory();
		}
	}
}
