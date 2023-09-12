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

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * This data class holds all relevant rule-related information.
 * 
 * @author Ruud Senden
 */
@Getter
public final class ReportingDescriptor implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty private String id;
	@JsonProperty private String guid;
	@JsonProperty private ReportingConfiguration defaultConfiguration = new ReportingConfiguration();
	@JsonProperty private String name;
	@JsonProperty private Map<String,MultiformatMessageString> messageStrings;
	@JsonProperty private Message shortDescription;
	@JsonProperty private Message fullDescription;
	@JsonProperty private URI helpUri;
	@JsonProperty private MultiformatMessageString help;
	@JsonProperty private Map<String, Object> properties;
	
	public Level getDefaultLevel() {
		return defaultConfiguration==null ? null : defaultConfiguration.getLevel();
	}
	
	public MultiformatMessageString getMessageString(String id) {
		return messageStrings==null ? null : messageStrings.get(id);
	}
}