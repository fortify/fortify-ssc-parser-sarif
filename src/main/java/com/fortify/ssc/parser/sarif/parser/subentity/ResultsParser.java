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
package com.fortify.ssc.parser.sarif.parser.subentity;

import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.subentity.RunParser.ResultDependencies;
import com.fortify.ssc.parser.sarif.parser.util.Region;

/**
 * <p>Given an array of result entries, this parser will create and invoke a
 * new {@link ResultParser} instance to parse and process individual result
 * entries.<p>
 * 
 * <p>This parser should usually be invoked using the 
 * {@link #parse(ScanData, Region)} method, with the given {@link Region}
 * pointing to the actual results array.</p>
 * 
 * @author Ruud Senden
 *
 */
public final class ResultsParser extends AbstractParser {
	private final VulnerabilityHandler vulnerabilityHandler;
	private final ResultDependencies resultDependencies;
	
	/**
	 * Constructor for setting {@link VulnerabilityHandler} and
	 * {@link ResultDependencies}.
	 * 
	 * @param vulnerabilityHandler
	 * @param resultDependencies
	 */
	public ResultsParser(final VulnerabilityHandler vulnerabilityHandler, final ResultDependencies resultDependencies) {
		this.vulnerabilityHandler = vulnerabilityHandler;
		this.resultDependencies = resultDependencies;
	}
	
	/**
	 * Add a handler for the root array, which will parse each array entry using a 
	 * new {@link ResultParser} instance.
	 */
	@Override
    protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/", jp->parseArrayEntries(jp, ()->new ResultParser(vulnerabilityHandler, resultDependencies)));
    }
	
	/** 
	 * We expect the region for which this parser is invoked to point at a
	 * JSON array.
	 */
	@Override
	protected void assertParseStart(JsonParser jsonParser) throws ScanParsingException {
		assertStartArray(jsonParser);
	}

}