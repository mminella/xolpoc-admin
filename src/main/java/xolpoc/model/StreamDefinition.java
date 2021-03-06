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

package xolpoc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.xd.module.ModuleDescriptor;
import org.springframework.xd.module.ModuleType;

/**
 * @author Mark Fisher
 */
public class StreamDefinition {

	private final String name;

	private final List<ModuleDescriptor> moduleDescriptors = new ArrayList<ModuleDescriptor>();

	public StreamDefinition(String name, String dsl) {
		this.name = name;
		String[] modules = StringUtils.tokenizeToStringArray(dsl, "|");
		for (int i = modules.length - 1; i >= 0; i--) {
			String[] nameAndOptions = modules[i].split("\\s", 2);
			String moduleName = nameAndOptions[0];
			ModuleDescriptor.Builder builder = new ModuleDescriptor.Builder()
					.setModuleName(moduleName)
					.setType((i == 0) ? ModuleType.source : (i == modules.length - 1) ? ModuleType.sink : ModuleType.processor)
					.setGroup(name)
					.setIndex(i);
			if (nameAndOptions.length == 2) {
				String optionsString = nameAndOptions[1];
				String[] optionTokens = optionsString.split("\\s");
				for (String s : optionTokens) {
					String[] kv = s.split("=", 2);
					builder.setParameter(kv[0].replaceFirst("--", ""), kv[1]);
				}
			}
			ModuleDescriptor descriptor = builder.build();
			moduleDescriptors.add(descriptor);
		}
	}

	public String getName() {
		return this.name;
	}

	public List<ModuleDescriptor> getModuleDescriptors() {
		return Collections.unmodifiableList(this.moduleDescriptors);
	}

}
