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
package com.fortify.ssc.parser.sarif.parser.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public final class Result {
	@JsonProperty private String guid;
	@JsonProperty private String correlationGuid;
	@JsonProperty private String ruleId;
	// @JsonProperty private int ruleIndex;
	// @JsonProperty private ReportingDescriptorReference rule;
	// @JsonProperty private ReportingDescriptorReference[] taxa;
	// @JsonProperty private Kind kind; // pass/open/informational/notApplicable/review/fail
	@JsonProperty private Level level;
	@JsonProperty private Message message; // Can contain message, or id pointing to rule.messageStrings
	@JsonProperty private ArtifactLocation[] locations; // TODO Are fileLocation and location the same?
	@JsonProperty private ArtifactLocation analysisTarget;
	// @JsonProperty private WebRequest webRequest;
	// @JsonProperty private WebResponse webResponse;
	@JsonProperty private Map<String,String> fingerprints;
	@JsonProperty private Map<String,String> partialFingerprints;
	// @JsonProperty private CodeFlow codeFlow;
	// @JsonProperty private Graph[] graphs;
	// @JsonProperty private GraphTraversal[] graphTraversals;
	// @JsonProperty private Stack[] stacks;
	@JsonProperty private ArtifactLocation[] relatedLocations; // TODO Are fileLocation and location the same?
	// @JsonProperty private Suppression[] suppressions;
	// @JsonProperty private String baselineState;
	// @JsonProperty private float rank;
	// @JsonProperty private Attachment[] attachments;
	// @JsonProperty private URI[] workItemUris;
	// @JsonProperty private URI hostedViewerUri;
	// @JsonProperty private ResultProvenance provenance;
	// @JsonProperty private Fix[] fixes;
}