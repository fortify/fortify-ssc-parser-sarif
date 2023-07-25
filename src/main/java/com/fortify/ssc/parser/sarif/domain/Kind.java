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
package com.fortify.ssc.parser.sarif.domain;

/**
 * A <code>result</code> object MAY contain a property named kind whose value is one of a fixed set of strings that specify the nature of the result.
 * @see <a href="https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317647">Static Analysis Results Interchange Format (SARIF) Version 2.1.0: 3.27.9 kind property</a>
 */
public enum Kind {
	/**
	 * The rule specified by <code>ruleId</code> (§3.27.5), <code>ruleIndex</code> (§3.27.6), and/or <code>rule</code> (§3.27.7) was evaluated, and no problem was found.
	 */
	pass,

	/**
	 * The specified rule was evaluated, and the tool concluded that there was insufficient information to decide whether a problem exists.
	 *<p>
	 * This value is used by proof-based tools. Sometimes such a tool can prove that there is no violation (<code>kind = "pass"</code>), sometimes it can prove that there is a violation (<code>kind = "fail"</code>), and sometimes it does not detect a violation but is unable to prove that there is none (kind = "open"). In such a tool, a kind value of "open" might be an indication that the user should add additional assertions to enable the tool to determine if there is a violation.
	 */
	open,

	/**
	 * The specified rule was evaluated and produced a purely informational result that does not indicate the presence of a problem.
	 */
	informational,

	/**
	 * The rule specified by <code>ruleId</code> was not evaluated, because it does not apply to the analysis target.
	 *
	 * EXAMPLE: In this example, a binary checker has a rule that applies to 32-bit binaries only. It produces a "notApplicable" result if it is run on a 64-bit binary. It also has a rule that checks the compiler version and produces an informational result:
<pre>{@code
"results": [
  {
    "ruleId": "ABC0001",
    "kind": "notApplicable",
    "message": {
      "text": "\"MyTool64.exe\" was not evaluated for rule ABC0001
               because it is not a 32-bit binary."
    },
    "locations": [
      {
        "physicalLocation": {
          "uri": "file://C:/bin/MyTool64.exe"
        }
      }
    ]
  },
  {
    "ruleId": "ABC0002",
    "kind": "informational",
    "message": {
      "text": "\"MyTool64.exe\" was compiled with Example Corporation
               Compiler version 10.2.2."
    },
    "locations": [
      {
        "physicalLocation": {
          "uri": "file://C:/bin/MyTool64.exe"
        }
      }
    ]
  }
]
}</pre>
	 */
	notApplicable,

	/**
	 * The result requires review by a human user to decide if it represents a problem.
	 * <p>
	 * This value is used by tools that are unable to check for certain conditions, but that wish to bring to the user’s attention the possibility that there might be a problem. For example, an accessibility checker might produce a result with the message "Do not use color alone to highlight important information," with <code>kind = "review"</code>. A user might address this issue by visually inspecting the UI.
	 */
	review,

	/**
	 * The result represents a problem whose severity is specified by the <code>level</code> property (§3.27.10).
	 */
	fail
}