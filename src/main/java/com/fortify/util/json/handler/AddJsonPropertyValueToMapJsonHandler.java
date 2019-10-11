/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.util.json.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.util.json.JsonHandler;
import com.fortify.util.json.ObjectMapperHelper;

/**
 * <p>This {@link JsonHandler} implementation reads a JSON object
 * property value into a new instance of the given {@link Class},
 * and adds this instance to a {@link Map}, using the JSON object 
 * property name as the map key.</p>
 * 
 * <p>Example usage:</p>
 * 
 * <p>Given the example implementation and JSON input below, after 
 * parsing the <code>handler.getMap()</code> method will return a
 * map containing <code>prop1</code> and <code>prop2</code> as keys, 
 * with corresponding <code>MyClazz</code> instances as values.</p>
 * 
 * Class: <pre>
 * public final class MyClazz {
 *   @JsonProperty int x;
 *   @JsonProperty int y;
 * }
 * </pre>
 * 
 * JSON: <pre>
 * "someObject": {
 *   "prop1": {"x": 1, "y": 2},
 *   "prop2": {"x": 5, "y": 10}
 * }
 * </pre>
 * 
 * Parser configuration: <pre>
 * AddJsonPropertyValueToMapJsonHandler<MyClazz> handler = new AddJsonPropertyValueToMapJsonHandler<>(MyClazz.class);
 * protected void addHandlers(Map<String, JsonHandler> pathToHandlerMap) {
 *   pathToHandlerMap.put("/someObject/*", handler);
 * }
 * </pre>
 *
 * @author Ruud Senden
 *
 * @param <T>
 */
public final class AddJsonPropertyValueToMapJsonHandler<T> implements JsonHandler {
	private final Map<String,T> map;
	private final Class<T> clazz;
	private final ObjectMapper objectMapper; 
	
	public AddJsonPropertyValueToMapJsonHandler(Class<T> clazz) {
		this(new LinkedHashMap<String,T>(), clazz);
	}
	
	public AddJsonPropertyValueToMapJsonHandler(Map<String,T> map, Class<T> clazz) {
		this(map, clazz, ObjectMapperHelper.getDefaultObjectMapper());
	}
	
	public AddJsonPropertyValueToMapJsonHandler(Map<String,T> map, Class<T> clazz, ObjectMapper objectMapper) {
		this.map = map;
		this.clazz = clazz;
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(JsonParser jsonParser) throws ScanParsingException, IOException {
		map.put(jsonParser.getCurrentName(), objectMapper.readValue(jsonParser, clazz));			
	}

	public final Map<String,T> getMap() {
		return Collections.unmodifiableMap(map);
	}
}