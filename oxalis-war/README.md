# Oxalis Web Archive (Oxalis WAR)

This module has been created in order to provide a web archive version of Oxalis.
This is compiled using Maven.

## Maven profiles (based on content)

Several profiles has been created in order to customize the WAR content:

| Profile |  Active by default | Description |
| ------- |  ----------- | ----------- |
| complete-build | **true** | Provides full build of Oxalis. No exclusion strategy is adopted here |
| complete-no-as4 |  false | Excludes AS4 plugin from inbound and outbound scenarios |
| inbound-as4 | false |  Provides an exclusive AS4 inbound version of Oxalis; this one excludes Quartz integration  as well as AS2 inbound module |
| inbound-as2 | false | Provide an exclusive AS2 inbound version of Oxalis; this one excludes Quartz integration  as well as AS4 inbound module |

Different profiles has been created in order to provide basic functionalities needed to build an Oxalis AS4 inbound only as well as a complete Oxalis build.
The principal cause of this is related to this [issue](https://github.com/difi/Oxalis-AS4/issues/65) handled by Difi in original Oxalis repository.

The final result will contain a Maven profile reference inside its file name.

## Maven profiles (based on environment) 

Despite original Oxalis logic, we decided to keep exernal configuration of Oxalis Mode.

| Profile |  Active by default | Description |
| ------- |  ----------- | ----------- |
| test | **true** | Provides an Oxalis WAR working in *test* environment |
| prod |  false | Provides an Oxalis WAR working in *production* environment |

If no profile is specified, *test* will be used.