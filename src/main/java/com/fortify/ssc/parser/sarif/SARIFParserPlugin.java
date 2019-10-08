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
        new ScanParser(scanBuilder).parse(scanData);
    }

	@Override
	public void parseVulnerabilities(ScanData scanData, VulnerabilityHandler vulnerabilityHandler) throws ScanParsingException, IOException {
		new VulnerabilitiesParser(vulnerabilityHandler).parse(scanData);
	}
}
