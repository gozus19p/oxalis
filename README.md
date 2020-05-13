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

*To be defined*.

## Build from source

*To be defined*.
