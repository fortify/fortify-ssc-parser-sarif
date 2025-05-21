package com.fortify.ssc.parser.sarif.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.CustomVulnAttribute;
import com.fortify.ssc.parser.sarif.domain.Kind;
import com.fortify.ssc.parser.sarif.domain.ReportingDescriptor;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.domain.RunData;
import com.fortify.util.ssc.parser.EngineTypeHelper;
import com.fortify.util.ssc.parser.HandleDuplicateIdVulnerabilityHandler;

public final class VulnerabilitiesProducer {
	private static final Logger LOG = LoggerFactory.getLogger(VulnerabilitiesProducer.class);
	private static final String ENGINE_TYPE = EngineTypeHelper.getEngineType();
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
		Kind kind = result.getKind();
		if ( kind == null ) {
			// SARIF specification says that if kind is not specified, then the default value of fail is to be used
			kind = Kind.fail;
		}
		switch(kind) {
			case review:
			case open:
			case fail:
				break;
			case informational:
			case notApplicable:
			case pass:
				// results with these kind values are not vulnerabilities.
				return;
		}
		Priority priority = getPriority(runData, result);
		if ( priority != null ) {
			StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getInstanceId(runData, result));
			
			// Set meta-data
			vb.setEngineType(ENGINE_TYPE);
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
    		vb.setLineNumber(result.resolveLineNumber());
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
			vb.setStringCustomAttributeValue(CustomVulnAttribute.help, getHelp(runData, result));
			vb.setStringCustomAttributeValue(CustomVulnAttribute.helpUri, getHelpUri(runData, result));
			vb.setStringCustomAttributeValue(CustomVulnAttribute.tags, getTags(runData, result));
			vb.setStringCustomAttributeValue(CustomVulnAttribute.snippet, result.resolveSnippet());
    		
    		vb.completeVulnerability();
		}
	}

	private String getVulnerabilityAbstract(RunData runData, Result result) {
		return result.getResultMessage(runData);
	}

	private String getHelp(RunData runData, Result result) {
		String help = null;
		ReportingDescriptor rule = result.resolveRule(runData);
		if ( rule != null && rule.getHelp() != null ) {
			help = rule.getHelp().getText();
		}
		return StringUtils.isBlank(help) ? "Not Available" : help;
	}

	private String getHelpUri(RunData runData, Result result) {
		String helpUri = null;
		ReportingDescriptor rule = result.resolveRule(runData);
		if ( rule != null && rule.getHelpUri() != null ) {
			helpUri = rule.getHelpUri().toString();
		}
		return StringUtils.isBlank(helpUri) ? "Not Available" : helpUri;
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
		if ( rule != null ) {
			if ( rule.getShortDescription() != null ) {
				category = result.resolveMessage(rule.getShortDescription(), runData);
			}
			if ( StringUtils.isBlank(category) && StringUtils.isNotBlank(rule.getName()) ) {
				if ( rule.getName().contains(StringUtils.SPACE)) {
					category = rule.getName();
				}else {
					category = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
				}
			}
			if ( StringUtils.isBlank(category) ) {
				category = getStringProperty(getRuleProperties(rule), "Type", null);
			}
		}
		if ( StringUtils.isBlank(category) ) {
			category = result.resolveRuleId(runData);
		}
		if ( StringUtils.isBlank(category) ) {
			category = runData.getToolName();
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
		if ( isConvertedFromFortifyXml(runData) ) {
			String priorityString = getStringProperty(result.getProperties(), "priority", null);
			if ( StringUtils.isNotBlank(priorityString) ) {
				return Priority.valueOf(priorityString);
			}
		}
		String securitySeverityString = getStringProperty(getRuleProperties(runData, result), "security-severity", null);
		if ( StringUtils.isNotBlank(securitySeverityString) ) {
			try {
				float securitySeverity = Float.parseFloat(securitySeverityString);
				// CVSS score range mapping from https://nvd.nist.gov/vuln-metrics/cvss
				if ( securitySeverity < 0 ){
					LOG.warn("Invalid security-severity, {} is less than 0.", securitySeverity);
				}else if ( securitySeverity < 4 ){
					return Priority.Low;
				}else if ( securitySeverity < 7 ){
					return Priority.Medium;
				}else if ( securitySeverity < 9 ){
					return Priority.High;
				}else if ( securitySeverity <= 10 ){
					return Priority.Critical;
				}else{
					LOG.warn("Invalid security-severity, {} is greater than 10.", securitySeverity);
				}
			} catch (NumberFormatException nfe) {
				LOG.warn("Error converting {} string '{}' to float: {}", "security-severity", securitySeverityString, nfe.getMessage());
			}
		}
		return result.resolveLevel(runData).getFortifyPriority();
	}

	private String getTags(RunData runData, Result result) {
		return getStringListProperty(getRuleProperties(runData, result), "tags", Collections.emptyList())
			.stream()
			// the tag "security" is almost always present for many SARIF reports because GitHub Code Scanning requires that tag be present for findings to appear
			// See https://docs.github.com/en/code-security/code-scanning/integrating-with-code-scanning/sarif-support-for-code-scanning
			// Since it's not really useful, filter it out.
			.filter(s -> ! "security".equalsIgnoreCase(s))
			.collect(Collectors.joining(", "));
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
				return Float.parseFloat(valueString);
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
	
	private List<String> getStringListProperty(Map<String, Object> properties, String key, List<String> defaultValue) {
		if ( properties!=null && properties.containsKey(key) && properties.get(key) instanceof List ) {
			return (List<String>) properties.get(key);
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
