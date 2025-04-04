# Fortify SSC Parser Plugin for SARIF 


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



<!-- START-INCLUDE:h2.support.md -->

## Support

For general assistance, please join the [Fortify Community](https://community.opentext.com/cybersec/fortify/) to get tips and tricks from other users and the OpenText team.
 
OpenText customers can contact our world-class [support team](https://www.opentext.com/support/opentext-enterprise/) for questions, enhancement requests and bug reports. You can also raise questions and issues through your OpenText Fortify representative like Customer Success Manager or Technical Account Manager if applicable.

You may also consider raising questions or issues through the [GitHub Issues page](https://github.com/fortify/fortify-ssc-parser-sarif/issues) (if available for this repository), providing public visibility and allowing anyone (including all contributors) to review and comment on your question or issue. Note that this requires a GitHub account, and given public visibility, you should refrain from posting any confidential data through this channel. 

<!-- END-INCLUDE:h2.support.md -->


---

*[This document was auto-generated from README.template.md; do not edit by hand](https://github.com/fortify/shared-doc-resources/blob/main/USAGE.md)*
