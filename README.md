# Carbonio Mailbox

![Contributors](https://img.shields.io/github/contributors/zextras/carbonio-mailbox "Contributors") ![Activity](https://img.shields.io/github/commit-activity/m/zextras/carbonio-mailbox "Activity") ![License](https://img.shields.io/badge/license-GPL%202-green
"License") ![Project](https://img.shields.io/badge/project-carbonio-informational
"Project") [![Twitter](https://img.shields.io/twitter/url/https/twitter.com/zextras.svg?style=social&label=Follow%20%40zextras)](https://twitter.com/zextras)

Mailbox is the core component of Carbonio.

This repository contains the source code of Carbonio Mailbox which consists of several subcomponents
and their roles:

- **client**: client package to interact with the mailbox
- **common**: package providing classes of common use, like utilities, clients and common parameters
- **native**: package to load native libraries
- **soap**: package describing SOAP APIs and tools to generate wsdl documentation
- **store**: the real mailbox service (SOAP APIs, IMAP, POP3 and CLI)

## Building Carbonio Mailbox from source

### 1. Build using local mvn command

1. Requirements:
- JDK version 17. Confirm by running:
   ```shell
   $ javac -version
   ```
- gcc to build `native` module

2. Build the Code by running:
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

## Run Carbonio Mailbox locally (for Development)
Following guide provides two different ways to run Carbonio Mailbox locally for Development:

### 1. Dev Mailbox in Container
See [docker/standalone/README.md](docker/standalone/README.md)

### 2. Local Sample Mailbox
Run the `com.zextras.mailbox.SampleLocalMailbox` main class in store module,
test directory.
This will start a Mailbox with in-memory LDAP and an HSQLDB database.

## Contribute to Carbonio Mailbox

All contributions are accepted! Please refer to the CONTRIBUTING file (if present in this repository)
for more detail on how to contribute. If the repository has a Code of Conduct, 
we kindly ask to follow that as well.

# RC

To publish packages in RC:
- Make sure [release-it](https://github.com/release-it/release-it) is 
  installed or install it by running `npm install`
- Update [pom.xml](pom.xml) version according to needs
- run `npm run release` accepting to commit, tag and push
It will result in CI building the tag and releasing the packages to RC channel.



## License(s)

See [COPYING](COPYING) file for detail.
