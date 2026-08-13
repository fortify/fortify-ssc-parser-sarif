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
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanEntry;
import com.fortify.util.cache.CachedObject;
import com.fortify.util.cache.CachedObjectArrayList;
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
	private final Map<String, ArtifactLocation> originalUriBaseIds;
	private final CachedObjectArrayList<Artifact> artifactsByIndex;
	private final Map<String, Integer> ruleIndexesById;
	private final ObjectMapper objectMapper;
	private final Map<String, Integer> ruleIndexesByGuid;
	private final CachedObjectArrayList<ReportingDescriptor> rulesByIndex;
	@Getter private Region resultsRegion = null;
	@Getter private String toolName;
	private final ScanData scanData;
	private final ScanEntry scanEntry;

	/**
	 * Private constructor; instances can be created through the
	 * {@link #parseRunData(DB, ExtendedJsonParser)}
	 * method.
	 * 
	 * @param scanData
	 * @param scanEntry
	 * @param objectMapper
	 */
	private RunData(final ScanData scanData, final ScanEntry scanEntry, final ObjectMapper objectMapper) {
		this.originalUriBaseIds = new HashMap<>();
		this.scanData = scanData;
		this.scanEntry = scanEntry;
		this.objectMapper = objectMapper;
		this.artifactsByIndex = new CachedObjectArrayList<>();
		this.ruleIndexesById = new HashMap<>();
		this.ruleIndexesByGuid = new HashMap<>();
		this.rulesByIndex = new CachedObjectArrayList<>();
	}

	/**
	 * This method parses auxiliary data from a SARIF <code>run</code> object;
	 * the returned {@link RunData} object provides access to this auxiliary data.
	 * 
	 * @param db         used to temporarily store some data in disk-backed
	 *                   collections
	 * @param jsonParser pointing at a <code>run</code> entry in the SARIF
	 *                   <code>runs</code> array
	 * @return {@link RunData} instance
	 * @throws IOException
	 */
	public static final RunData parseRunData(final ExtendedJsonParser jsonParser,
			final ScanData scanData,
			final ScanEntry scanEntry,
			final ObjectMapper objectMapper) throws IOException {
		RunData runData = new RunData(scanData, scanEntry, objectMapper);
		new StreamingJsonParser()
				.handler("/originalUriBaseIds/*", runData::addOriginalUriBaseId)
				.handler("/artifacts/*", runData::addArtifactWithRegion)
				.handler("/tool/driver/rules/*", runData::addRuleWithRegion)
				.handler("/tool/driver/name", String.class, runData::setToolName)
				.handler("/results", runData::setResultsRegion)
				.parseObjectProperties(jsonParser, "/");
		return runData;
	}

	/**
	 * Handler: Parse artifact with region capture (single pass).
	 * 
	 * Called by StreamingJsonParser for each array element at path "/artifacts/*".
	 * Parser is positioned at START_OBJECT when handler is invoked.
	 */
	private void addArtifactWithRegion(ExtendedJsonParser jp) throws IOException {
		// Factory method: parse + capture region in single pass
		CachedObject<Artifact> cached = CachedObject.parse(jp, Artifact.class,
				scanData, scanEntry, objectMapper);
		artifactsByIndex.add(cached);
	}

	/**
	 * Handler: Parse rule with region capture (single pass).
	 * 
	 * Called by StreamingJsonParser for each array element at path
	 * "/tool/driver/rules/*".
	 * Parser is positioned at START_OBJECT when handler is invoked.
	 * 
	 * Also updates rule lookup indexes for getRule*ById/ByGuid searches.
	 */
	private void addRuleWithRegion(ExtendedJsonParser jp) throws IOException {
		// Factory method: parse + capture region in single pass
		CachedObject<ReportingDescriptor> cached = CachedObject.parse(jp, ReportingDescriptor.class,
				scanData, scanEntry, objectMapper);
		rulesByIndex.add(cached);

		// Update indexes (for getRule*ById/ByGuid lookups)
		int index = rulesByIndex.size() - 1;
		ReportingDescriptor rule = cached.getOrReload(); // Get rule to extract ID/GUID
		addRuleIndex(ruleIndexesById, rule.getId(), index);
		addRuleIndex(ruleIndexesByGuid, rule.getGuid(), index);
	}

	private final void addOriginalUriBaseId(ExtendedJsonParser jp) throws IOException {
		originalUriBaseIds.put(jp.currentName(), jp.readValueAs(ArtifactLocation.class));
	}

	private final void addRuleIndex(Map<String, Integer> map, String key, int index) {
		if (StringUtils.isNotBlank(key)) {
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
		return uriBaseId == null ? null : originalUriBaseIds.get(uriBaseId);
	}

	public final Artifact getArtifactByIndex(Integer index) {
		if (index == null)
			return null;
		return artifactsByIndex.getCachedObject(index);
	}

	public final ReportingDescriptor getRuleById(String id) {
		return getRuleByIndex(ruleIndexesById.get(id));
	}

	public final ReportingDescriptor getRuleByGuid(String guid) {
		return getRuleByIndex(ruleIndexesByGuid.get(guid));
	}

	public final ReportingDescriptor getRuleByIndex(Integer index) {
		if (index == null)
			return null;
		return rulesByIndex.getCachedObject(index);
	}
}
