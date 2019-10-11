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
package com.fortify.util.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.input.BoundedInputStream;

/**
 * This {@link InputStream} wrapper returns only the data between the start 
 * and end positions provided in a given {@link Region}. If no {@link Region} 
 * is provided, this implementation will return the full contents of the original
 * {@link InputStream}.
 * 
 * @author Ruud Senden
 *
 */
public class RegionInputStream extends BoundedInputStream {

	public RegionInputStream(InputStream in, Region region, boolean propagateClose) throws IOException {
		super(in, region==null?-1:region.getEnd());
		if ( region!=null ) {
			skip(region.getStart());
		}
		setPropagateClose(propagateClose);
	}

}
