# Fortify SSC Parser Plugin for SARIF

This Fortify SSC parser plugin allows for importing SARIF (Static Analysis Results Interchange Format) files. 

### <a name="related-links">Related Links</a>

* **Downloads**:  
  _Beta versions may be unstable or non-functional. The `*-thirdParty.zip` file is for informational purposes only and does not need to be downloaded._
	* **Release versions**: https://bintray.com/package/files/fortify-ps/binaries/fortify-ssc-parser-sarif-release?order=desc&sort=fileLastModified&basePath=&tab=files  
	* **Beta versions**: https://bintray.com/package/files/fortify-ps/binaries/fortify-ssc-parser-sarif-beta?order=desc&sort=fileLastModified&basePath=&tab=files
	* **Sample input files**: [sampleData](sampleData)
* **GitHub**: https://github.com/fortify-ps/fortify-ssc-parser-sarif
* **Automated builds**: https://travis-ci.com/fortify-ps/fortify-ssc-parser-sarif
* **SARIF resources**:
	* Specification: https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html
	* Microsoft SARIF SDK: https://github.com/microsoft/sarif-sdk

## <a name="limitations">Limitations</a>

The plugin should be able to parse any SARIF files that adhere to the SARIF 2.1.0 specification. Other versions of the
specification are currently not supported. At the moment, the plugin only parses and displays basic issue information.
Future versions of the plugin may display more information like code flows, thread flows, web requests, web responses, ...

Actual results may vary due to the flexibility of the SARIF specification. Some examples:

* The plugin may be unable to calculate consistent, unique issue instance id's because the input file doesn't provide sufficient 
details to uniquely identify an issue
* The plugin may not be able to determine Fortify Priority Order because the input file does not provide issue severity levels
* The plugin may be unable to determine Fortify Priority Order because the input file uses custom properties to specify issue severity
* The plugin may be unable to display appropriate issue category or description because the input file is lacking this information, or 
providing this information in a non-standard way 

Being a generic format, you may have multiple tools generating SARIF files that you want to import into SSC. Due to limitations
in the SSC parser framework, it is currently not possible to import SARIF files from different sources into a single SSC
application version. Independent of which tool was actually used to generate the SARIF file, SSC will assume that all SARIF files 
originate from the scan engine. SSC will try to merge these uploads, thereby basically marking all issues from a previously uploaded
SARIF file as 'removed'.

## <a name="usage">Usage</a>

The following sections describe how to install and use the plugin. For generic information
about how to install and use SSC parser plugins, please see the Fortify SSC documentation.

### <a name="plugin-install--upgrade">Plugin Install & Upgrade</a>

* Obtain the plugin binary jar file
	* Either download from Bintray (see [Related Links](#related-links)) 
	* Or by building yourself (see [Information for plugin developers](#information-for-plugin-developers))
* If you already have another version of the plugin installed, first uninstall the plugin by following the steps in [Plugin Uninstall](#plugin-uninstall)
* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Click the `NEW` button
	* Accept the warning
	* Upload the plugin jar file
	* Enable the plugin by clicking the `ENABLE` button
  
### <a name="plugin-uninstall">Plugin Uninstall</a>

* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Select the parser plugin that you want to uninstall
	* Click the `DISABLE` button
	* Click the `REMOVE` button 

### <a name="obtain-results">Obtain results</a>

Some products provide native support for producing analysis results in SARIF format, these results can be
directly uploaded to SSC. The SARIF MultiTool can be used to convert various other output formats into 
SARIF format. The following example illustrates how to install the SARIF MultiTool, and how to use this
tool to generate SARIF output for the Fortify EightBall example. This example is for Windows only at the 
moment.

* Download & install: https://dotnet.microsoft.com/download/dotnet-core/thank-you/sdk-3.1.201-windows-x64-installer
* Download & install: https://dotnet.microsoft.com/download/dotnet-core/thank-you/runtime-aspnetcore-2.1.17-windows-hosting-bundle-installer
* Run the following command in an Administrator command prompt:  
  `dotnet tool install --global Sarif.Multitool --version 2.2.2`
* Open a new command prompt (to use updated environment variables) and run the following commands:   
```
cd \path\to\Fortify\SCA\Samples\EightBall
sourceanalyzer -b EightBall -clean
sourceanalyzer -b EightBall EightBall.java
sourceanalyzer -b EightBall -scan -f EightBall.fpr
sarif convert EightBall.fpr --tool FortifyFpr --output EightBall.fpr.sarif --pretty-print --force
reportgenerator -format xml -f EightBall.xml -source EightBall.fpr
sarif convert EightBall.xml --tool Fortify --output EightBall.xml.sarif --pretty-print --force
```


### <a name="upload-results">Upload results</a>

SSC web interface (manual upload):

* Navigate to the Artifacts tab of your application version
* Click the `UPLOAD` button
* Click the `ADD FILES` button, and select the JSON file to upload
* Enable the `3rd party results` check box
* Select the `SARIF` type
  
SSC clients (FortifyClient, Maven plugin, ...):

* Generate a scan.info file containing a single line as follows:  
  `engineType=SARIF`
* Generate a zip file containing the following:
	* The scan.info file generated in the previous step
	* The JSON file containing scan results
* Upload the zip file generated in the previous step to SSC
	* Using any SSC client, for example FortifyClient
	* Similar to how you would upload an FPR file



## <a name="information-for-plugin-developers">Information for plugin developers</a>

The following sections provide information that may be useful for developers of this 
parser plugin.

### <a name="ides">IDE's</a>

This project uses Lombok. In order to have your IDE compile this project without errors, 
you may need to add Lombok support to your IDE. Please see https://projectlombok.org/setup/overview 
for more information.

### <a name="gradle">Gradle</a>

It is strongly recommended to build this project using the included Gradle Wrapper
scripts; using other Gradle versions may result in build errors and other issues.

The Gradle build uses various helper scripts from https://github.com/fortify-ps/gradle-helpers;
please refer to the documentation and comments in included scripts for more information. 

### <a name="commonly-used-commands">Commonly used commands</a>

All commands listed below use Linux/bash notation; adjust accordingly if you
are running on a different platform. All commands are to be executed from
the main project directory.

* `./gradlew tasks --all`: List all available tasks
* Build: (plugin binary will be stored in `build/libs`)
	* `./gradlew clean build`: Clean and build the project
	* `./gradlew build`: Build the project without cleaning
	* `./gradlew dist`: Build distribution zip
* Version management:
	* `./gradlew printProjectVersion`: Print the current version
	* `./gradlew startSnapshotBranch -PnextVersion=2.0`: Start a new snapshot branch for an upcoming `2.0` version
	* `./gradlew releaseSnapshot`: Merge the changes from the current branch to the master branch, and create release tag
* `./fortify-scan.sh`: Run a Fortify scan; requires Fortify SCA to be installed

Note that the version management tasks operate only on the local repository; you will need to manually
push any changes (including tags and branches) to the remote repository.

### <a name="versioning">Versioning</a>

The various version-related Gradle tasks assume the following versioning methodology:

* The `master` branch is only used for creating tagged release versions
* A branch named `<version>-SNAPSHOT` contains the current snapshot state for the upcoming release
* Optionally, other branches can be used to develop individual features, perform bug fixes, ...
	* However, note that the Gradle build may be unable to identify a correct version number for the project
	* As such, only builds from tagged versions or from a `<version>-SNAPSHOT` branch should be published to a Maven repository

### <a name="automated-builds--publishing">Automated Builds & publishing</a>

Travis-CI builds are automatically triggered when there is any change in the project repository,
for example due to pushing changes, or creating tags or branches. If applicable, binaries and related 
artifacts are automatically published to Bintray using the `bintrayUpload` task:

* Building a tagged version will result in corresponding release version artifacts to be published
* Building a branch named `<version>-SNAPSHOT` will result in corresponding beta version artifacts to be published
* No artifacts will be deployed for any other build, for example when Travis-CI builds the `master` branch

See the [Related Links](#related-links) section for the relevant Travis-CI and Bintray links.


# <a name="licensing">Licensing</a>
See [LICENSE.TXT](LICENSE.TXT)

