package com.fortify.ssc.parser.sarif.parser;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.ssc.parser.sarif.parser.util.Constants;

/**
 * This class parses the SARIF JSON to set the various {@link ScanBuilder}
 * properties. If the input file contains multiple runs or invocations,
 * the corresponding {@link ScanBuilder} properties may be set multiple
 * times. As such, for example the scan date will be set to the end time 
 * of the last invocation of the last run, based on the order of runs and
 * invocations in the input file.
 * 
 * @author Ruud Senden
 */
public class ScanParser extends AbstractParser {
	private final ScanData scanData;
    private final ScanBuilder scanBuilder;
    @JsonProperty private String version;
    
	public ScanParser(final ScanData scanData, ScanBuilder scanBuilder) {
		this.scanData = scanData;
		this.scanBuilder = scanBuilder;
	}
	
	public final void parse() throws ScanParsingException, IOException {
		super.parse(scanData);
	}
	
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/runs/invocations/endTime", 
				jp -> scanBuilder.setScanDate(DATE_CONVERTER.convert(jp.getValueAsString())));
		pathToHandlerMap.put("/runs/invocations/machine", 
				jp -> scanBuilder.setHostName(jp.getValueAsString()));
		pathToHandlerMap.put("/runs/instanceGuid", 
				jp -> scanBuilder.setBuildId(jp.getValueAsString()));
		pathToHandlerMap.put("/runs/logicalId", 
				jp -> scanBuilder.setScanLabel(jp.getValueAsString()));
		pathToHandlerMap.put("/runs/files", 
				jp -> scanBuilder.setNumFiles(countObjectEntries(jp)));
	}
	
	@Override
	protected <T> T finish() throws ScanParsingException {
		if ( !"2.1.0".equals(version) ) {
			throw new ScanParsingException(Constants.MSG_UNSUPPORTED_INPUT_FILE_VERSION+": "+version);
		}
		scanBuilder.setEngineVersion(version);
		scanBuilder.completeScan();
		return null;
	}
}
