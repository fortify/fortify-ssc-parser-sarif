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
package com.fortify.util.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.util.io.Region;
import com.fortify.util.io.RegionInputStream;

/**
 * This abstract class provides functionality for stream-based parsing of arbitrary JSON 
 * structures. 
 * TODO Add more information/examples how to use the various
 *      parse methods.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractStreamingJsonParser<T extends AbstractStreamingJsonParser<T>> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamingJsonParser.class);
	private static final JsonFactory JSON_FACTORY = new JsonFactory().disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
	private static final Set<JsonToken> SET_START_OBJECT = new HashSet<>(Arrays.asList(JsonToken.START_OBJECT));
	private static final Set<JsonToken> SET_START_ARRAY = new HashSet<>(Arrays.asList(JsonToken.START_ARRAY));
	private static final Set<JsonToken> SET_START_OBJECT_OR_ARRAY = new HashSet<>(Arrays.asList(JsonToken.START_OBJECT, JsonToken.START_ARRAY));
	private final Map<String, JsonHandler> pathToHandlerMap = new LinkedHashMap<>();
	private Set<JsonToken> expectedStartTokens = SET_START_OBJECT;
	@SuppressWarnings("unchecked")
	private T _this = (T)this;

	public final T handler(String path, JsonHandler handler) {
		addHandlerAndParentHandlers(path, handler);
		return _this;
	}
	
	public final T expectedStartTokens(JsonToken... jsonTokens) {
		expectedStartTokens = new HashSet<>(Arrays.asList(jsonTokens));
		return _this;
	}
	
	/**
	 * This method adds the given handler to {@link #pathToHandlerMap},
	 * then calls {@link #addParentHandlers(String)} to add intermediate
	 * handlers for reaching the given handler.
	 * 
	 * @param pathToHandlerMap
	 */
	private final void addHandlerAndParentHandlers(String path, JsonHandler handler) {
		pathToHandlerMap.put(path, handler);
		addParentHandlers(path);
	}

	private final void addParentHandlers(String path) {
		LOG.debug("Adding parent handlers for "+path);
		String[] pathElts = path.split("/");
		String currentPath = "";
		for ( String pathElt : pathElts ) {
			currentPath = getPath(currentPath, pathElt);
			addParentHandler(currentPath);
		}
	}
	
	/**
	 * This method adds an intermediate handler for traversing into the given path.
	 * @param pathToHandlerMap
	 * @param path
	 */
	private final void addParentHandler(final String path) {
		if ( !pathToHandlerMap.containsKey(path) ) {
			LOG.debug("Adding parent handler for "+path);
			pathToHandlerMap.put(path, jsonParser->parseObjectOrArrayChildren(jsonParser, path));
		}
	}

	/**
	 * Parse JSON contents retrieved from the given {@link InputStream} using
	 * the previously configured handlers.
	 */ 
	public final void parse(InputStream inputStream) throws ScanParsingException, IOException {
		parse(inputStream, null);
	}
	
	/**
	 * Parse JSON contents retrieved from the given {@link InputStream} object
	 * for the given input region, using the previously configured handlers.
	 */
	public final void parse(InputStream inputStream, Region inputRegion) throws ScanParsingException, IOException {
		try ( final InputStream content = new RegionInputStream(
					inputStream, inputRegion, false);
				final JsonParser jsonParser = JSON_FACTORY.createParser(content)) {
			jsonParser.nextToken();
			assertToken(jsonParser, expectedStartTokens);
			parse(jsonParser, "/");
		}
	}

	/**
	 * This method checks whether a {@link JsonHandler} has been registered for the 
	 * current JSON element. If a {@link JsonHandler} is found, this method will simply
	 * invoke the {@link JsonHandler} to parse the contents of the current JSON element.
	 * If no {@link JsonHandler} is found, this method will skip all children of the
	 * current JSON element.
	 * 
	 * This method simply returns after handling the current JSON elements; recursive
	 * parsing is handled by registered {@link JsonHandler} instances.
	 * 
	 * @param jsonParser
	 * @param parentPath
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	private final void parse(final JsonParser jsonParser, String parentPath) throws ScanParsingException, IOException {
		JsonToken currentToken = jsonParser.getCurrentToken();
		if ( currentToken != null && (currentToken==JsonToken.START_ARRAY || currentToken==JsonToken.START_OBJECT || currentToken.isScalarValue())) {
			String currentPath = getPath(parentPath, jsonParser.getCurrentName());
			LOG.trace("Processing "+currentPath);
			JsonHandler handler = pathToHandlerMap.computeIfAbsent(currentPath, k->pathToHandlerMap.get(getPath(parentPath, "*")));
			if ( handler != null ) {
				LOG.debug("Handling "+currentPath);
				handler.handle(jsonParser);
			} else {
				skipChildren(jsonParser);
			}
		}
	}
	
	/**
	 * Append the given currentName to the given parentPath,
	 * correctly handling the separator.
	 * 
	 * @param parentPath
	 * @param currentName
	 * @return
	 */
	private final String getPath(String parentPath, String currentName) {
		String result = parentPath;
		if ( currentName!=null ) {
			result+=result.endsWith("/")?"":"/";
			result+=currentName;
		}
		if ( "".equals(result) ) { result="/"; }
		return result;
	}

	/**
	 * Parse the children of the current JSON object or JSON array.
	 * 
	 * @param jsonParser
	 * @param currentPath
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	private final void parseObjectOrArrayChildren(JsonParser jsonParser, String currentPath) throws ScanParsingException, IOException {
		JsonToken currentToken = jsonParser.getCurrentToken();
		if ( currentToken==JsonToken.START_OBJECT ) {
			parseObjectProperties(jsonParser, currentPath);
		} else if ( currentToken==JsonToken.START_ARRAY ) {
			parseArrayEntries(jsonParser, currentPath);
		}
	}
	
	/**
	 * Parse the individual object properties of the current JSON object.
	 * 
	 * @param jsonParser
	 * @param currentPath
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public final void parseObjectProperties(JsonParser jsonParser, String currentPath) throws ScanParsingException, IOException {
		parseChildren(jsonParser, currentPath, JsonToken.END_OBJECT);
	}
	
	/**
	 * Parse the individual array entries of the current JSON array.
	 * 
	 * @param jsonParser
	 * @param currentPath
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public final void parseArrayEntries(JsonParser jsonParser, String currentPath) throws ScanParsingException, IOException {
		parseChildren(jsonParser, currentPath, JsonToken.END_ARRAY);
	}
	
	/**
	 * Parse the children of the current JSON element, up to the given endToken
	 * 
	 * @param jsonParser
	 * @param currentPath
	 * @param endToken
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	private final void parseChildren(JsonParser jsonParser, String currentPath, JsonToken endToken) throws ScanParsingException, IOException {
		while (jsonParser.nextToken()!=endToken) {
			parse(jsonParser, currentPath);
		}
	}
	
	/**
	 * Assuming the given {@link JsonParser} instance is currently pointing at a JSON array,
	 * this method will return the number of array entries.
	 * 
	 * @param jsonParser
	 * @return
	 * @throws ScanParsingException Thrown if the given JsonParser does not
	 *         point at the start of an array
	 * @throws IOException
	 */
	public static final int countArrayEntries(JsonParser jsonParser) throws ScanParsingException, IOException {
		assertStartArray(jsonParser);
		int result = 0;
		while (jsonParser.nextToken()!=JsonToken.END_ARRAY) {
			result++;
			skipChildren(jsonParser);
		}
		return result;
	}
	
	/**
	 * Assuming the given {@link JsonParser} instance is currently pointing at a JSON object,
	 * this method will return the number of object entries.
	 * 
	 * @param jsonParser
	 * @return
	 * @throws ScanParsingException Thrown if the given JsonParser does not
	 *         point at the start of an object
	 * @throws IOException
	 */
	public static final int countObjectEntries(JsonParser jsonParser) throws ScanParsingException, IOException {
		assertStartObject(jsonParser);
		int result = 0;
		while (jsonParser.nextToken()!=JsonToken.END_OBJECT) {
			result++;
			skipChildren(jsonParser);
		}
		return result;
	}
	
	/**
	 * Get the region (start and end position) of the current JSON element.
	 * 
	 * @param jsonParser
	 * @return
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public static final Region getObjectOrArrayRegion(JsonParser jsonParser) throws ScanParsingException, IOException {
		assertStartObjectOrArray(jsonParser);
		// TODO Do we need to take into account file encoding to determine number of bytes
		//      for the '[' character?
		long start = jsonParser.getCurrentLocation().getByteOffset()-"[".getBytes().length; 
		jsonParser.skipChildren();
		long end = jsonParser.getCurrentLocation().getByteOffset();
		return new Region(start, end);
	}
	
	/**
	 * If the given {@link JsonParser} is currently pointing at a JSON object or array,
	 * this method will skip all children of that object or array.
	 * @param jsonParser
	 * @throws IOException
	 */
	public static final void skipChildren(final JsonParser jsonParser) throws IOException {
		switch (jsonParser.getCurrentToken()) {
		case START_ARRAY:
		case START_OBJECT:
			LOG.trace("Skipping children");
			jsonParser.skipChildren();
			break;
		default: break;
		}
	}
	
	/**
	 * Assert that the given {@link JsonParser} is currently pointing at the start tag of a 
	 * JSON array, throwing a {@link ScanParsingException} otherwise.
	 * @param jsonParser
	 * @throws ScanParsingException
	 */
	public static final void assertStartArray(final JsonParser jsonParser) throws ScanParsingException {
		assertToken(jsonParser, SET_START_ARRAY);
	}

	/**
	 * Assert that the given {@link JsonParser} is currently pointing at the start tag of a 
	 * JSON object, throwing a {@link ScanParsingException} otherwise.
	 * @param jsonParser
	 * @throws ScanParsingException
	 */
	public static final void assertStartObject(final JsonParser jsonParser) throws ScanParsingException {
		assertToken(jsonParser, SET_START_OBJECT);
	}
	
	/**
	 * Assert that the given {@link JsonParser} is currently pointing at the start tag of a 
	 * JSON object or array, throwing a {@link ScanParsingException} otherwise.
	 * @param jsonParser
	 * @throws ScanParsingException
	 */
	public static final void assertStartObjectOrArray(final JsonParser jsonParser) throws ScanParsingException {
		assertToken(jsonParser, SET_START_OBJECT_OR_ARRAY);
	}
	
	public static final void assertToken(final JsonParser jsonParser, Set<JsonToken> expectedTokens) throws ScanParsingException {
		if (!expectedTokens.contains(jsonParser.currentToken())) {
			throw new ScanParsingException(String.format("Expected one of "+expectedTokens+" at %s", jsonParser.getTokenLocation()));
		}
	}
}