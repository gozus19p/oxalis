# Oxalis (Intercent-ER Implementation)

This repository contains a fork of the [PEPPOL](http://www.peppol.eu/) Access Point, named Oxalis,
which was originally developed by Steinar Overbeck Cook, [SendRegning](http://www.sendregning.no/)
and now looked after by the Norwegian agency for Public Management and eGovernment (Difi).

Original Oxalis repository handled by Difi can be found [here](https://github.com/difi/oxalis).

This repository is handled by Intercent-ER PEPPOL Access Point.

## Modules

| Component | Type | Description |
| --------- | ---- | ----------- |
| oxalis-notier-core | jar | Library that contains the whole set of business logic used in order to interact with NoTI-ER |
| oxalis-notier-integration | jar | Library that contains a set of DTO used by Oxalis and NoTI-ER in order to exchange data using REST web services |
| oxalis-notier-rest-server | jar | Library that contains web services exposed from Oxalis |
| oxalis-quartz   | jar  | Quartz integration that includes Quartz job management |
| oxalis-persist | jar | Library that overrides standard Oxalis persist logic |
| oxalis-rest | jar | Oxalis HTTP client configuration (developed in order to communicate with NoTI-ER) |

This web application threats dependencies against standard Oxalis components.
Please refer to original documentation for those modules.

## Installation

* make sure the latest version of Tomcat is installed. See [installation guide](/doc/installation.md) for additional details.
* make sure that Tomcat is up and running and that manager is available with user manager/manager
* make sure that Tomcat is also up and running on SSL at localhost:443 (unless you terminate SSL in front of Tomcat)
* make sure that ''your'' keystore is installed in a known directory (separate instructions for constructing the keystore)
* Create an `OXALIS_HOME` directory and edit the file `oxalis.conf`
* Add `OXALIS_HOME` environment variable to reference that directory
* Build Oxalis yourself (see below) or download the binary artifacts provided by Difi from [Maven Central](https://search.maven.org)
  Search for "oxalis" and download the latest version of `oxalis-distribution`.
* Deploy `oxalis.war` to your Tomcat `webapps` directory
* Send a sample invoice; modify `example.sh` to your liking and execute it.
* See the [installation guide](/doc/installation.md) for more additional details.
* To install or replace the PEPPOL certificate, see the [keystore document](/doc/keystore.md).
* Oxalis is meant to be extended rather than changing the Oxalis source code.


## Build from source

Note that the Oxalis "head" revision on *master* branch is often in "flux" and should be considered a "nightly build".
The official releases are tagged and may be downloaded by clicking on [Tags](https://github.com/difi/oxalis/tags).

* make sure [Maven 3](http://maven.apache.org/) is installed
* make sure [JDK 8](http://www.oracle.com/technetwork/java/javase/) is installed (the version we have tested with)
* pull the version of interest from [GitHub](https://github.com/difi/oxalis).
* from `oxalis` root directory run : `mvn clean install -Pdist`
* locate assembled artifacts in `oxalis-dist/oxalis-distribution/target/oxalis-distribution-<version.number>-distro/`

