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
