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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.ssc.parser.sarif.parser.AbstractParser;
import com.fortify.ssc.parser.sarif.parser.util.CustomSerializerElsa;

/**
 * This class will parse individual rule entries from the
 * /resources/rules array. Parsed data will be stored in 
 * a {@link Rule} instance, which itself is added to the
 * rules map provided in the constructor, under the objectId
 * provided in the constructor.
 * 
 * @author Ruud Senden
 *
 */
public final class RuleParser extends AbstractParser {
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
		this.rule = new Rule(objectId);
		rules.put(objectId, rule);
		initializeHandlers();
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
		public static final CustomSerializerElsa<Rule> SERIALIZER = new CustomSerializerElsa<>(Rule.class, RuleConfiguration.class, SARIFLevel.class, Enum.class);
		private static final long serialVersionUID = 1L;
		private final String objectId;
		@JsonProperty private String id;
		@JsonProperty private String name;
		@JsonProperty private RuleConfiguration configuration = new RuleConfiguration();
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
		public RuleConfiguration getConfiguration() {
			return configuration;
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
		
		public static final class RuleConfiguration implements Serializable {
			private static final long serialVersionUID = 1L;
			@JsonProperty private SARIFLevel defaultLevel = SARIFLevel.warning;

			public SARIFLevel getDefaultLevel() {
				return defaultLevel;
			}
		}
	}
}