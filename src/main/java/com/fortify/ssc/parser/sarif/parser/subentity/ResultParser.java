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

import java.net.URI;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.subentity.RuleParser.Rule;
import com.fortify.ssc.parser.sarif.parser.subentity.RunParser.ResultDependencies;
import com.fortify.ssc.parser.sarif.parser.util.Constants;

/**
 * This class will parse individual result entries from a
 * /runs/results array. Parsed data will be temporarily stored 
 * in the current parser instance. Once the full result entry
 * has been parsed, the {@link #finish()} method will generate
 * a Fortify vulnerability if applicable for the current result
 * entry (i.e. result level is not 'pass' or 'notApplicable'). 
 * 
 * @author Ruud Senden
 *
 */
public final class ResultParser extends AbstractParser {
	private final Logger LOG = LoggerFactory.getLogger(ResultParser.class);
	private final VulnerabilityHandler vulnerabilityHandler;
	private final ResultDependencies resultDependencies;
	
	@JsonProperty private FileLocation analysisTarget;
	@JsonProperty private String ruleId;
	@JsonProperty private String ruleMessageId;
	@JsonProperty private SARIFLevel level;
	@JsonProperty private String locations_physicalLocation_region_startLine;
	
	/**
	 * Constructor for setting {@link VulnerabilityHandler} and
	 * {@link ResultDependencies}.
	 * 
	 * @param vulnerabilityHandler
	 * @param resultDependencies
	 */
	public ResultParser(VulnerabilityHandler vulnerabilityHandler, ResultDependencies resultDependencies) {
		this.vulnerabilityHandler = vulnerabilityHandler;
		this.resultDependencies = resultDependencies;
	}

	@Override
	protected <T> T finish() {
		Rule rule = getRule();
		Priority priority = getPriority(rule);
		if ( priority != null ) {
			StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getVulnerabilityUuid());
			vb.setAccuracy(5.0f);
			vb.setAnalyzer("External");
			vb.setCategory(getCategory(rule));
			vb.setClassName(null);
			vb.setConfidence(2.5f);
    		vb.setEngineType(Constants.ENGINE_TYPE);
    		vb.setFileName(getFileName());
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
    		vb.setSubCategory(getSubCategory(rule));
    		//vb.setTaintFlag(taintFlag);
    		//vb.setVulnerabilityAbstract(vulnerabilityAbstract);
    		//vb.setVulnerabilityRecommendation(vulnerabilityRecommendation);
    		addCustomAttributes(vb);
    		
    		vb.completeVulnerability();
		}
		return null;
	}

	private Rule getRule() {
		return ruleId==null ? null : resultDependencies.getRules().get(ruleId);
	}

	private Priority getPriority(Rule rule) {
		Priority result = null;
		if ( level == null && rule!=null ) {
			level = rule.getConfiguration().getDefaultLevel();
		}
		if ( level == null ) {
			LOG.error("Level for vulnerability cannot be determined; ignoring vulnerability");
		} else {
			result = level.getFortifyPriority();
		}
		return result;
	}
	
	private String getVulnerabilityUuid() {
		// TODO Generate UUID based on correlationGuid, fingerprints or partialFingerPrints properties
		return UUID.randomUUID().toString();
	}
	
	private String getCategory(Rule rule) {
		return rule==null || rule.getName()==null 
				? Constants.ENGINE_TYPE 
				: StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
	}
	
	private String getSubCategory(Rule rule) {
		return rule==null || rule.getName()==null ? ruleId : null;
	}
	
	private String getFileName() {
		// TODO If analysisTarget not defined, get file name from locations[]
		return analysisTarget==null?null:analysisTarget.getFullFileName(resultDependencies);
	}
	
	private void addCustomAttributes(StaticVulnerabilityBuilder vb) {
		// TODO Add custom attributes
		
	}
	
	private static final class FileLocation {
		@JsonProperty private URI uri;
		@JsonProperty private String uriBaseId;
		public String getFullFileName(ResultDependencies resultDependencies) {
			URI resultUri = this.uri;
			// TODO Lookup uriBaseId in resultDependencies.getOriginalUriBaseIds()
			//      and append uri to corresponding original base URI
			// TODO Convert URI to file name
			if ( uriBaseId!=null ) {
				URI baseUri = resultDependencies.getOriginalUriBaseIds().get(uriBaseId);
				resultUri = baseUri.resolve(resultUri);
			}
			return resultUri.getPath(); 
		}
	}

	
	/**
	 * This enum defines the custom vulnerability attributes that are generated by the
	 * SARIF parser plugin. 
	 * 
	 * TODO Update custom attributes
	 * 
	 * @author Ruud Senden
	 *
	 */
	public static enum CustomVulnAttribute implements com.fortify.plugin.spi.VulnerabilityAttribute {

		custom1(AttrType.STRING),
		custom2(AttrType.DECIMAL),
	    ;

	    private final AttrType attributeType;

	    CustomVulnAttribute(final AttrType attributeType) {
	        this.attributeType = attributeType;
	    }

	    @Override
	    public String attributeName() {
	        return name();
	    }

	    @Override
	    public AttrType attributeType() {
	        return attributeType;
	    }
	}
	
}