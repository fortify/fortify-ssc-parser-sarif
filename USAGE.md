
<!-- START-INCLUDE:repo-usage.md -->


<!-- START-INCLUDE:usage/h1.standard-parser-usage.md -->

<x-tag-head>
<x-tag-meta http-equiv="X-UA-Compatible" content="IE=edge"/>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/highlight.min.js"/>
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js" />
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.js" />
--></x-tag-script>

<x-tag-style><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/styles/github.min.css" />
--></x-tag-style>

<x-tag-style><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.css" />
--></x-tag-style>
</x-tag-head>

# Fortify SSC Parser Plugin for SARIF - Usage

## Introduction


<!-- START-INCLUDE:p.marketing-intro.md -->

[Fortify Application Security](https://www.microfocus.com/en-us/solutions/application-security) provides your team with solutions to empower [DevSecOps](https://www.microfocus.com/en-us/cyberres/use-cases/devsecops) practices, enable [cloud transformation](https://www.microfocus.com/en-us/cyberres/use-cases/cloud-transformation), and secure your [software supply chain](https://www.microfocus.com/en-us/cyberres/use-cases/securing-the-software-supply-chain). As the sole Code Security solution with over two decades of expertise and acknowledged as a market leader by all major analysts, Fortify delivers the most adaptable, precise, and scalable AppSec platform available, supporting the breadth of tech you use and integrated into your preferred toolchain. We firmly believe that your great code [demands great security](https://www.microfocus.com/cyberres/application-security/developer-security), and with Fortify, go beyond 'check the box' security to achieve that.

<!-- END-INCLUDE:p.marketing-intro.md -->



<!-- START-INCLUDE:repo-intro.md -->

This Fortify SSC parser plugin allows for importing SARIF (Static Analysis Results Interchange Format) files. 

### Limitations

* **SARIF 2.1.0 only**  
  The plugin should be able to parse any SARIF files that adhere to the SARIF 2.1.0 specification. Other versions of the
  specification are currently not supported. 

* **Only basic issue information**  
  At the moment, the plugin only parses and displays basic issue information. Future versions of the plugin may display 
  more information like code flows, thread flows, web requests, web responses, ...

* **Actual results may vary depending on input**  
  For example, due to the flexibility of the SARIF specification:  
    * The plugin may be unable to calculate consistent, unique issue instance id's because the input file doesn't provide sufficient details to uniquely identify an issue
    * The plugin may not be able to determine Fortify Priority Order because the input file does not provide issue severity levels
    * The plugin may be unable to determine Fortify Priority Order because the input file uses custom properties to specify issue severity
    * The plugin may be unable to display appropriate issue category or description because the input file is lacking this information, or providing this information in a non-standard way 

* **SARIF results from multiple tools cannot be uploaded to single SSC application version**  
  Being a generic format, you may have multiple tools generating SARIF files that you want to import into SSC. Due to limitations
  in the SSC parser framework, it is currently not possible to import SARIF files from different sources into a single SSC 
  application version. Independent of which tool was actually used to generate the SARIF file, SSC will assume that all SARIF files 
  originate from the same scan engine. SSC will try to merge these uploads, thereby basically marking all issues from a previously uploaded
  SARIF file as 'removed'.

<!-- END-INCLUDE:repo-intro.md -->


## Plugin Installation

These sections describe how to install, upgrade and uninstall the parser plugin in SSC.

### Install & Upgrade

* Obtain the plugin binary jar file; either:
     * Download from the repository release page: https://github.com/fortify/fortify-ssc-parser-sarif/releases
     * Build the plugin from source: https://github.com/fortify/fortify-ssc-parser-sarif/CONTRIB.md
* If you already have another version of the plugin installed, first uninstall the previously  installed version of the plugin by following the steps under [Uninstall](#uninstall) below
* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Click the `NEW` button
	* Accept the warning
	* Upload the plugin jar file
	* Enable the plugin by clicking the `ENABLE` button
  
### Uninstall

* In Fortify Software Security Center:
     * Navigate to Administration->Plugins->Parsers
     * Select the parser plugin that you want to uninstall
     * Click the `DISABLE` button
     * Click the `REMOVE` button 

## Obtain results


<!-- START-INCLUDE:parser-obtain-results.md -->

Some products provide native support for producing analysis results in SARIF format, these results can be
directly uploaded to SSC. The SARIF MultiTool can be used to convert various other output formats into 
SARIF format. 

### Install SARIF MultiTool

The following example illustrates how to install the SARIF MultiTool. This example is for Windows only at the moment:

* Download & install: https://dotnet.microsoft.com/download/dotnet-core/thank-you/sdk-3.1.201-windows-x64-installer
* Download & install: https://dotnet.microsoft.com/download/dotnet-core/thank-you/runtime-aspnetcore-2.1.17-windows-hosting-bundle-installer
* Run the following command in an Administrator command prompt:  
  `dotnet tool install --global Sarif.Multitool --version 2.2.2`

### Convert FPR to SARIF 
  
As an example, SARIF MultiTool can be used to convert Fortify scan results into SARIF format. The resulting
SARIF file can then be uploaded to SSC and processed by the SARIF parser plugin. Obviously this use case is
for demonstration purposes only; uploading an FPR file directly will yield much better results than uploading
a SARIF file converted from an FPR file.

SARIF MultiTool supports two Fortify formats for conversion into SARIF format; either the FPR file can be 
converted directly, or the XML output of the __Fortify Security Report__ legacy report can be converted.
There are some small variations between these approaches:

* Converting from an FPR file directly is more straightforward; no need to generate intermediate report
* Converting from an FPR file directly provides more low-level details like accuracy, impact, probability, ...
* Converting from an FPR file directly lacks the issue instance id, so the parser plugin needs to calculate an instance id
* Converting from an XML report provides the issue instance id, but does not provide some other low-level details
* Contents of an XML report are configurable; search expressions and filter sets can be used to select the issues to be exported

In preparation for generating SARIF files for the Fortify SCA EightBall example, perform the following 
steps: 

* Open a new command prompt (to use updated environment variables if you just installed SARIF MultiTool)
* Navigate to `<Fortify SCA Installation Directory>\Samples\basic\EightBall`
* Follow the instructions in the README.txt file in that directory to scan the code

The following command can be used to generate a SARIF file for EightBall.fpr directly. Obviously the same
approach can be used for any other FPR file.

* `sarif convert EightBall.fpr --tool FortifyFpr --output EightBall.fpr.sarif --pretty-print --force`

An XML legacy report can be generated either from Audit WorkBench, or using the following command. Note 
that by default, this report will only output one issue per category, and will output only Critical and
High issues. You can adjust and save these settings in Audit WorkBench. See the SCA and AWB user guides
for more details. 

* `reportgenerator -format xml -f EightBall.xml -source EightBall.fpr`

Once you have an XML report, the following command can be used to generate a SARIF file:

* `sarif convert EightBall.xml --tool Fortify --output EightBall.xml.sarif --pretty-print --force`

<!-- END-INCLUDE:parser-obtain-results.md -->


## Upload results

Results can be uploaded through the SSC web interface, REST API, or SSC client utilities like FortifyClient or [fcli](https://github.com/fortify-ps/fcli). The SSC web interface, FortifyClient and most other Fortify clients require the raw results to be packaged into a zip-file; REST API and fcli allow for uploading raw results directly.

To upload results through the SSC web interface or most clients:

* Create a `scan.info` file containing a single line as follows:   
     `engineType=SARIF`
* Create a zip file containing the following:
	* The scan.info file generated in the previous step
	* The raw results file as obtained from the target system (see [Obtain results](#obtain-results) section above)
* Upload the zip file generated in the previous step to SSC
	* Using any SSC client, for example FortifyClient or Maven plugin
	* Or using the SSC web interface
	* Similar to how you would upload an FPR file
	
Both SSC REST API and fcli provide options for specifying the engine type directly, and as such it is not necessary to package the raw results into a zip-file with accompanying `scan.info` file. For example, fcli allows for uploading raw scan results using a command like the following:

`fcli ssc appversion-artifact upload <raw-results-file> --appversion MyApp:MyVersion --engine-type SARIF`

<!-- END-INCLUDE:usage/h1.standard-parser-usage.md -->


<!-- END-INCLUDE:repo-usage.md -->


---

*[This document was auto-generated from USAGE.template.md; do not edit by hand](https://github.com/fortify/shared-doc-resources/blob/main/USAGE.md)*
