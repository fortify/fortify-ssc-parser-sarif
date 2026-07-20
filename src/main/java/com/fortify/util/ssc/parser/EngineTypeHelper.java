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
package com.fortify.util.ssc.parser;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Utility class that reads the engine type from plugin.xml.
 * This class was removed from fortify-ssc-parser-util v2.0.0+ and has been
 * reimplemented locally to maintain backward compatibility.
 * 
 * @author Ruud Senden
 *
 */
public class EngineTypeHelper {
    public static final String ENGINE_TYPE_UNKNOWN = "UNKNOWN";
    private static final Logger logger = LoggerFactory.getLogger(EngineTypeHelper.class);
    private static final String ENGINE_TYPE = _getEngineType();

    private EngineTypeHelper() {
    }

    public static final String getEngineType() {
        return ENGINE_TYPE;
    }

    /**
     * Get the engine type from plugin.xml
     * 
     * @return Engine type from plugin.xml, or the value of the
     *         {@link #ENGINE_TYPE_UNKNOWN} constant ({@value #ENGINE_TYPE_UNKNOWN})
     *         if engine type cannot be determined
     */
    private static final String _getEngineType() {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try (InputStream inputStream = EngineTypeHelper.class.getClassLoader().getResourceAsStream("plugin.xml")) {
            domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document dDoc = builder.parse(inputStream);

            XPath xPath = XPathFactory.newInstance().newXPath();
            return (String) xPath.evaluate("/plugin/issue-parser/engine-type/text()", dDoc, XPathConstants.STRING);
        } catch (Exception e) {
            logger.warn("Unable to determine engine type from plugin.xml", e);
            return ENGINE_TYPE_UNKNOWN;
        }
    }
}
