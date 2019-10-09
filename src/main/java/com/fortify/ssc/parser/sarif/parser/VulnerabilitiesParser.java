package com.fortify.ssc.parser.sarif.parser;

import java.io.IOException;
import java.util.Map;

import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.parser.subentity.RunParser;

/**
 * This is the top-level parser class for generating vulnerabilities
 * based on SARIF input data. It will look for the /runs array, and
 * parse each individual run using the {@link RunParser} class. 
 * 
 * @author Ruud Senden
 *
 */
public final class VulnerabilitiesParser extends AbstractParser {
	private final ScanData scanData;
	private final VulnerabilityHandler vulnerabilityHandler;
	
	public VulnerabilitiesParser(final ScanData scanData, final VulnerabilityHandler vulnerabilityHandler) {
		this.vulnerabilityHandler = vulnerabilityHandler;
		this.scanData = scanData;
	}
	
	public final void parse() throws ScanParsingException, IOException {
		super.parse(scanData);
	}
	
	@Override
	protected void addHandlers(Map<String, Handler> pathToHandlerMap) {
		pathToHandlerMap.put("/runs", jp->parseArrayEntries(jp, ()->new RunParser(scanData, vulnerabilityHandler)));
	}
}
