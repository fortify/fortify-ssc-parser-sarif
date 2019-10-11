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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.util.json.JsonHandler;
import com.fortify.util.json.AbstractStreamingJsonParser;

/**
 * This {@link JsonHandler} implementation iterates over the
 * array that the given {@link JsonParser} is currently
 * pointing at, and calls the {@link JsonHandler#handle(JsonParser)}
 * method of the configured array entry {@link JsonHandler}. The
 * configured {@link JsonHandler} is required to move the pointer to 
 * the end of the array entry.
 * 
 * @author Ruud Senden
 *
 */
public class JsonArrayHandler implements JsonHandler {
	private final JsonHandler arrayEntryHandler;
	
	public JsonArrayHandler(JsonHandler arrayEntryHandler) {
		this.arrayEntryHandler = arrayEntryHandler;
	}

	@Override
	public final void handle(JsonParser jsonParser) throws ScanParsingException, IOException {
		AbstractStreamingJsonParser.assertStartArray(jsonParser);
		while (jsonParser.nextToken()!=JsonToken.END_ARRAY) {
			arrayEntryHandler.handle(jsonParser);
		}
		
	}
}