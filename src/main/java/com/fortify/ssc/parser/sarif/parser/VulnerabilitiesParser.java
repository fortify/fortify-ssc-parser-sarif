package com.fortify.ssc.parser.sarif.parser;

import java.io.IOException;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.parser.util.Constants;
import com.fortify.ssc.parser.sarif.parser.util.ResultWrapperWithRunData;
import com.fortify.ssc.parser.sarif.parser.util.RunData;
import com.fortify.util.io.Region;
import com.fortify.util.json.handler.JsonArrayHandler;
import com.fortify.util.json.handler.JsonArrayMapperHandler;
import com.fortify.util.ssc.parser.ScanDataStreamingJsonParser;
import com.fortify.util.ssc.parser.VulnerabilityBuilder;
import com.fortify.util.ssc.parser.VulnerabilityBuilder.CustomStaticVulnerabilityBuilder;

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
	private final VulnerabilityBuilder vulnerabilityBuilder;
	
	/**
	 * Constructor for storing {@link ScanData} and {@link VulnerabilityBuilder}
	 * instances.
	 * @param scanData
	 * @param vulnerabilityBuilder
	 */
	public VulnerabilitiesParser(final ScanData scanData, final VulnerabilityBuilder vulnerabilityBuilder) {
		this.vulnerabilityBuilder = vulnerabilityBuilder;
		this.scanData = scanData;
	}
	
	/**
	 * Main method to commence parsing the SARIF document provided by the
	 * configured {@link ScanData}.
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public final void parse() throws ScanParsingException, IOException {
		new ScanDataStreamingJsonParser()
			.handler("/runs", new JsonArrayHandler(jp->parseRun(jp)))
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
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	private final void parseRun(JsonParser jsonParser) throws ScanParsingException, IOException {
		try ( DB db = DBMaker.tempFileDB()
				.closeOnJvmShutdown().fileDeleteAfterClose()
				.fileMmapEnableIfSupported()
				.make() ) {
			parseResults(RunData.parseRunData(db, jsonParser));
		}
	}
	
	/**
	 * This method re-parses the SARIF <code>results</code> array, based on the
	 * input document {@link Region} previously collected in the given {@link RunData}
	 * object. For each entry in the <code>results</code> array:
	 * <ol>
	 *  <li>The JSON contents are mapped to a {@link Result} object</li>
	 *  <li>The {@link Result} and {@link RunData} objects are wrapped into
	 *      a new {@link ResultWrapperWithRunData} instance</li>
	 *  <li>The {@link ResultWrapperWithRunData} instance is passed to the
	 *      {@link #produceVulnerability(ResultWrapperWithRunData)} method
	 *      to produce the actual Fortify vulnerability (if applicable)</li>
	 * </ol>
	 * @param runData
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	private final void parseResults(final RunData runData) throws ScanParsingException, IOException {
		new ScanDataStreamingJsonParser()
			.expectedStartTokens(JsonToken.START_ARRAY)
			.handler("/", new JsonArrayMapperHandler<>(result->produceVulnerability(new ResultWrapperWithRunData(result, runData)), Result.class))
			.parse(scanData, runData.getResultsRegion());
	}
	
	/**
	 * This method produces a Fortify vulnerability based on the given
	 * {@link ResultWrapperWithRunData} instance. No vulnerability will be produced 
	 * if {@link ResultWrapperWithRunData#getLevelOrDefault()} returns a level that
	 * indicates that the result is not interesting from a Fortify perspective.
	 * @param result
	 */
	private final void produceVulnerability(ResultWrapperWithRunData result) {
		Priority priority = result.getLevelOrDefault().getFortifyPriority();
		if ( priority != null ) {
			CustomStaticVulnerabilityBuilder vb = vulnerabilityBuilder.startStaticVulnerability();
			vb.setInstanceId(result.getVulnerabilityId());
			vb.setAccuracy(5.0f);
			vb.setAnalyzer("External");
			vb.setCategory(result.getCategory());
			vb.setClassName(null);
			vb.setConfidence(2.5f);
    		vb.setEngineType(Constants.ENGINE_TYPE);
    		vb.setFileName(result.getFullFileName("Unknown"));
    		//vb.setFunctionName(functionName);
    		vb.setImpact(2.5f);
    		//vb.setKingdom(kingdom);
    		vb.setLikelihood(2.5f);
    		//vb.setLineNumber(lineNumber);
    		//vb.setMappedCategory(mappedCategory);
    		//vb.setMinVirtualCallConfidence(minVirtualCallConfidence);
    		//vb.setPackageName(packageName);
    		vb.setPriority(priority);
    		vb.setProbability(2.5f);
    		//vb.setRemediationConstant(remediationConstant);
    		//vb.setRuleGuid(ruleGuid);
    		//vb.setSink(sink);
    		//vb.setSinkContext(sinkContext);
    		//vb.setSource(source);
    		//vb.setSourceContext(sourceContext);
    		//vb.setSourceFile(sourceFile);
    		//vb.setSourceLine(sourceLine);
    		vb.setSubCategory(result.getSubCategory());
    		//vb.setTaintFlag(taintFlag);
    		vb.setVulnerabilityAbstract(result.getResultMessage());
    		//vb.setVulnerabilityRecommendation(vulnerabilityRecommendation);
    		addCustomAttributes(vb, result);
    		
    		vb.completeVulnerability();
		}
	}

	private void addCustomAttributes(StaticVulnerabilityBuilder vb, ResultWrapperWithRunData result) {
		// TODO Add custom attributes
		
	}
}
