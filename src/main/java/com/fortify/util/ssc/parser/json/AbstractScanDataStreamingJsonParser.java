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
package com.fortify.util.ssc.parser.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fortify.plugin.api.ScanData;
import com.fortify.util.io.Region;
import com.fortify.util.json.AbstractStreamingJsonParser;

/**
 * Abstract base class for stream-based JSON parsers that parse data from a
 * {@link ScanData} source. This class was removed from fortify-ssc-parser-util
 * v2.0.0+ and has been reimplemented locally to maintain backward
 * compatibility.
 * 
 * Provides convenient methods for parsing JSON data with support for filtering
 * input by file extension.
 * 
 * @param <T> Concrete subtype
 * @author Ruud Senden
 */
public abstract class AbstractScanDataStreamingJsonParser<T extends AbstractScanDataStreamingJsonParser<T>>
        extends AbstractStreamingJsonParser<T> {
    private final List<String> supportedExtensions = new ArrayList<>();

    public AbstractScanDataStreamingJsonParser(String supportedExtension, String... supportedExtensions) {
        this.supportedExtensions.add(supportedExtension);
        this.supportedExtensions.addAll(Arrays.asList(supportedExtensions));
    }

    /**
     * Parse JSON contents retrieved from the given {@link ScanData} using
     * the previously configured handlers.
     * 
     * @param scanData {@link ScanData} instance
     * @throws IOException if there is any error while accessing or parsing the
     *                     input data
     */
    public final void parse(ScanData scanData) throws IOException {
        parse(scanData, null);
    }

    /**
     * Parse JSON contents retrieved from the given {@link ScanData} object
     * for the given input region, using the previously configured handlers.
     * 
     * @param scanData    {@link ScanData} instance
     * @param inputRegion {@link Region} to be parsed
     * @throws IOException if there is any error while accessing or parsing the
     *                     input data
     */
    public final void parse(ScanData scanData, Region inputRegion) throws IOException {
        try (final InputStream inputStream = scanData.getInputStream(fileName -> hasSupportedExtension(fileName))) {
            parse(inputStream, inputRegion);
        }
    }

    private final boolean hasSupportedExtension(String fileName) {
        for (String ext : supportedExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
