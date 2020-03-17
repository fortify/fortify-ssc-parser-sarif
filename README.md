# WORK IN PROGRESS
This parser plugin is work in progress and far from complete.

# Fortify SSC parser plugin for SARIF input files

This project provides an SSC parser plugin for parsing SARIF input files.

## Usage instructions

* Download/clone the source code
* Run `gradle build` in the project directory
* Install and enable the plugin jar in SSC; the plugin jar is available in the 
  `build/libs` directory after running `gradle build`
* Upload the `src/test/resources/example1.sarif` file as an
  artifact to an SSC application version, selecting the `3rd party results` 
  checkbox and `SARIF` as the scan type in the SSC artifact upload 
  dialog

# Licensing

See [LICENSE.TXT](LICENSE.TXT)

