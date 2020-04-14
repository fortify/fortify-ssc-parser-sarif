package com.fortify.ssc.parser.sarif.parser;

import java.io.IOException;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.domain.RunData;
import com.fortify.util.io.Region;
import com.fortify.util.json.ExtendedJsonParser;

/**
 * This class parses a SARIF JSON input document to generate Fortify vulnerabilities.
 * Parsing is done using the following process:
 * <ol>
 *   <li>The main {@link #parse()} method invokes the {@link #parseRun(JsonParser)}
 *       method for each entry in the SARIF <code>runs</code> array.</li>
 *   <li>The {@link #parseRun(JsonParser)} method first collects some auxiliary
 *       data from the current run, like rules and base URI's. This data is then
 *       passed to the {@link #parseResults(RunData)} method, which will parse 
 *       and process the actual vulnerability data.</li>
 *   <li>The {@link #parseResults(RunData)} method re-parses the region of the 
 *       input document that contains the SARIF <code>results</code> array.
 *       For each entry in the <code>results</code> array, the JSON contents 
 *       are mapped to a {@link Result} object, and passed to the  
 *       {@link #produceVulnerability(Result, RunData)} method.</li>
 *   <li>The {@link #produceVulnerability(Result, RunData)} method wraps
 *       the {@link Result} object into a {@link ResultWrapperWithRunData} object,
 *       which provides various utility methods that combine information from
 *       {@link Result} and {@link RunData}. Based on information provided by the
 *       {@link ResultWrapperWithRunData} object, a Fortify vulnerability will
 *       be produced, ignoring any results for which the <code>level</code> property
 *       indicates a non-interesting result.
 * </ol>
 * 
 * @author Ruud Senden
 */
public final class VulnerabilitiesParser {
	private final ScanData scanData;
	private final VulnerabilitiesProducer vulnerabilitiesProducer;
	
	/**
	 * Constructor for storing {@link ScanData} and {@link VulnerabilityHandler}
	 * instances.
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
	 *   <li>Initialize a temporary disk-backed database</li>
	 *   <li>Parse SARIF data into a {@link RunData} object (which stores some 
	 *       of the data in the disk-backed database</li>
	 *   <li>Invoke {@link #parseResults(RunData)} to parse and process the 
	 *       SARIF <code>results</code> array</li>
	 *   <li>Close the temporary database once parsing has completed</li>
	 * @param jsonParser
	 * @throws IOException
	 */
	private final void parseRun(ExtendedJsonParser jsonParser) throws IOException {
		try ( DB db = DBMaker.tempFileDB()
				.closeOnJvmShutdown().fileDeleteAfterClose()
				.fileMmapEnableIfSupported()
				.make() ) {
			RunData runData = RunData.parseRunData(db, jsonParser);
			parseResults(runData);
		}
	}
	
	/**
	 * This method re-parses the SARIF <code>results</code> array, based on the
	 * input document {@link Region} previously collected in the given {@link RunData}
	 * object. For each entry in the <code>results</code> array:
	 * <ol>
	 *  <li>The JSON contents are mapped to a {@link Result} object</li>
	 *  <li>The {@link Result} and {@link RunData} objects are passed to the
	 *      {@link VulnerabilitiesProducer#produceVulnerability(RunData, Result)} method to produce
	 *      the actual Fortify vulnerability (if applicable)</li>
	 * </ol>
	 * @param runData
	 * @throws IOException
	 */
	private final void parseResults(final RunData runData) throws IOException {
		new SarifScanDataStreamingJsonParser()
			.expectedStartTokens(JsonToken.START_ARRAY)
			.handler("/*", Result.class, result->vulnerabilitiesProducer.produceVulnerability(runData, result))
			.parse(scanData, runData.getResultsRegion());
	}
}
