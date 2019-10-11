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

import static com.fortify.util.json.AbstractStreamingJsonParser.getObjectOrArrayRegion;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.ssc.parser.sarif.domain.ArtifactLocation;
import com.fortify.ssc.parser.sarif.domain.ReportingDescriptor;
import com.fortify.util.io.Region;
import com.fortify.util.json.StreamingJsonParser;
import com.fortify.util.json.handler.AddJsonPropertyValueToMapJsonHandler;
import com.fortify.util.json.handler.JsonArrayToObjectMapHandler;

/**
 * This class stores auxiliary data for a <code>run</code> entry in the SARIF 
 * <code>runs</code> array, like base URI's and rules.
 * 
 * @author Ruud Senden
 *
 */
public final class RunData {
	private final JsonArrayToObjectMapHandler<String, ReportingDescriptor> rulesHandler;
	private final AddJsonPropertyValueToMapJsonHandler<ArtifactLocation> originalUriBaseIdHandler;
	private Region resultsRegion = null;
	
	/**
	 * Private constructor; instances can be created through the {@link #parseRunData(DB, JsonParser)}
	 * method.
	 * 
	 * @param db
	 */
	private RunData(final DB db) {
		originalUriBaseIdHandler = 
    			new AddJsonPropertyValueToMapJsonHandler<>(ArtifactLocation.class);
		rulesHandler = new JsonArrayToObjectMapHandler<String, ReportingDescriptor>(
			db.hashMap("rules", Serializer.STRING, ReportingDescriptor.SERIALIZER).create(), ReportingDescriptor.class, entry->entry.getId());
	}
	
	/**
	 * This method parses auxiliary data from a SARIF <code>run</code> object;
	 * the returned {@link RunData} object provides access to this auxiliary data.
	 * 
	 * @param db used to temporarily store some data in disk-backed collections
	 * @param jsonParser pointing at a <code>run</code> entry in the SARIF <code>runs</code> array
	 * @return {@link RunData} instance
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public static final RunData parseRunData(final DB db, final JsonParser jsonParser) throws ScanParsingException, IOException {
		RunData runData = new RunData(db);
		new StreamingJsonParser()
			.handler("/originalUriBaseIds/*", runData.originalUriBaseIdHandler)
			.handler("/tool/driver/rules", runData.rulesHandler)
			.handler("/results", jp -> runData.resultsRegion = getObjectOrArrayRegion(jp))
			.parseObjectProperties(jsonParser, "/");
		return runData;
	}

	public final Map<String, ArtifactLocation> getOriginalUriBaseIds() {
		return Collections.unmodifiableMap(originalUriBaseIdHandler.getMap());
	}
	
	public final Map<String, ReportingDescriptor> getRules() {
		return Collections.unmodifiableMap(rulesHandler.getMap());
	}
	
	public Region getResultsRegion() {
		return resultsRegion;
	}
}