/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
package com.fortify.ssc.parser.sarif.parser;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.domain.RunData;
import com.fortify.util.io.Region;
import com.fortify.util.json.DefaultObjectMapperFactory;
import com.fortify.util.json.ExtendedJsonParser;

/**
 * This class parses a SARIF JSON input document to generate Fortify
 * vulnerabilities.
 * Parsing is done using the following process:
 * <ol>
 * <li>The main {@link #parse()} method invokes the
 * {@link #parseRun(JsonParser)}
 * method for each entry in the SARIF <code>runs</code> array.</li>
 * <li>The {@link #parseRun(JsonParser)} method first collects some auxiliary
 * data from the current run, like rules and base URI's. This data is then
 * passed to the {@link #parseResults(RunData)} method, which will parse
 * and process the actual vulnerability data.</li>
 * <li>The {@link #parseResults(RunData)} method re-parses the region of the
 * input document that contains the SARIF <code>results</code> array.
 * For each entry in the <code>results</code> array, the JSON contents
 * are mapped to a {@link Result} object, and passed to the
 * {@link #produceVulnerability(Result, RunData)} method.</li>
 * <li>The {@link #produceVulnerability(Result, RunData)} method wraps
 * the {@link Result} object into a {@link ResultWrapperWithRunData} object,
 * which provides various utility methods that combine information from
 * {@link Result} and {@link RunData}. Based on information provided by the
 * {@link ResultWrapperWithRunData} object, a Fortify vulnerability will
 * be produced, ignoring any results for which the <code>level</code> property
 * indicates a non-interesting result.
 * </ol>
 * 
 * @author Ruud Senden
 */
public final class VulnerabilitiesParser {
	private static final Logger LOG = LoggerFactory.getLogger(VulnerabilitiesParser.class);
	private final ScanData scanData;
	private final VulnerabilitiesProducer vulnerabilitiesProducer;

	/**
	 * Constructor for storing {@link ScanData} and {@link VulnerabilityHandler}
	 * instances.
	 * 
	 * @param scanData
	 * @param vulnerabilityHandler
	 */
	public VulnerabilitiesParser(final ScanData scanData, final VulnerabilityHandler vulnerabilityHandler) {
		this.scanData = scanData;
		this.vulnerabilitiesProducer = new VulnerabilitiesProducer(vulnerabilityHandler);
	}

	/**
	 * Main method to commence parsing the SARIF document provided by the
	 * configured {@link ScanData}.
	 * 
	 * @throws IOException
	 */
	public final void parse() throws ScanParsingException, IOException {
		new SarifScanDataStreamingJsonParser()
				.handler("/runs/*", this::parseRun)
				.parse(scanData);
	}

	/**
	 * This method parses an individual run from the SARIF <code>runs</code>
	 * array using the following steps:
	 * <ol>
	 * <li>Parse SARIF data into a {@link RunData} object (no MapDB needed)</li>
	 * <li>Invoke {@link #parseResults(RunData)} to parse and process the
	 * SARIF <code>results</code> array</li>
	 * </ol>
	 * 
	 * @param jsonParser
	 * @throws IOException
	 */
	private final void parseRun(ExtendedJsonParser jsonParser) throws IOException {
		// IMPORTANT: Document stream lifetime requirement
		// parseRunData() needs sourceInputStream to stay open for entire run parsing
		// + storage in CachedObject for potential re-parsing on GC
		InputStream sourceInputStream = scanData.getInputStream(name -> name.endsWith(".sarif") || name.endsWith(".json"));
		ObjectMapper objectMapper = DefaultObjectMapperFactory.getDefaultObjectMapper();

		try {
			RunData runData = RunData.parseRunData(jsonParser, sourceInputStream, objectMapper);
			parseResults(runData);
		} catch (IOException e) {
			LOG.error("Failed to parse SARIF run data", e);
			throw e;
		}
	}

	/**
	 * This method re-parses the SARIF <code>results</code> array, based on the
	 * input document {@link Region} previously collected in the given
	 * {@link RunData}
	 * object. For each entry in the <code>results</code> array:
	 * <ol>
	 * <li>The JSON contents are mapped to a {@link Result} object</li>
	 * <li>The {@link Result} and {@link RunData} objects are passed to the
	 * {@link VulnerabilitiesProducer#produceVulnerability(RunData, Result)} method
	 * to produce the actual Fortify vulnerability (if applicable)</li>
	 * </ol>
	 * 
	 * @param runData
	 * @throws IOException
	 */
	private final void parseResults(final RunData runData) throws IOException {
		new SarifScanDataStreamingJsonParser()
				.expectedStartTokens(JsonToken.START_ARRAY)
				.handler("/*", Result.class, result -> vulnerabilitiesProducer.produceVulnerability(runData, result))
				.parse(scanData, runData.getResultsRegion());
	}
}