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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.util.CustomSerializerElsa;

/**
 * This class will parse individual rule entries from the
 * /resources/rules object, or the /tool/driver/rules array. 
 * Parsed data will be stored in a {@link Rule} instance, 
 * which itself is added to the rules map provided in the 
 * constructor. The map key will either be the rule property
 * name (when parsing /resources/rules) as provided to the
 * constructor, or the id from the parsed {@link Rule}
 * instance.
 * 
 * @author Ruud Senden
 *
 */
public final class RuleParser extends AbstractParser {
	private final Map<String, Rule> rules;
	private final Rule rule;

	/**
	 * This constructor instantiates a new {@link Rule} object
	 * with the given objectId, adds this new {@link Rule} to
	 * the given rules map, and then calls the {@link #initializeHandlers()}
	 * method to register the JSON handlers to write data to our {@link Rule}
	 * instance.
	 * 
	 * @param rules
	 * @param objectId
	 */
	public RuleParser(Map<String, Rule> rules, String objectId) {
		super(false);
		this.rules = rules;
		this.rule = new Rule(objectId);
		initializeHandlers();
	}
	
	@Override
	protected <T> T finish() throws ScanParsingException, IOException {
		rules.put(rule.getObjectId(), rule);
		return null;
	}
	
	/** 
	 * This method simply adds property handlers for our {@link Rule} instance.
	 */
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		addPropertyHandlers(pathToHandlerMap, rule);
	}
	
	/**
	 * This data class holds all relevant rule-related information.
	 * 
	 * @author Ruud Senden
	 */
	public static final class Rule implements Serializable {
		public static final CustomSerializerElsa<Rule> SERIALIZER = new CustomSerializerElsa<>(Rule.class, RuleConfiguration.class, SARIFLevel.class, SARIFMessage.class, Enum.class, HashMap.class, LinkedHashMap.class);
		private static final long serialVersionUID = 1L;
		private final String objectId;
		@JsonProperty private String id;
		@JsonProperty private RuleConfiguration configuration = new RuleConfiguration();
		@JsonProperty private String name;
		@JsonProperty private Map<String,String> messageStrings;
		@JsonProperty private SARIFMessage shortDescription;
		@JsonProperty private SARIFMessage fullDescription;
		
		public Rule(String objectId) {
			this.objectId = objectId;
		}
		public String getObjectId() {
			return objectId==null?id:objectId;
		}
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public RuleConfiguration getConfiguration() {
			return configuration;
		}
		public Map<String, String> getMessageStrings() {
			return messageStrings;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}
		public SARIFMessage getShortDescription() {
			return shortDescription;
		}
		public SARIFMessage getFullDescription() {
			return fullDescription;
		}
		
		public static final class RuleConfiguration implements Serializable {
			private static final long serialVersionUID = 1L;
			@JsonProperty private SARIFLevel defaultLevel = SARIFLevel.warning;

			public SARIFLevel getDefaultLevel() {
				return defaultLevel;
			}
		}
	}
}