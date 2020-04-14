package com.fortify.ssc.parser.sarif.parser;

import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.domain.ReportingDescriptor;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.domain.RunData;
import com.fortify.util.ssc.parser.HandleDuplicateIdVulnerabilityHandler;

public final class VulnerabilitiesProducer {
	private final VulnerabilityHandler vulnerabilityHandler;
	
	/**
	 * Constructor for storing {@link VulnerabilityHandler} instance.
	  * @param vulnerabilityHandler
	 */
	public VulnerabilitiesProducer(final VulnerabilityHandler vulnerabilityHandler) {
		this.vulnerabilityHandler = new HandleDuplicateIdVulnerabilityHandler(vulnerabilityHandler);
	}
	
	/**
	 * This method produces a Fortify vulnerability based on the given
	 * {@link ResultWrapperWithRunData} instance. No vulnerability will be produced 
	 * if {@link ResultWrapperWithRunData#resolveLevel()} returns a level that
	 * indicates that the result is not interesting from a Fortify perspective.
	 * @param result
	 */
	public final void produceVulnerability(RunData runData, Result result) {
		Priority priority = getPriority(runData, result);
		if ( priority != null ) {
			StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getInstanceId(runData, result));
			vb.setAccuracy(5.0f);
			vb.setAnalyzer("External");
			vb.setCategory(getCategory(runData, result));
			vb.setClassName(null);
			vb.setConfidence(2.5f);
			vb.setEngineType(runData.getEngineType());
    		vb.setFileName(result.resolveFullFileName(runData, "Unknown"));
    		//vb.setFunctionName(functionName);
    		vb.setImpact(2.5f);
    		vb.setKingdom(getKingdom(runData, result));
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
    		vb.setSubCategory(getSubCategory());
    		//vb.setTaintFlag(taintFlag);
    		vb.setVulnerabilityAbstract(result.getResultMessage(runData));
    		//vb.setVulnerabilityRecommendation(vulnerabilityRecommendation);
    		addCustomAttributes(vb, runData, result);
    		
    		vb.completeVulnerability();
		}
	}

	private void addCustomAttributes(StaticVulnerabilityBuilder vb, RunData runData, Result result) {
		// TODO Add custom attributes
		
	}
	
	private String getInstanceId(RunData runData, Result result) {
		return DigestUtils.sha256Hex(getInstanceIdString(runData, result));
	}
	
	public String getInstanceIdString(RunData runData, Result result) {
		if ( StringUtils.isNotBlank(result.getGuid()) ) {
			return result.getGuid();
		} else if ( StringUtils.isNotBlank(result.getCorrelationGuid()) ) {
			return result.getCorrelationGuid();
		} else if ( result.getFingerprints()!=null && result.getFingerprints().size()>0 ) {
			return new TreeMap<>(result.getFingerprints()).toString();
		} else {
			return generateInstanceIdString(runData, result);
		}
	}
	
	// As described at https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317932
	// we calculate a unique id string based on tool name, full file location,
	// rule id and partial finger prints if available. To increase chances
	// of generating a unique id, we also include the result message.
	// However, this could potentially still result in duplicate id strings. 
	// Possibly we could add information from other properties like region, 
	// logical location or code flows, but these may either not be available, or 
	// still result in duplicate uuid strings.
	private String generateInstanceIdString(RunData runData, Result result) {
		String partialFingerPrints = result.getPartialFingerprints()==null?"":new TreeMap<>(result.getPartialFingerprints()).toString();
		return String.join("|", 
			runData.getToolName(),
			result.resolveFullFileName(runData, "Unknown"),
			result.resolveRuleId(runData),
			partialFingerPrints,
			result.getResultMessage(runData));
	}
	
	public String getCategory(RunData runData, Result result) {
		ReportingDescriptor rule = result.resolveRule(runData);
		if ( rule != null ) {
			if ( StringUtils.isNotBlank(rule.getName()) ) {
				return StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
			} else if ( rule.getProperties()!=null && rule.getProperties().containsKey("Type") ) {
				return rule.getProperties().get("Type").toString();
			}
		} else {
			String ruleId = result.resolveRuleId(runData);
			if ( StringUtils.isNotBlank(ruleId) ) {
				return ruleId;
			} 
		}
		return runData.getEngineType();
	}
	
	public String getSubCategory() {
		return null;
	}

	public Priority getPriority(RunData runData, Result result) {
		if ( "Fortify".equalsIgnoreCase(runData.getToolName()) && result.getProperties()!=null && result.getProperties().containsKey("priority")) {
			return Priority.valueOf(result.getProperties().get("priority").toString());
		} else {
			return result.resolveLevel(runData).getFortifyPriority();
		}
	}
	
	public String getKingdom(RunData runData, Result result) {
		if ( result.getProperties()!=null && result.getProperties().containsKey("kingdom") ) {
			return result.getProperties().get("kingdom").toString();
		} else {
			ReportingDescriptor rule = result.resolveRule(runData);
			if ( rule!=null && rule.getProperties()!=null && rule.getProperties().containsKey("Kingdom") ) {
				return rule.getProperties().get("Kingdom").toString();
			}
		}
		return null;
	}
}
