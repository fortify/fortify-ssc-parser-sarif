package com.fortify.ssc.parser.sarif;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.plugin.spi.ParserPlugin;
import com.fortify.ssc.parser.sarif.parser.ScanParser;
import com.fortify.ssc.parser.sarif.parser.VulnerabilitiesParser;
import com.fortify.ssc.parser.sarif.parser.subentity.ResultsParser.CustomVulnAttribute;

/**
 * Main {@link ParserPlugin} implementation for parsing SARIF results; see
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/cs01/sarif-v2.1.0-cs01.html
 * for the supported SARIF specification. This class simply defines the various 
 * parser plugin SPI methods; actual parsing is done by the appropriate dedicated 
 * parser classes.
 * 
 * @author Ruud Senden
 *
 */
public class SARIFParserPlugin implements ParserPlugin<CustomVulnAttribute> {
    private static final Logger LOG = LoggerFactory.getLogger(SARIFParserPlugin.class);

    @Override
    public void start() throws Exception {
        LOG.info("SARIF parser plugin is starting");
    }

    @Override
    public void stop() throws Exception {
        LOG.info("SARIF parser plugin is stopping");
    }

    @Override
    public Class<CustomVulnAttribute> getVulnerabilityAttributesClass() {
        return CustomVulnAttribute.class;
    }

    @Override
    public void parseScan(final ScanData scanData, final ScanBuilder scanBuilder) throws ScanParsingException, IOException {
        new ScanParser(scanData, scanBuilder).parse();
    }

	@Override
	public void parseVulnerabilities(final ScanData scanData, final VulnerabilityHandler vulnerabilityHandler) throws ScanParsingException, IOException {
		new VulnerabilitiesParser(scanData, vulnerabilityHandler).parse();
	}
}
