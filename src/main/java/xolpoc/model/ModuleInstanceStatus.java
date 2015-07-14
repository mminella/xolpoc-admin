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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Fisher
 */
public class ModuleInstanceStatus {

	private final String id;

	private final String state;

	private final Map<String, String> attributes = new HashMap<String, String>();

	public ModuleInstanceStatus(String id, String state, Map<String, String> attributes) {
		this.id = id;
		this.state = (state != null ? state : "unknown");
		this.attributes.putAll(attributes);
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}
}
