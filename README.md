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

# Fortify SSC Parser Plugin for SARIF

## Introduction

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
    * The plugin may be unable to calculate consistent, unique issue instance id's because the input file doesn't provide sufficient 
details to uniquely identify an issue
    * The plugin may not be able to determine Fortify Priority Order because the input file does not provide issue severity levels
    * The plugin may be unable to determine Fortify Priority Order because the input file uses custom properties to specify issue severity
    * The plugin may be unable to display appropriate issue category or description because the input file is lacking this information, or 
providing this information in a non-standard way 

* **SARIF results from multiple tools cannot be uploaded to single SSC application version**  
  Being a generic format, you may have multiple tools generating SARIF files that you want to import into SSC. Due to limitations
  in the SSC parser framework, it is currently not possible to import SARIF files from different sources into a single SSC
  application version. Independent of which tool was actually used to generate the SARIF file, SSC will assume that all SARIF files 
  originate from the same scan engine. SSC will try to merge these uploads, thereby basically marking all issues from a previously uploaded
  SARIF file as 'removed'.

### Related Links

* **Downloads**: https://github.com/fortify-ps/fortify-ssc-parser-sarif/releases
    * _Development releases may be unstable or non-functional. The `*-thirdparty.zip` file is for informational purposes only and does not need to be downloaded._
* **Sample input files**: [sampleData](sampleData)
* **GitHub**: https://github.com/fortify-ps/fortify-ssc-parser-sarif
* **Automated builds**: https://github.com/fortify-ps/fortify-ssc-parser-sarif/actions
* **SARIF resources**:
	* Specification: https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html
	* Microsoft SARIF SDK: https://github.com/microsoft/sarif-sdk  
	Note that this SDK is not used by the parser plugin, but it provides some useful examples

## Plugin Installation

These sections describe how to install, upgrade and uninstall the plugin.

### Install & Upgrade

* Obtain the plugin binary jar file
	* Either download from Bintray (see [Related Links](#related-links)) 
	* Or by building yourself (see [Developers](#developers))
* If you already have another version of the plugin installed, first uninstall the previously 
 installed version of the plugin by following the steps under [Uninstall](#uninstall) below
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


## Upload results

As a 3<sup>rd</sup>-party results zip bundle:

* Generate a scan.info file containing a single line as follows:  
`engineType=SARIF`
* Generate a zip file containing the following:
	* The scan.info file generated in the previous step
	* The JSON file containing scan results
* Upload the zip file generated in the previous step to SSC
	* Using any SSC client, for example FortifyClient or Maven plugin
	* Or using the SSC web interface
	* Similar to how you would upload an FPR file

As raw scan results:  

* Navigate to the Artifacts tab of your application version
* Click the `UPLOAD` button
* Click the `ADD FILES` button, and select the JSON file to upload
* Enable the `3rd party results` check box
* Select the `SARIF` type

*Note that uploading raw scan results is only supported for manual uploads through the SSC web interface, and this functionality was removed in SSC 20.2 so no longer available in recent SSC versions. Please submit a feature request if you'd like to see this easier process for ad-hoc uploading of 3<sup>rd</sup>-party results restored, referencing Octane id #448174.*

## Developers

The following sections provide information that may be useful for developers of this utility.

### IDE's

This project uses Lombok. In order to have your IDE compile this project without errors, 
you may need to add Lombok support to your IDE. Please see https://projectlombok.org/setup/overview 
for more information.

### Gradle Wrapper

It is strongly recommended to build this project using the included Gradle Wrapper
scripts; using other Gradle versions may result in build errors and other issues.

The Gradle build uses various helper scripts from https://github.com/fortify-ps/gradle-helpers;
please refer to the documentation and comments in included scripts for more information. 

### Common Commands

All commands listed below use Linux/bash notation; adjust accordingly if you
are running on a different platform. All commands are to be executed from
the main project directory.

* `./gradlew tasks --all`: List all available tasks
* Build: (plugin binary will be stored in `build/libs`)
	* `./gradlew clean build`: Clean and build the project
	* `./gradlew build`: Build the project without cleaning
	* `./gradlew dist distThirdParty`: Build distribution zip and third-party information bundle
* `./fortify-scan.sh`: Run a Fortify scan; requires Fortify SCA to be installed

### Automated Builds

This project uses GitHub Actions workflows to perform automated builds for both development and production releases. All pushes to the main branch qualify for building a production release. Commits on the main branch should use [Conventional Commit Messages](https://www.conventionalcommits.org/en/v1.0.0/); it is recommended to also use conventional commit messages on any other branches.

User-facing commits (features or fixes) on the main branch will trigger the [release-please-action](https://github.com/google-github-actions/release-please-action) to automatically create a pull request for publishing a release version. This pull request contains an automatically generated CHANGELOG.md together with a version.txt based on the conventional commit messages on the main branch. Merging such a pull request will automatically publish the production binaries and Docker images to the locations described in the [Related Links](#related-links) section.

Every push to a branch in the GitHub repository will also automatically trigger a development release to be built. By default, development releases are only published as build job artifacts. However, if a tag named `dev_<branch-name>` exists, then development releases are also published to the locations described in the [Related Links](#related-links) section. The `dev_<branch-name>` tag will be automatically updated to the commit that triggered the build.


## License
<x-insert text="<!--"/>

See [LICENSE.TXT](LICENSE.TXT)

<x-insert text="-->"/>

<x-include url="file:LICENSE.TXT"/>

