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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fortify.util.io.Region;
import com.fortify.util.json.ExtendedJsonParser;
import com.fortify.util.json.StreamingJsonParser;

import lombok.Getter;

/**
 * This class stores auxiliary data for a <code>run</code> entry in the SARIF 
 * <code>runs</code> array, like base URI's and rules.
 * 
 * @author Ruud Senden
 *
 */
public final class RunData {
    private static final Logger LOG = LoggerFactory.getLogger(RunData.class);
	private final Map<String, ArtifactLocation> originalUriBaseIds;
	private final List<Artifact> artifactsByIndex;
	private final Map<String, Integer> ruleIndexesById;
	private final Map<String, Integer> ruleIndexesByGuid;
	private final List<ReportingDescriptor> rulesByIndex;
	@Getter private Region resultsRegion = null;
	@Getter private String toolName;
	
	/**
	 * Private constructor; instances can be created through the {@link #parseRunData(DB, ExtendedJsonParser)}
	 * method.
	 * 
	 * @param db
	 */
	@SuppressWarnings("unchecked")
    private RunData(final DB db) {
		// We assume there's only a limited set of URI base id's, so store in memory
		this.originalUriBaseIds = new HashMap<>();
		// We assume large scans may include a lot of artifacts and rules, so we use disk-backed collections.
		// Note that alternatively we could use a hash & position-based approach like the SARIF .NET SDK
		// (see DeferredDictionary and DeferredList) to avoid serializing entries to disk, but for now
		// disk-backed collections seem to perform well and the implementation is much easier to understand.
		this.artifactsByIndex = (List<Artifact>) db.indexTreeList("artifactsByIndex", Serializer.JAVA).create();
		this.ruleIndexesById = db.hashMap("ruleIndexesById", Serializer.STRING, Serializer.INTEGER).create();
		this.ruleIndexesByGuid = db.hashMap("ruleIndexesByGuid", Serializer.STRING, Serializer.INTEGER).create();
		this.rulesByIndex = (List<ReportingDescriptor>) db.indexTreeList("rulesByIndex", Serializer.JAVA).create();
	}
	
	/**
	 * This method parses auxiliary data from a SARIF <code>run</code> object;
	 * the returned {@link RunData} object provides access to this auxiliary data.
	 * 
	 * @param db used to temporarily store some data in disk-backed collections
	 * @param jsonParser pointing at a <code>run</code> entry in the SARIF <code>runs</code> array
	 * @return {@link RunData} instance
	 * @throws IOException
	 */
	public static final RunData parseRunData(final DB db, final ExtendedJsonParser jsonParser) throws IOException {
		RunData runData = new RunData(db);
		new StreamingJsonParser()
			.handler("/originalUriBaseIds/*", runData::addOriginalUriBaseId)
			.handler("/artifacts/*", Artifact.class, runData::addArtifact)
			.handler("/tool/driver/rules/*", ReportingDescriptor.class, runData::addRule)
			.handler("/tool/driver/name", String.class, runData::setToolName)
			.handler("/results", runData::setResultsRegion)
			.parseObjectProperties(jsonParser, "/");
		return runData;
	}

	private final void addOriginalUriBaseId(ExtendedJsonParser jp) throws IOException {
		originalUriBaseIds.put(jp.getCurrentName(), jp.readValueAs(ArtifactLocation.class));
	}
	
	private final void addArtifact(Artifact artifact) {
		artifactsByIndex.add(artifact);
	}

	private final void addRule(ReportingDescriptor reportingDescriptor) {
		rulesByIndex.add(reportingDescriptor);
		int index = rulesByIndex.size()-1;
		addRuleIndex(ruleIndexesById, reportingDescriptor.getId(), index);
		addRuleIndex(ruleIndexesByGuid, reportingDescriptor.getGuid(), index);
	}
	
	private final void addRuleIndex(Map<String,Integer> map, String key, int index) {
		if ( StringUtils.isNotBlank(key) ) {
			map.put(key, index);
		}
	}
	
	private final void setResultsRegion(ExtendedJsonParser jp) throws IOException {
		this.resultsRegion = jp.getObjectOrArrayRegion();
	}
	
	private final void setToolName(String toolName) {
		this.toolName = toolName;
	}
	
	public final ArtifactLocation getBaseLocation(String uriBaseId) {
		return uriBaseId==null ? null : originalUriBaseIds.get(uriBaseId);
	}
	
	public final Artifact getArtifactByIndex(Integer index) {
		if ( index==null || artifactsByIndex==null || artifactsByIndex.isEmpty() ) { return null; }
        if ( index<0 || index>=artifactsByIndex.size() ) {
            LOG.warn("SARIF input error: Ignoring non-existing artifact index "+index);
            return null;
         }
         return artifactsByIndex.get(index);
	}
	
	public final ReportingDescriptor getRuleById(String id) {
		return getRuleByIndex(ruleIndexesById.get(id));
	}
	
	public final ReportingDescriptor getRuleByGuid(String guid) {
		return getRuleByIndex(ruleIndexesByGuid.get(guid));
	}
	
	public final ReportingDescriptor getRuleByIndex(Integer index) {
	    if ( index==null || rulesByIndex==null || rulesByIndex.isEmpty() ) { return null; }
	    if ( index<0 || index>=rulesByIndex.size() ) {
	       LOG.warn("SARIF input error: Ignoring non-existing rule index "+index);
	       return null;
	    }
		return rulesByIndex.get(index);
	}
}