/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
package com.fortify.ssc.parser.sarif.domain;

import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public final class Result {
	@JsonProperty private String guid;
	@JsonProperty private String correlationGuid;
	@JsonProperty private String ruleId;
	@JsonProperty private Integer ruleIndex;
	@JsonProperty private ReportingDescriptorReference rule;
	// @JsonProperty private ReportingDescriptorReference[] taxa;
	@JsonProperty private Kind kind;
	@JsonProperty private Level level;
	@JsonProperty private Message message;
	@JsonProperty private Location[] locations; 
	@JsonProperty private ArtifactLocation analysisTarget;
	// @JsonProperty private WebRequest webRequest;
	// @JsonProperty private WebResponse webResponse;
	@JsonProperty private Map<String,String> fingerprints;
	@JsonProperty private Map<String,String> partialFingerprints;
	// @JsonProperty private CodeFlow codeFlow;
	// @JsonProperty private Graph[] graphs;
	// @JsonProperty private GraphTraversal[] graphTraversals;
	// @JsonProperty private Stack[] stacks;
	// @JsonProperty private Location[] relatedLocations;
	// @JsonProperty private Suppression[] suppressions;
	// @JsonProperty private String baselineState;
	// @JsonProperty private float rank;
	// @JsonProperty private Attachment[] attachments;
	// @JsonProperty private URI[] workItemUris;
	// @JsonProperty private URI hostedViewerUri;
	// @JsonProperty private ResultProvenance provenance;
	// @JsonProperty private Fix[] fixes;
	@JsonProperty private Map<String, Object> properties;
	
	private volatile ReportingDescriptor resolvedRule;
	
	public String resolveFullFileName(RunData runData, final String defaultValue) {
		String value = defaultValue;
		Location[] locations = getLocations();
		if ( locations!=null && locations.length>0 ) {
			if ( locations[0].getMessage()!=null ){
				value = resolveMessage(locations[0].getMessage(), runData);
			} else if ( locations[0].getPhysicalLocation()!=null ) {
				value = locations[0].getPhysicalLocation().resolveArtifactLocation(runData).getFullFileName(runData);
			}
		} else if ( getAnalysisTarget()!=null ) {
			value = getAnalysisTarget().getFullFileName(runData);
		}
		return value;
	}
	
	public ReportingDescriptor resolveRule(RunData runData) {
		if ( this.resolvedRule == null ) {
			this.resolvedRule = resolveRuleByIndex(runData);
			if ( this.resolvedRule == null ) {
				this.resolvedRule = resolveRuleById(runData);
				if ( this.resolvedRule == null ) {
					this.resolvedRule = resolveRuleByGuid(runData);
					if ( this.resolvedRule == null ) {
						this.resolvedRule = new ReportingDescriptor(); // Set empty rule to avoid multiple lookups
					}
				}
			}
		}
		return this.resolvedRule;
	}
	
	private ReportingDescriptor resolveRuleByIndex(RunData runData) {
		Integer ruleIndex = resolveRuleIndex();
		return ruleIndex==null ? null : runData.getRuleByIndex(ruleIndex);
	}
	
	private ReportingDescriptor resolveRuleById(RunData runData) {
		String ruleId = resolveRuleId(runData);
		return ruleId==null ? null : runData.getRuleById(ruleId);
	}
	
	private ReportingDescriptor resolveRuleByGuid(RunData runData) {
		String ruleGuid = resolveRuleGuid(runData);
		return ruleGuid==null ? null : runData.getRuleByGuid(ruleGuid);
	}
	
	private Integer resolveRuleIndex() {
		if ( getRuleIndex()!=null ) { 
			return getRuleIndex(); 
		} else if ( getRule()!=null && getRule().getIndex()!=null ) {
			return getRule().getIndex();
		}
		return null;
	}
	
	public String resolveRuleId(RunData runData) { 
		if ( StringUtils.isNotBlank(getRuleId()) ) { 
			return getRuleId(); 
		} else if ( getRule()!=null && StringUtils.isNotBlank(getRule().getId()) ) {
			return getRule().getId();
		} else {
			ReportingDescriptor ruleByIndex = resolveRuleByIndex(runData);
			if ( ruleByIndex!=null ) { 
				return ruleByIndex.getId();
			}
		}
		return null;
	}
	
	public String resolveRuleGuid(RunData runData) {
		if ( getRule()!=null && StringUtils.isNotBlank(getRule().getGuid()) ) {
			return getRule().getGuid();
		} else {
			ReportingDescriptor ruleByIndex = resolveRuleByIndex(runData);
			if ( ruleByIndex!=null ) { 
				return ruleByIndex.getGuid();
			}
		}
		return null;
	}

	public Level resolveLevel(RunData runData) {
		Level level = getLevel();
		// TODO Currently we don't check for level overrides;
		//      See https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317648
		if ( level == null ) {
			ReportingDescriptor resolvedRule = resolveRule(runData);
			if ( resolvedRule!=null ) {
				level = resolvedRule.getDefaultLevel();
			} else if ( getKind()==Kind.fail || getKind()==null ) {
				level = Level.warning;
			} else {
				level = Level.none;
			}
		}
		return level;
	}

	public String getResultMessage(RunData runData) {
		return resolveMessage(getMessage(), runData);
	}

	public String resolveMessage(Message msg, RunData runData) {
		String text = msg.getText();
		if ( StringUtils.isBlank(text) && msg.getId()!=null ) {
			MultiformatMessageString msgString = getMultiformatMessageStringForId(runData, msg.getId());
			// TODO Should we throw an exception if msgString==null, instead of just returning null?
			text = msgString==null ? null : msgString.getText();
		} 
		if ( StringUtils.isNotBlank(text) ) {
			// TODO SARIF specification doesn't clearly state whether args should be resolved before replacing links, or vice versa
			//      (i.e. can args contain links, do link characters need to be escaped in args?)
			text = resolveArgs(text, msg.getArguments());
			text = replaceLinks(text, runData);
		}
		return text;
	}
	
	protected static final String resolveArgs(String text, String[] args) {
		// Based on the length of the args array, we produce another array containing placeholders "{0}", "{1}", "{2}", ...
		// Then we do a simple search and replace for each of these placeholders using StringUtils#replaceEach.
		// This works better than something like Java's MessageFormat as this has many other characteristics that we don't want.
		String[] placeholders = args==null ? null 
				: IntStream.rangeClosed(0, args.length-1).mapToObj(i->String.format("{%d}", i)).toArray(String[]::new);
		return StringUtils.replaceEach(text, placeholders, args);
	}
	
	// For now we simply render the link text; future versions may add extra info from the actual link
	// as per the example in https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317467
	protected static final String replaceLinks(String text, RunData runData) {
		// Use regex (?<!\\)\[(.*?)(?<!\\)\]\(.*?\) to find link texts:
		// - Find any non-escaped '[':   (?<!\\)\[
		// - Find link text:             (.*?)
		// - Find next non-escaped ']':  (?<!\\)\]
		// - Find link destination:      \(.*?\)
		text = text.replaceAll("(?<!\\\\)\\[(.*?)(?<!\\\\)\\]\\(.*?\\)", "$1");
		// Replace all escaped '[', ']' and '\' with non-escaped version
		text = text.replaceAll("\\\\\\[", "[");
		text = text.replaceAll("\\\\\\]", "]");
		text = text.replaceAll("\\\\\\\\", "\\\\");
		return text;
	}

	private MultiformatMessageString getMultiformatMessageStringForId(RunData runData, String id) {
		ReportingDescriptor rule = resolveRule(runData);
		return rule == null ? null : rule.getMessageString(id);
	}
}