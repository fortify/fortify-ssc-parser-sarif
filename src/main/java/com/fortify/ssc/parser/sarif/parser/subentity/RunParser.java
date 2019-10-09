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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.subentity.RuleParser.Rule;
import com.fortify.ssc.parser.sarif.parser.util.Region;

/**
 * This class will parse an individual run from the runs array using a
 * two-step process:
 * <ol>
 *  <li>Collect all data that may be referenced from the SARIF results array
 *      (which contains the actual vulnerability data), like rules and URI base id's.
 *      Data is stored in a {@link ResultDependencies} instance, using either 
 *      in-memory collections or disk-backed collections, depending on the amount 
 *      of data a given property could potentially contain.<br/>
 *      If the results array is encountered, this step just stores the region 
 *      (start and end position of the results array) so we only have to re-parse
 *      this region of the input file in the second step.</li>
 *      
 *  <li>Once all auxiliary data has been loaded, this parser will create a
 *      new {@link ResultsParser}, passing in the {@link VulnerabilityHandler}
 *      and {@link ResultDependencies}, and then invoke this parser on the
 *      original input file for the previously collected region only.</li>
 * </ol>
 * @author Ruud Senden
 *
 */
public final class RunParser extends AbstractParser {
	private final DB db;
	private Region resultsRegion;
	private final ResultDependencies resultDependencies;
	private final ScanData scanData;
	private final VulnerabilityHandler vulnerabilityHandler;

	/**
	 * This constructor performs the following:
	 * <ol>
	 *  <li>Invoke the super constructor with value 'false' to postpone
	 *      handler initialization</li>
	 *  <li>Instantiate {@link DB} for storing data in disk-backed collections</li>
	 *  <li>Instantiate {@link ResultDependencies} with our {@link DB} instance;
	 *      this object will contain all data collected for use by {@link ResultsParser}</li>
	 *  <li>Invoke {@link #initializeHandlers()} to initialize JSON handlers</li>
	 * </ol>
	 * @param vulnerabilityHandler 
	 * @param scanData 
	 */
    public RunParser(ScanData scanData, VulnerabilityHandler vulnerabilityHandler) {
    	super(false);
		this.scanData = scanData;
		this.vulnerabilityHandler = vulnerabilityHandler;
		this.db = DBMaker.tempFileDB()
				.closeOnJvmShutdown().fileDeleteAfterClose()
				.fileMmapEnableIfSupported()
				.make();
		this.resultDependencies = new ResultDependencies(db);
		initializeHandlers();
	}
    
    /**
     * Add the various handlers for:
     * <ul>
     *  <li>Storing data in the {@link ResultDependencies} instance</li>
     *  <li>Determine the region of the results array</li>
     * </ul>
     */
    @Override
    protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
    	resultDependencies.addHandlers(pathToHandlerMap);
    	pathToHandlerMap.put("/results", jp->resultsRegion=getObjectOrArrayRegion(jp));
    }
    
    /**
     * If we found a results array while parsing the current run, invoke
     * {@link ResultsParser} to generate vulnerabilities based on the contents
     * of the results array. Once finished, we close the {@link DB} instance.
     */
    @Override
    protected <T> T finish() throws ScanParsingException, IOException {
    	if ( resultsRegion != null ) {
    		new ResultsParser(vulnerabilityHandler, resultDependencies).parse(scanData, resultsRegion);
    	}
    	// TODO DB will not be closed if exception is thrown anywhere between
    	//      constructor invocation and the following line (i.e. while parsing
    	//      data into ResultDependencies, or while invoking ResultsParser). 
    	db.close();
    	return null;
    }
    
    public final class ResultDependencies {
    	private final Map<String, Rule> rules;
    	private final Map<String,SARIFFileLocation> originalUriBaseIds;
    	
    	public ResultDependencies(final DB db) {
    		rules = db.hashMap("rules", Serializer.STRING, Rule.SERIALIZER).create();
    		originalUriBaseIds = new LinkedHashMap<>();
		}
    	
    	
    	/**
         * Add the various parser handlers for collecting our data.
         */
        protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
        	pathToHandlerMap.put("/tool/driver/rules", jp->parseArrayEntries(jp, ()->new RuleParser(rules)));
        	pathToHandlerMap.put("/originalUriBaseIds/*", jp->originalUriBaseIds.put(jp.getCurrentName(), objectMapper.readValue(jp, SARIFFileLocation.class)));
        	addPropertyHandlers(pathToHandlerMap, this);
        }

		public Map<String, SARIFFileLocation> getOriginalUriBaseIds() {
			return Collections.unmodifiableMap(originalUriBaseIds);
		}
		
		public Map<String, Rule> getRules() {
			return Collections.unmodifiableMap(rules);
		}
    }
}