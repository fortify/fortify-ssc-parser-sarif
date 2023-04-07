# Contributing to Fortify SSC Parser Plugin for SARIF

## Contribution Agreement

Contributions like bug fixes and enhancements may be submitted through Pull Requests on this repository. Before we can accept 3<sup>rd</sup>-party pull requests, you will first need to sign and submit the [Contribution Agreement](https://github.com/fortify/repo-resources/raw/main/static/Open%20Source%20Contribution%20Agreement%20Jan2020v1.pdf). Please make sure to mention your GitHub username when submitting the form, to allow us to verify that the author of a pull request has accepted this agreement. 


<!-- START-INCLUDE:repo-devinfo.md -->


<!-- START-INCLUDE:devinfo/h2.standard-parser-devinfo.md -->

## Information for Developers

The following sections provide information that may be useful for developers of this parser plugin.


<!-- START-INCLUDE:devinfo/h3.release-please.md -->

### Conventional commits & versioning

Versioning is handled automatically by [`release-please-action`](https://github.com/google-github-actions/release-please-action) based on [Conventional Commits](https://www.conventionalcommits.org/). Every commit to the `main`
branch should follow the Conventional Commits convention. Following are some examples; these can be combined in a single commit message (separated by empty lines), or you can have commit messages describing just a single fix or feature.

```
chore: Won't show up in changelog

ci: Change to GitHub Actions workflow; won't show up in changelog

docs: Change to documentation; won't show up in changelog

fix: Some fix (#2)

feat: New feature (#3)

feat!: Some feature that breaks backward compatibility

feat: Some feature
  BREAKING-CHANGE: No longer supports xyz
```

See the output of `git log` to view some sample commit messages.

`release-please-action` invoked from the GitHub CI workflow generates pull requests containing updated `CHANGELOG.md` and `version.txt` files based on these commit messages. Merging the pull request will result in a new release version being published; this includes publishing the image to Docker Hub, and creating a GitHub release describing the changes.

<!-- END-INCLUDE:devinfo/h3.release-please.md -->



<!-- START-INCLUDE:devinfo/h3.lombok.md -->

### Lombok

This project uses Lombok. Gradle builds will automatically handle Lombok annotations, but to have your IDE compile this project without errors, you may need to add Lombok support to your IDE. Please see https://projectlombok.org/setup/overview for more information.

<!-- END-INCLUDE:devinfo/h3.lombok.md -->



<!-- START-INCLUDE:devinfo/h3.gradle-wrapper.md -->

### Gradle Wrapper

It is strongly recommended to build this project using the included Gradle Wrapper scripts; using other Gradle versions may result in build errors and other issues.

<!-- END-INCLUDE:devinfo/h3.gradle-wrapper.md -->



<!-- START-INCLUDE:devinfo/p.gradle-helpers.md -->

The Gradle build uses various helper scripts from https://github.com/fortify-ps/shared-gradle-helpers; please refer to the documentation and comments in included scripts for more information. 

<!-- END-INCLUDE:devinfo/p.gradle-helpers.md -->


### Common Commands

All commands listed below use Linux/bash notation; adjust accordingly if you are running on a different platform. All commands are to be executed from the main project directory.

* `./gradlew tasks --all`: List all available tasks
* Build: (plugin binary will be stored in `build/libs`)
	* `./gradlew clean build`: Clean and build the project
	* `./gradlew build`: Build the project without cleaning
	* `./gradlew dist distThirdParty`: Build distribution zip and third-party information bundle
* `./fortify-scan.sh`: Run a Fortify scan; requires Fortify SCA to be installed

<!-- END-INCLUDE:devinfo/h2.standard-parser-devinfo.md -->


<!-- END-INCLUDE:repo-devinfo.md -->


---

*[This document was auto-generated from CONTRIBUTING.template.md; do not edit by hand](https://github.com/fortify/shared-doc-resources/blob/main/USAGE.md)*
