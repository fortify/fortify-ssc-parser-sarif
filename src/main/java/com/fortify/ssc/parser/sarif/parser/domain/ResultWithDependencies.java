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

import com.fortify.ssc.parser.sarif.parser.domain.Result.Kind;

import lombok.experimental.Delegate;

/**
 * This {@link Result} wrapper class provides access to the original
 * {@link Result} methods, but adds various utility methods that 
 * utilize information provided by the {@link IResultDependencies}
 * object.
 * 
 * @author Ruud Senden
 *
 */
public class ResultWithDependencies {
	@Delegate private final Result result;
	private final IResultDependencies dependencies;
	private volatile ReportingDescriptor rule;
	
	public ResultWithDependencies(Result result, IResultDependencies dependencies) {
		this.result = result;
		this.dependencies = dependencies;
	}
	
	public String getFullFileName(String defaultValue) {
		final Map<String,ArtifactLocation> originalUriBaseIds = dependencies.getOriginalUriBaseIds();
		Location[] locations = getLocations();
		if ( locations!=null && locations.length>0 && locations[0].getPhysicalLocation()!=null ) {
			defaultValue = locations[0].getPhysicalLocation().getArtifactLocation().getFullFileName(originalUriBaseIds);
		} else if ( getAnalysisTarget()!=null ) {
			defaultValue = getAnalysisTarget().getFullFileName(originalUriBaseIds);
		}
		return defaultValue;
	}
	
	public ReportingDescriptor getRule() {
		if ( this.rule == null ) {
			String ruleId = getRuleId();
			this.rule = ruleId==null ? null : dependencies.getRules().get(ruleId);
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
	
	
}
