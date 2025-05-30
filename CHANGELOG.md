# Changelog

## [1.7.0](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.6.1...v1.7.0) (2025-05-27)


### Features

* Display line number if available ([2a27588](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/2a27588edc2fe8847f5e3df1c83e52a08646009c))
* Display snippet if available ([2a27588](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/2a27588edc2fe8847f5e3df1c83e52a08646009c))

### [1.6.1](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.6.0...v1.6.1) (2025-05-20)


### Bug Fixes

* Gracefully handle non-existing rule & artifact indexes ([ea84b5a](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/ea84b5a80d0652672e39643a11f3664521011578))

## [1.6.0](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.5.2...v1.6.0) (2025-04-04)


### Features

* Display SARIF tags ([#31](https://www.github.com/fortify/fortify-ssc-parser-sarif/issues/31)) ([f57f3ac](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/f57f3ac7a996b98a108fdfd27dc7f56f53a3cde4))

### [1.5.2](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.5.1...v1.5.2) (2023-09-12)


### Bug Fixes

* Use proper fortify-ssc-parser-util and transitive dependencies" ([2ef9d9b](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/2ef9d9bb3eaeb171b0e8d5bdbe07a70cc9e8af0e))

### [1.5.1](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.5.0...v1.5.1) (2023-09-12)


### Bug Fixes

* Compatibility update for Java 17 / SSC 23.2 ([c4ec141](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/c4ec1417be505e9705677921efe1422ae82abe55))

## [1.5.0](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.4.0...v1.5.0) (2023-07-27)


### Features

* handle SARIF kind ([#19](https://www.github.com/fortify/fortify-ssc-parser-sarif/issues/19)) ([d052119](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/d05211948a66c0fe070abd151ee4935d2597a6ff))


### Bug Fixes

* correct test data file names ([4c6de5d](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/4c6de5d0c7ac9496a5a4be1244c6ab0e0c5b0091))

## [1.4.0](https://www.github.com/fortify/fortify-ssc-parser-sarif/compare/v1.3.0...v1.4.0) (2023-03-03)


### Features

* if rule.name contains spaces, don't treat as camel case ([3a4b198](https://www.github.com/fortify/fortify-ssc-parser-sarif/commit/3a4b198e92a2927fdc72baab7e4afd348ba2dfac))

## [1.3.0](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/compare/v1.2.4...v1.3.0) (2022-10-21)


### Features

* display help if available ([c1d42f6](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/c1d42f6913b6a5927ea668ccb193feeba683fe17))
* display help uri if available ([19fb073](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/19fb073abe0cc9c607a617da617e21455859b8f8))
* use location.message if available as the file name ([029b680](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/029b680d0ede805195c75423596fef66c4aead2d))
* use security-severity property to determine priority ([ff10a65](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/ff10a65d4812d470bde2b28e204990b535355584))
* use shortDescription if available as category ([a78bc35](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/a78bc353022334bfbd3728e1f9484761b61476c0))

### [1.2.4](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/compare/v1.2.3...v1.2.4) (2022-06-14)


### Bug Fixes

* Allow SARIF to be imported on SSC 22.1+ ([1e84ae3](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/1e84ae3545a76e8fa1fdd1f460511cfabd873a97))

### [1.2.3](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/compare/v1.2.2...v1.2.3) (2022-05-03)


### Bug Fixes

* Add missing changelog entry in previous release ([15449c6](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/15449c69818193278263d15e8654d0112b346cd3))
* Update (potentially vulnerable) dependency versions ([3bb57d7](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/3bb57d731bfd18957f3d15adf742cf8a976042c6))

### [1.2.2](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/compare/v1.2.1...v1.2.2) (2022-05-03)


### Bug Fixes

* Improve upload instructions in README.md ([1fd84b9](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/1fd84b93cf7e585b862d9458de10327a680a0516))

### 1.2.1 (2021-03-11)


### Features

* No functional changes ([5bdbb4d](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/5bdbb4d19a33e9cb76e4b2d219bb391606c84c57))
* Releases are now automatically published to GitHub Releases page ([5bdbb4d](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/5bdbb4d19a33e9cb76e4b2d219bb391606c84c57))
* Update build & release process ([5bdbb4d](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/5bdbb4d19a33e9cb76e4b2d219bb391606c84c57))
* Update dependencies ([5bdbb4d](https://www.github.com/fortify-ps/fortify-ssc-parser-sarif/commit/5bdbb4d19a33e9cb76e4b2d219bb391606c84c57))
