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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.domain.IResultDependencies;
import com.fortify.ssc.parser.sarif.parser.domain.ReportingDescriptor;
import com.fortify.ssc.parser.sarif.parser.domain.Result;
import com.fortify.ssc.parser.sarif.parser.domain.ResultWithDependencies;
import com.fortify.ssc.parser.sarif.parser.subentity.RunParser.ResultDependencies;
import com.fortify.ssc.parser.sarif.parser.util.Constants;
import com.fortify.ssc.parser.sarif.parser.util.Region;

/**
 * <p>Given an array of result entries, this parser will produce Fortify vulnerabilities.<p>
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
	private final IResultDependencies resultDependencies;
	
	/**
	 * Constructor for setting {@link VulnerabilityHandler} and
	 * {@link ResultDependencies}.
	 * 
	 * @param vulnerabilityHandler
	 * @param resultDependencies
	 */
	public ResultsParser(final VulnerabilityHandler vulnerabilityHandler, final IResultDependencies resultDependencies) {
		this.vulnerabilityHandler = vulnerabilityHandler;
		this.resultDependencies = resultDependencies;
	}
	
	/**
	 * Add a handler for the root array, which will call {@link #produceVulnerabilities(Result)}
	 * for every array entry.
	 * 
	 * new {@link ResultParser} instance.
	 */
	@Override
    protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/", new MapperArrayHandler<>(result->produceVulnerabilities(result), Result.class));
    }
	
	/** 
	 * We expect the region for which this parser is invoked to point at a
	 * JSON array.
	 */
	@Override
	protected void assertParseStart(JsonParser jsonParser) throws ScanParsingException {
		assertStartArray(jsonParser);
	}
	
	private final void produceVulnerabilities(Result orgResult) {
		ResultWithDependencies result = new ResultWithDependencies(orgResult, resultDependencies); 
		Priority priority = result.getLevelOrDefault().getFortifyPriority();
		if ( priority != null ) {
			StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getVulnerabilityUuid());
			vb.setAccuracy(5.0f);
			vb.setAnalyzer("External");
			vb.setCategory(getCategory(result));
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
    		vb.setSubCategory(getSubCategory(result));
    		//vb.setTaintFlag(taintFlag);
    		vb.setVulnerabilityAbstract(result.getResultMessage());
    		//vb.setVulnerabilityRecommendation(vulnerabilityRecommendation);
    		addCustomAttributes(vb);
    		
    		vb.completeVulnerability();
		}
	}
	
	private String getVulnerabilityUuid() {
		// TODO Generate UUID based on correlationGuid, fingerprints or partialFingerPrints properties
		return UUID.randomUUID().toString();
	}
	
	private String getCategory(ResultWithDependencies result) {
		ReportingDescriptor rule = result.getRule();
		return rule==null || rule.getName()==null 
				? Constants.ENGINE_TYPE 
				: StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
	}
	
	private String getSubCategory(ResultWithDependencies result) {
		ReportingDescriptor rule = result.getRule();
		return rule==null || rule.getName()==null 
				? result.getRuleId() 
				: null;
	}

	private void addCustomAttributes(StaticVulnerabilityBuilder vb) {
		// TODO Add custom attributes
		
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