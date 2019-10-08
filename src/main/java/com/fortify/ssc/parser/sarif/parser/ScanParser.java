package com.fortify.ssc.parser.sarif.parser;

import java.util.Map;

import com.fortify.plugin.api.ScanBuilder;

public class ScanParser extends AbstractParser {
    private final ScanBuilder scanBuilder;
    
	public ScanParser(ScanBuilder scanBuilder) {
		this.scanBuilder = scanBuilder;
	}
	
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/version", 
				jp -> scanBuilder.setEngineVersion(jp.getValueAsString()));
		pathToHandlerMap.put("/runs/invocations/endTime", 
				jp -> scanBuilder.setScanDate(DATE_DESERIALIZER.convert(jp.getValueAsString())));
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
	protected <T> T finish() {
		scanBuilder.completeScan();
		return null;
	}
}
