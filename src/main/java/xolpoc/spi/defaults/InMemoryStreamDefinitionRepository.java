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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import xolpoc.model.StreamDefinition;
import xolpoc.spi.StreamDefinitionRepository;

/**
 * @author Mark Fisher
 */
public class InMemoryStreamDefinitionRepository implements StreamDefinitionRepository {

	private final Map<String, StreamDefinition> definitions = new HashMap<String, StreamDefinition>();

	@Override
	public Map<String, StreamDefinition> findAll() {
		return Collections.unmodifiableMap(definitions);
	}

	@Override
	public StreamDefinition find(String name) {
		return definitions.get(name);
	}

	@Override
	public StreamDefinition create(String name, String dsl) {
		StreamDefinition definition = new StreamDefinition(name, dsl);
		definitions.put(name, definition);
		return definition;
	}

	@Override
	public boolean delete(String name) {
		return definitions.remove(name) != null;
	}

}
