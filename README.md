# Carbonio Mailbox

![Contributors](https://img.shields.io/github/contributors/zextras/carbonio-mailbox "Contributors") ![Activity](https://img.shields.io/github/commit-activity/m/zextras/carbonio-mailbox "Activity") ![License](https://img.shields.io/badge/license-GPL%202-green
"License") ![Project](https://img.shields.io/badge/project-carbonio-informational
"Project") [![Twitter](https://img.shields.io/twitter/url/https/twitter.com/zextras.svg?style=social&label=Follow%20%40zextras)](https://twitter.com/zextras)

This repository contains the source code of Carbonio Mailbox which consists of several subcomponents
and their roles:

- **client**: client package to interact with the mailbox
- **common**: package providing classes of common use, like utilities, clients and common parameters
- **native**: package to load native libraries
- **soap**: package describing SOAP APIs and tools to generate wsdl documentation
- **store**: the mailbox service and servlets (SOAP APIs, IMAP, POP3 and CLI)

## Building Carbonio Mailbox from source
Requirements:
- JDK version 17. Confirm by running: `javac -version`
- gcc to build `native` module

Build the Code by running:
   ```shell
   $ mvn clean install -DskipTests
   ```

## Adding an attribute to LDAP
- add the attribute definition in [attrs.xml](store/src/main/resources/conf/attrs/attrs.xml)  
- generate attributes and rights by running `mvn 
  antrun:run@generate-zattr-rights`
- add the migration files by following the instructions in [updates](store/ldap/src/updates/attrs/ReadMe.md)
- update the tests in AttributeManagerTest.java that check on the attributes 
  count number
## Generating Rights and ZAttr classes
Whenever you make changes to [attrs.xml](store/src/main/resources/conf/attrs/attrs.xml)
you can generate rights and ZAttr* classes by running:
> mvn antrun:run@generate-zattr-rights

## Generating SOAP DOCS
> mvn antrun:run@generate-soap-docs

## Local Mailbox (Development)

### 1. Mailbox in Container
See [docker/standalone/README.md](docker/standalone/README.md)

### 2. Local Sample Mailbox (java main)
Run the main class [SampleLocalMailbox](store/src/test/java/com/zextras/mailbox).  
This will start a Mailbox with in-memory LDAP and an HSQLDB database.


## RC
Release is managed with [release-it](https://github.com/release-it/release-it).

1. Install the dependencies with `npm i`.
2. Create a branch "rc"
3. Run `release-it --ci`. This will bump the versions, commit, tag and push the
   code.
4. The make sure the tag was built. This will deliver the RC.
5. Finalize the work by merging the RC in the main branch.

## Contribute to Carbonio Mailbox

All contributions are accepted! Please refer to the CONTRIBUTING file (if present in this repository)
for more detail on how to contribute. If the repository has a Code of Conduct, 
we kindly ask to follow that as well.


## License(s)

See [COPYING](COPYING) file for detail.
