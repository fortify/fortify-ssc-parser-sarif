# Fortify SSC Parser Plugin for SARIF 


<!-- START-INCLUDE:p.marketing-intro.md -->

Build secure software fast with [Fortify](https://www.microfocus.com/en-us/solutions/application-security). Fortify offers end-to-end application security solutions with the flexibility of testing on-premises and on-demand to scale and cover the entire software development lifecycle.  With Fortify, find security issues early and fix at the speed of DevOps. 

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


## Resources


<!-- START-INCLUDE:repo-resources.md -->

* **Usage**: [USAGE.md](USAGE.md)
* **Releases**: https://github.com/fortify/fortify-ssc-parser-sarif/releases
    * _Development releases may be unstable or non-functional. The `*-thirdparty.zip` file is for informational purposes only and does not need to be downloaded._
* **Sample input files**: [sampleData](sampleData)
* **Source code**: https://github.com/fortify/fortify-ssc-parser-sarif
* **Automated builds**: https://github.com/fortify/fortify-ssc-parser-sarif/actions
* **Contributing Guidelines**: [CONTRIBUTING.md](CONTRIBUTING.md)
* **Code of Conduct**: [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
* **License**: [LICENSE.txt](LICENSE.txt)
* **SARIF resources**:
	* Specification: https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html
	* Microsoft SARIF SDK: https://github.com/microsoft/sarif-sdk  
	Note that this SDK is not used by the parser plugin, but it provides some useful examples

<!-- END-INCLUDE:repo-resources.md -->


## Support

The software is provided "as is", without warranty of any kind, and is not supported through the regular Micro Focus Support channels. Support requests may be submitted through the [GitHub Issues](https://github.com/fortify/fortify-ssc-parser-sarif/issues) page for this repository. A (free) GitHub account is required to submit new issues or to comment on existing issues. 

Support requests created through the GitHub Issues page may include bug reports, enhancement requests and general usage questions. Please avoid creating duplicate issues by checking whether there is any existing issue, either open or closed, that already addresses your question, bug or enhancement request. If an issue already exists, please add a comment to provide additional details if applicable.

Support requests on the GitHub Issues page are handled on a best-effort basis; there is no guaranteed response time, no guarantee that reported bugs will be fixed, and no guarantee that enhancement requests will be implemented. If you require dedicated support for this and other Fortify software, please consider purchasing Micro Focus Fortify Professional Services. Micro Focus Fortify Professional Services can assist with general usage questions, integration of the software into your processes, and implementing customizations, bug fixes, and feature requests (subject to feasibility analysis). Please contact your Micro Focus Sales representative or fill in the [Professional Services Contact Form](https://www.microfocus.com/en-us/cyberres/contact/professional-services) to obtain more information on pricing and the services that Micro Focus Fortify Professional Services can provide.

---

*This document was auto-generated from README.template.md; do not edit by hand*
