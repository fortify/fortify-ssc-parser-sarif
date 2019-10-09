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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.fortify.ssc.parser.sarif.parser.subentity.ResultParser;

/**
 * This constants class provides some constants that can be used
 * throughout the parser implementation.
 * 
 * @author Ruud Senden
 *
 */
public class Constants {
	private static final String DEFAULT_ENGINE_TYPE = "SARIF";
	public static final String ENGINE_TYPE = getEngineType();
	public static final String MSG_UNSUPPORTED_INPUT_FILE_VERSION = "Unsupported input file version";
	
	private Constants() {}
	
	/**
	 * Get the engine type from plugin.xml, defaulting to 
	 * {@value #DEFAULT_ENGINE_TYPE} if there is any error
	 * parsing plugin.xml.
	 * 
	 * @return
	 */
	private static final String getEngineType() {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document dDoc = builder.parse(ResultParser.class.getClassLoader().getResourceAsStream("plugin.xml"));

            XPath xPath = XPathFactory.newInstance().newXPath();
            return (String) xPath.evaluate("/plugin/issue-parser/engine-type/text()", dDoc, XPathConstants.STRING);
        } catch (Exception e) {
            return DEFAULT_ENGINE_TYPE;
        }
	}
}
