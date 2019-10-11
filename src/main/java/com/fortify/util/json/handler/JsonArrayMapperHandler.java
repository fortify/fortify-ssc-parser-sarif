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

import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.util.json.JsonHandler;

/**
 * This {@link JsonHandler} implementation iterates over the
 * array that the given {@link JsonParser} is currently
 * pointing at. Each array entry will be read into a new instance
 * of the configured type using {@link ObjectMapper}; for each mapped
 * array entry the configured {@link Consumer} will be called.
 * 
 * @author Ruud Senden
 *
 */
public class JsonArrayMapperHandler<T> extends JsonArrayHandler {
	public JsonArrayMapperHandler(Consumer<T> consumer, Class<T> clazz) {
		super(new JsonMapperHandler<>(consumer, clazz));
	}
	
	public JsonArrayMapperHandler(Consumer<T> consumer, Class<T> clazz, ObjectMapper objectMapper) {
		super(new JsonMapperHandler<>(consumer, clazz, objectMapper));
	}
}