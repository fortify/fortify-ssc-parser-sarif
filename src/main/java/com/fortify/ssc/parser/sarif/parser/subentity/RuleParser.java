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

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;

public final class RuleParser extends AbstractParser {
	private final Map<String, Rule> rules;
	private final Rule rule;

	public RuleParser(Map<String, Rule> rules, String objectId) {
		super(false);
		this.rules = rules;
		this.rule = new Rule(objectId);
		initializeHandlers();
	}
	
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		addPropertyHandlers(pathToHandlerMap, rule);
	}
	
	@Override
	protected <T> T finish() {
		System.err.println(new ReflectionToStringBuilder(rule).build());
		rules.put(rule.getObjectId(), rule);
		return null;
	}
	
	public static final class Rule implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String objectId;
		@JsonProperty private String id;
		@JsonProperty private String name;
		@JsonProperty private Map<String,String> messageStrings;
		@JsonProperty private Map<String,String> shortDescription;
		@JsonProperty private Map<String,String> fullDescription;
		
		public Rule(String objectId) {
			this.objectId = objectId;
		}
		public String getObjectId() {
			return objectId;
		}
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public Map<String, String> getMessageStrings() {
			return messageStrings;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}
		public Map<String, String> getShortDescription() {
			return shortDescription;
		}
		public Map<String, String> getFullDescription() {
			return fullDescription;
		}
	}
}