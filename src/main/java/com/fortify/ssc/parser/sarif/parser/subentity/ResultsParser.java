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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.domain.ArtifactLocation;
import com.fortify.ssc.parser.sarif.parser.domain.Level;
import com.fortify.ssc.parser.sarif.parser.domain.Location;
import com.fortify.ssc.parser.sarif.parser.domain.ReportingDescriptor;
import com.fortify.ssc.parser.sarif.parser.domain.Result;
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
	
	private final void produceVulnerabilities(Result result) {
		new VulnerabilityProducer(vulnerabilityHandler, resultDependencies, result).produceVulnerability();
	}
	
	private static final class VulnerabilityProducer {
		private final Logger LOG = LoggerFactory.getLogger(VulnerabilityProducer.class);
		private final VulnerabilityHandler vulnerabilityHandler;
		private final ResultDependencies resultDependencies;
		private final Result result;
		public VulnerabilityProducer(VulnerabilityHandler vulnerabilityHandler, ResultDependencies resultDependencies, Result result) {
			this.vulnerabilityHandler = vulnerabilityHandler;
			this.resultDependencies = resultDependencies;
			this.result = result;
		}
		
		public final void produceVulnerability() {
			ReportingDescriptor rule = getRule();
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
		}
	
		private ReportingDescriptor getRule() {
			String ruleId = result.getRuleId();
			return ruleId==null ? null : resultDependencies.getRules().get(ruleId);
		}
	
		private Priority getPriority(ReportingDescriptor rule) {
			Priority priority = null;
			Level level = result.getLevel();
			if ( level == null && rule!=null ) {
				level = rule.getDefaultConfiguration().getLevel();
			}
			if ( level == null ) {
				LOG.error("Level for vulnerability cannot be determined; ignoring vulnerability");
			} else {
				priority = level.getFortifyPriority();
			}
			return priority;
		}
		
		private String getVulnerabilityUuid() {
			// TODO Generate UUID based on correlationGuid, fingerprints or partialFingerPrints properties
			return UUID.randomUUID().toString();
		}
		
		private String getCategory(ReportingDescriptor rule) {
			return rule==null || rule.getName()==null 
					? Constants.ENGINE_TYPE 
					: StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
		}
		
		private String getSubCategory(ReportingDescriptor rule) {
			return rule==null || rule.getName()==null ? result.getRuleId() : null;
		}
		
		private String getFileName() {
			String fileName = "Unknown";
			Map<String, ArtifactLocation> originalUriBaseIds = resultDependencies.getOriginalUriBaseIds();
			Location[] locations = result.getLocations();
			if ( locations!=null && locations.length>0 && locations[0].getPhysicalLocation()!=null ) {
				fileName = locations[0].getPhysicalLocation().getArtifactLocation().getFullFileName(originalUriBaseIds);
			} else if ( result.getAnalysisTarget()!=null ) {
				fileName = result.getAnalysisTarget().getFullFileName(originalUriBaseIds);
			}
			return fileName;
		}
		
		private void addCustomAttributes(StaticVulnerabilityBuilder vb) {
			// TODO Add custom attributes
			
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