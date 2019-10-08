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
package com.fortify.ssc.parser.sarif;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanEntry;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.sarif.SARIFParserPlugin;

class SARIFParserPluginTest {
	private static final String[] SAMPLE_FILES = {"example1.sarif", "example2.sarif"};
	
	
	private final ScanData getScanData(String fileName) {
		return new ScanData() {
		
			@Override
			public String getSessionId() {
				return UUID.randomUUID().toString();
			}
			
			@Override
			public List<ScanEntry> getScanEntries() {
				return null;
			}
			
			@Override
			public InputStream getInputStream(Predicate<String> matcher) throws IOException {
				return ClassLoader.getSystemResourceAsStream(fileName);
			}
			
			@Override
			public InputStream getInputStream(ScanEntry scanEntry) throws IOException {
				return ClassLoader.getSystemResourceAsStream(fileName);
			}
		};
	}
	
	private final ScanBuilder scanBuilder = (ScanBuilder) Proxy.newProxyInstance(
			SARIFParserPluginTest.class.getClassLoader(), 
			  new Class[] { ScanBuilder.class }, new InvocationHandler() {
				
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					System.err.println(method.getName()+": "+(args==null?null:Arrays.asList(args)));
					return null;
				}
			});
	
	private final VulnerabilityHandler vulnerabilityHandler = new VulnerabilityHandler() {
		
		@Override
		public StaticVulnerabilityBuilder startStaticVulnerability(String instanceId) {
			System.err.println("startStaticVulnerability: "+instanceId);
			return (StaticVulnerabilityBuilder) Proxy.newProxyInstance(
					SARIFParserPluginTest.class.getClassLoader(), 
					  new Class[] { StaticVulnerabilityBuilder.class }, new InvocationHandler() {
						
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							System.err.println(method.getName()+": "+(args==null?null:Arrays.asList(args)));
							return null;
						}
					}); 
		}
	};
	
	@Test
	void testParseScan() throws Exception {
		for ( String file : SAMPLE_FILES ) {
			System.err.println("\n\n---- "+file+" - parseScan");
			new SARIFParserPlugin().parseScan(getScanData(file), scanBuilder);
			// TODO Check actual output
		}
	}
	
	@Test
	void testParseVulnerabilities() throws Exception {
		for ( String file : SAMPLE_FILES ) {
			System.err.println("\n\n---- "+file+" - parseVulnerabilities");
			new SARIFParserPlugin().parseVulnerabilities(getScanData(file), vulnerabilityHandler);
			// TODO Check actual output
		}
	}

}