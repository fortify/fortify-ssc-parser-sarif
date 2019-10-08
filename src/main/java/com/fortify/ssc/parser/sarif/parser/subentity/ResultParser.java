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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;

public final class ResultParser extends AbstractParser {
	private static enum SARIFLevel {
		// TODO add mapping to Fortify priority
		warning, error, open, note
	}
	
	private final List<Result> results;
	private final Result result;

	public ResultParser(List<Result> results) {
		super(false);
		this.results = results;
		this.result = new Result();
		initializeHandlers();
	}
	
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		addPropertyHandlers(pathToHandlerMap, result);
	}
	
	@Override
	protected <T> T finish() {
		System.err.println(new ReflectionToStringBuilder(result).build());
		results.add(result);
		return null;
	}
	
	public static final class Result implements Serializable {
		private static final long serialVersionUID = 1L;
		
		@JsonProperty private String analysisTarget_uri;
		@JsonProperty private String ruleId;
		@JsonProperty private String ruleMessageId;
		@JsonProperty private String level;
		@JsonProperty private String locations_physicalLocation_region_startLine;
		
		public void buildVulnerability(VulnerabilityHandler vulnerabilityHandler) {
    		StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(UUID.randomUUID().toString());
    		vb.setEngineType("SARIF"); // TODO Get this dynamically from plugin.xml
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
    		
    		try {
    			vb.setPriority(Priority.valueOf(StringUtils.capitalize(level)));
    		} catch ( NullPointerException | IllegalArgumentException e ) {
    			vb.setPriority(Priority.Medium);
    		}
    		
    		vb.completeVulnerability();
		}
	}
}