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
package com.fortify.ssc.parser.sarif.parser.util;

import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.fortify.ssc.parser.sarif.domain.ArtifactLocation;
import com.fortify.ssc.parser.sarif.domain.Level;
import com.fortify.ssc.parser.sarif.domain.Location;
import com.fortify.ssc.parser.sarif.domain.Message;
import com.fortify.ssc.parser.sarif.domain.MultiformatMessageString;
import com.fortify.ssc.parser.sarif.domain.ReportingDescriptor;
import com.fortify.ssc.parser.sarif.domain.Result;
import com.fortify.ssc.parser.sarif.domain.Result.Kind;

import lombok.experimental.Delegate;

/**
 * This {@link Result} wrapper class provides access to the configured
 * {@link Result} methods, but adds various utility methods that 
 * utilize information provided by the configured {@link RunData}
 * object.
 * 
 * @author Ruud Senden
 *
 */
public class ResultWrapperWithRunData {
	@Delegate private final Result result;
	private final RunData runData;
	private volatile ReportingDescriptor rule;
	
	public ResultWrapperWithRunData(Result result, RunData runData) {
		this.result = result;
		this.runData = runData;
	}
	
	public String getFullFileName(final String defaultValue) {
		String value = defaultValue;
		final Map<String,ArtifactLocation> originalUriBaseIds = runData.getOriginalUriBaseIds();
		Location[] locations = getLocations();
		if ( locations!=null && locations.length>0 && locations[0].getPhysicalLocation()!=null ) {
			value = locations[0].getPhysicalLocation().getArtifactLocation().getFullFileName(originalUriBaseIds);
		} else if ( getAnalysisTarget()!=null ) {
			value = getAnalysisTarget().getFullFileName(originalUriBaseIds);
		}
		return value;
	}
	
	public ReportingDescriptor getRule() {
		if ( this.rule == null ) {
			String ruleId = getRuleId();
			this.rule = ruleId==null ? null : runData.getRules().get(ruleId);
		}
		return this.rule;
	}
	
	public Level getLevelOrDefault() {
		Level level = result.getLevel();
		ReportingDescriptor rule = getRule();
		// TODO Currently we don't check for level overrides;
		//      See https://docs.oasis-open.org/sarif/sarif/v2.1.0/cs01/sarif-v2.1.0-cs01.html#_Toc16012604 
		if ( level == null ) {
			if ( rule!=null ) {
				level = rule.getDefaultConfiguration().getLevel();
			} else if ( getKind()==Kind.fail || getKind()==null ) {
				level = Level.warning;
			} else {
				level = Level.none;
			}
		}
		
		return level;
	}
	
	public String getResultMessage() {
		Message msg = getMessage();
		if ( msg.getText()!=null ) {
			return msg.getText();
		} else if ( msg.getId()!=null ) {
			MultiformatMessageString msgString = getRule().getMessageStrings().get(msg.getId());
			// TODO Do we need to improve handling of single quotes around arguments?
			return MessageFormat.format(msgString.getText().replace("'", "''"), (Object[])msg.getArguments());
		} else {
			return null;
		}
	}
	
	public String getVulnerabilityUuid() {
		String uuidString = null;
		if ( StringUtils.isNotBlank(getGuid()) ) {
			uuidString = getGuid();
		} else if ( StringUtils.isNotBlank(getCorrelationGuid()) ) {
			uuidString = getCorrelationGuid();
		} else {
			uuidString = getCalculatedIdString();
		}
		return DigestUtils.sha256Hex(uuidString);
	}
	
	// TODO This may need improvement
	//      As described at https://docs.oasis-open.org/sarif/sarif/v2.1.0/cs01/sarif-v2.1.0-cs01.html#_Toc16012888
	//      we calculate a unique id string based on tool name, full file location,
	//      rule id and partial finger prints if available. To increase chances
	//      of generating a unique id, we also include the result message.
	//      However, this could potentially still result in duplicate id strings. 
	//      Possibly we could add information from other properties like region or 
	//      logical location, but these may either not be available, or still result 
	//      in duplicate uuid strings.
	private String getCalculatedIdString() {
		if ( getFingerprints()!=null && getFingerprints().size()>0 ) {
			return new TreeMap<>(getFingerprints()).toString();
		} else {
			String partialFingerPrints = getPartialFingerprints()==null?"":new TreeMap<>(getPartialFingerprints()).toString();
			return String.join("|", 
				runData.getToolName(),
				getFullFileName("Unknown"),
				getRuleId(),
				partialFingerPrints,
				getResultMessage());
		}
	}
	
	public String getCategory() {
		ReportingDescriptor rule = getRule();
		return rule==null || rule.getName()==null 
				? Constants.ENGINE_TYPE 
				: StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rule.getName()), StringUtils.SPACE));
	}
	
	public String getSubCategory() {
		ReportingDescriptor rule = getRule();
		return rule==null || rule.getName()==null 
				? result.getRuleId() 
				: null;
	}
}
