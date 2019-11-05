package com.fortify.ssc.parser.sarif.parser;

import static com.fortify.util.json.AbstractStreamingJsonParser.countArrayEntries;

import java.io.IOException;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.ssc.parser.sarif.parser.util.Constants;
import com.fortify.ssc.parser.sarif.parser.util.SarifScanDataStreamingJsonParser;
import com.fortify.util.jackson.DateConverter;

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
public class ScanParser {
	private final ScanData scanData;
    private final ScanBuilder scanBuilder;
    private String version;
    private int numFiles = 0;
    
	public ScanParser(final ScanData scanData, ScanBuilder scanBuilder) {
		this.scanData = scanData;
		this.scanBuilder = scanBuilder;
	}
	
	public final void parse() throws ScanParsingException, IOException {
		new SarifScanDataStreamingJsonParser()
			.handler("/version", jp -> version=jp.getValueAsString())
			.handler("/runs/invocations/endTimeUtc", jp -> scanBuilder.setScanDate(DateConverter.getInstance().convert(jp.getValueAsString())))
			.handler("/runs/invocations/machine", jp -> scanBuilder.setHostName(jp.getValueAsString()))
			.handler("/runs/automationId/guid", jp -> scanBuilder.setBuildId(jp.getValueAsString()))
			.handler("/runs/automationId/id", jp -> scanBuilder.setScanLabel(jp.getValueAsString()))
			.handler("/runs/artifacts", jp -> numFiles+=countArrayEntries(jp))
			.parse(scanData);
		
		if ( !"2.1.0".equals(version) ) {
			throw new ScanParsingException(Constants.MSG_UNSUPPORTED_INPUT_FILE_VERSION+": "+version);
		}
		scanBuilder.setEngineVersion(version);
		scanBuilder.setNumFiles(numFiles);
		scanBuilder.completeScan();
	}
}
