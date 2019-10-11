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
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.util.json.JsonHandler;
import com.fortify.util.json.ObjectMapperHelper;

/**
 * This {@link JsonHandler} implementation reads the value that {@link JsonParser}
 * is currently pointing at into a new instance of the configured type using 
 * {@link ObjectMapper}, then calls the configured {@link Consumer} with the
 * mapped value.
 * 
 * @author Ruud Senden
 *
 */
public final class JsonMapperHandler<T> implements JsonHandler {
	private final Consumer<T> consumer;
	private final Class<T> clazz;
	private final ObjectMapper objectMapper; 
	
	public JsonMapperHandler(Consumer<T> consumer, Class<T> clazz) {
		this(consumer, clazz, ObjectMapperHelper.getDefaultObjectMapper());
	}

	public JsonMapperHandler(Consumer<T> consumer, Class<T> clazz, ObjectMapper objectMapper) {
		this.consumer = consumer;
		this.clazz = clazz;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public void handle(JsonParser jsonParser) throws ScanParsingException, IOException {
		consumer.accept(objectMapper.readValue(jsonParser, clazz));
	}
}