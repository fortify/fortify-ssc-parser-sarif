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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.subentity.RuleParser.Rule;
import com.fortify.ssc.parser.sarif.parser.subentity.RunParser.ResultDependencies;

public final class ResultsParser extends AbstractParser {
	private final VulnerabilityHandler vulnerabilityHandler;
	private final ResultDependencies resultDependencies;
	
	public ResultsParser(final VulnerabilityHandler vulnerabilityHandler, final ResultDependencies resultDependencies) {
		this.vulnerabilityHandler = vulnerabilityHandler;
		this.resultDependencies = resultDependencies;
	}
	
	@Override
    protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/", jp->parseArrayEntries(jp, ()->new ResultParser()));
    }
	
	/** 
	 * We expect the region for which this parser is invoked to point at a
	 * JSON array.
	 */
	@Override
	protected void assertParseStart(JsonParser jsonParser) throws ScanParsingException {
		assertStartArray(jsonParser);
	}
	
	private class ResultParser extends AbstractParser {
		private final Logger LOG = LoggerFactory.getLogger(ResultParser.class);
		@JsonProperty private String analysisTarget_uri;
		@JsonProperty private String ruleId;
		@JsonProperty private String ruleMessageId;
		@JsonProperty private SARIFLevel level;
		@JsonProperty private String locations_physicalLocation_region_startLine;
		
		@Override
		protected <T> T finish() {
			Priority priority = getPriority();
			if ( priority != null ) {
				StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(UUID.randomUUID().toString());
	    		vb.setEngineType("SARIF"); // TODO Get this dynamically from plugin.xml
	    		vb.setPriority(priority);
	    		//vb.setKingdom(FortifyKingdom.ENVIRONMENT.getKingdomName());
	    		//vb.setAnalyzer(FortifyAnalyser.CONFIGURATION.getAnalyserName());
	    		vb.setCategory(ruleId);
	    		//vb.setSubCategory(name);
	    		
	    		// Set mandatory values to JavaDoc-recommended values
	    		vb.setAccuracy(5.0f);
	    		vb.setConfidence(2.5f);
	    		vb.setLikelihood(2.5f);
	    		vb.setImpact(2.5f);
	    		vb.setProbability(2.5f);
	    		
	    		vb.setFileName(analysisTarget_uri);
	    		vb.setVulnerabilityAbstract(ruleMessageId);
	    		
	    		vb.completeVulnerability();
			}
			return null;
		}

		private Priority getPriority() {
			Priority result = null;
			if ( level == null && ruleId!=null ) {
				Rule rule = resultDependencies.getRules().get(ruleId);
				if ( rule != null ) {
					level = rule.getConfiguration().getDefaultLevel();
				}
			}
			if ( level == null ) {
				LOG.error("Level for vulnerability cannot be determined; ignoring vulnerability");
			} else {
				result = level.getFortifyPriority();
			}
			return result;
		}
		
	}

}