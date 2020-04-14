package com.fortify.ssc.parser.sarif.parser;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.CustomVulnAttribute;
import com.fortify.ssc.parser.sarif.domain.ReportingDescriptor;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.domain.RunData;
import com.fortify.util.ssc.parser.HandleDuplicateIdVulnerabilityHandler;

public final class VulnerabilitiesProducer {
	private static final Logger LOG = LoggerFactory.getLogger(VulnerabilitiesProducer.class);
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
	@SuppressWarnings("deprecation") // SSC JavaDoc states that severity is mandatory, but method is deprecated
	public final void produceVulnerability(RunData runData, Result result) {
		Priority priority = getPriority(runData, result);
		if ( priority != null ) {
			StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getInstanceId(runData, result));
			
			// Set meta-data
			vb.setEngineType(getEngineType(runData, result));
			vb.setKingdom(getKingdom(runData, result));
			vb.setAnalyzer(getAnalyzer(runData, result));
			vb.setCategory(getCategory(runData, result));
			vb.setSubCategory(getSubCategory(runData, result));
			
			// Set mandatory values to JavaDoc-recommended values
			vb.setAccuracy(getAccuracy(runData, result));
			vb.setSeverity(getSeverity(runData, result));
			vb.setConfidence(getConfidence(runData, result));
			vb.setProbability(getProbability(runData, result));
			vb.setImpact(getImpact(runData, result));
			vb.setLikelihood(getLikelihood(runData, result));
			
			// Set standard vulnerability fields based on input
			vb.setFileName(getFileName(runData, result));
			vb.setPriority(priority);
			vb.setRuleGuid(getRuleGuid(runData, result));
			vb.setVulnerabilityAbstract(getVulnerabilityAbstract(runData, result));
			
			//vb.setClassName(null);
    		//vb.setFunctionName(functionName);
    		//vb.setLineNumber(lineNumber);
    		//vb.setMappedCategory(mappedCategory);
    		//vb.setMinVirtualCallConfidence(minVirtualCallConfidence);
    		//vb.setPackageName(packageName);
    		//vb.setRemediationConstant(remediationConstant);
    		//vb.setRuleGuid(ruleGuid);
    		//vb.setSink(sink);
    		//vb.setSinkContext(sinkContext);
    		//vb.setSource(source);
    		//vb.setSourceContext(sourceContext);
    		//vb.setSourceFile(sourceFile);
    		//vb.setSourceLine(sourceLine);
    		//vb.setTaintFlag(taintFlag);
    		//vb.setVulnerabilityRecommendation(vulnerabilityRecommendation);
			
			//vb.set*CustomAttributeValue(...)
			
			vb.setStringCustomAttributeValue(CustomVulnAttribute.categoryAndSubCategory, getCategoryAndSubCategory(runData, result));
			vb.setStringCustomAttributeValue(CustomVulnAttribute.toolName, runData.getToolName());
    		
    		vb.completeVulnerability();
		}
	}

	private String getVulnerabilityAbstract(RunData runData, Result result) {
		return result.getResultMessage(runData);
	}

	private String getFileName(RunData runData, Result result) {
		return result.resolveFullFileName(runData, "Unknown");
	}

	private String getInstanceId(RunData runData, Result result) {
		return DigestUtils.sha256Hex(getInstanceIdString(runData, result));
	}
	
	private String getInstanceIdString(RunData runData, Result result) {
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
			getFileName(runData, result),
			result.resolveRuleId(runData),
			partialFingerPrints,
			getVulnerabilityAbstract(runData, result));
	}
	
	private String getEngineType(RunData runData, Result result) {
		return runData.getEngineType();
	}
	
	private String getKingdom(RunData runData, Result result) {
		String kingdom = getStringProperty(result.getProperties(), "kingdom", null);
		if ( StringUtils.isBlank(kingdom) ) {
			kingdom = getStringProperty(getRuleProperties(runData, result), "Kingdom", null);
		}
		return kingdom;
	}
	
	private String getCategory(RunData runData, Result result) {
		String category = null;
		ReportingDescriptor rule = result.resolveRule(runData);
		if ( rule != null && StringUtils.isNotBlank(rule.getName()) ) {
			category = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
		}
		if ( StringUtils.isBlank(category) ) {
			category = getStringProperty(getRuleProperties(rule), "Type", null);
		}
		if ( StringUtils.isBlank(category) ) {
			category = result.resolveRuleId(runData);
		}
		if ( StringUtils.isBlank(category) ) {
			category = getEngineType(runData, result);
		}
		return category;
	}
	
	private String getSubCategory(RunData runData, Result result) {
		return getStringProperty(getRuleProperties(runData, result), "Subtype", null);
	}
	
	private String getAnalyzer(RunData runData, Result result) {
		return "External";
	}

	private float getAccuracy(RunData runData, Result result) {
		return getFloatProperty(getRuleProperties(runData, result), "Accuracy", 5.0f);
	}
	
	private float getSeverity(RunData runData, Result result) {
		return getFloatProperty(result.getProperties(), "InstanceSeverity", 2.5f);
	}
	
	private float getConfidence(RunData runData, Result result) {
		return getFloatProperty(result.getProperties(), "Confidence", 2.5f);
	}
	
	private float getProbability(RunData runData, Result result) {
		return getFloatProperty(getRuleProperties(runData, result), "Probability", 2.5f);
	}
	
	private float getImpact(RunData runData, Result result) {
		return getFloatProperty(getRuleProperties(runData, result), "Impact", 2.5f);
	}
	
	private float getLikelihood(RunData runData, Result result) {
		return 2.5f;
	}

	private Priority getPriority(RunData runData, Result result) {
		String priorityString = null;
		if ( isConvertedFromFortifyXml(runData) ) {
			priorityString = getStringProperty(result.getProperties(), "priority", null);
		}
		return StringUtils.isNotBlank(priorityString) 
				? Priority.valueOf(priorityString)
				: result.resolveLevel(runData).getFortifyPriority();
	}
	
	private String getRuleGuid(RunData runData, Result result) {
		if ( isConvertedFromFortifyXml(runData) ) {
			return getStringProperty(result.getProperties(), "fortifyRuleId", null);
		} else if ( isConvertedFromFortifyFpr(runData) ) {
			return result.resolveRuleGuid(runData);
		} else {
			return null;
		}
	}
	
	private String getCategoryAndSubCategory(RunData runData, Result result) {
		String category = getCategory(runData, result);
		String subCategory = getSubCategory(runData, result);
		return StringUtils.isBlank(subCategory) ? category : String.join(": ", category, subCategory);
	}
	
	private float getFloatProperty(Map<String, Object> properties, String key, float defaultValue) {
		String valueString = getStringProperty(properties, key, null);
		if ( StringUtils.isNotBlank(valueString) ) {
			try {
				return new Float(valueString);
			} catch (NumberFormatException nfe) {
				LOG.warn("Error converting {} string '{}' to float: {}", key, valueString, nfe.getMessage());
			}
		}
		return defaultValue;
	}
	
	private String getStringProperty(Map<String, Object> properties, String key, String defaultValue) {
		if ( properties!=null && properties.containsKey(key) ) {
			return properties.get(key).toString();
		}
		return defaultValue;
	}
	
	private Map<String, Object> getRuleProperties(ReportingDescriptor rule) {
		return rule==null ? null : rule.getProperties();
	}
	
	private Map<String, Object> getRuleProperties(RunData runData, Result result) {
		return getRuleProperties(result.resolveRule(runData));
	}
	
	private boolean isConvertedFromFortifyFpr(RunData runData) {
		return "Micro Focus Fortify Static Code Analyzer".equalsIgnoreCase(runData.getToolName());
	}

	private boolean isConvertedFromFortifyXml(RunData runData) {
		return "Fortify".equalsIgnoreCase(runData.getToolName());
	}

}
