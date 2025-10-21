# Carbonio Mailbox

![Contributors](https://img.shields.io/github/contributors/zextras/carbonio-mailbox "Contributors") ![Activity](https://img.shields.io/github/commit-activity/m/zextras/carbonio-mailbox "Activity") ![License](https://img.shields.io/badge/license-GPL%202-green
"License") ![Project](https://img.shields.io/badge/project-carbonio-informational
"Project") [![Twitter](https://img.shields.io/twitter/url/https/twitter.com/zextras.svg?style=social&label=Follow%20%40zextras)](https://twitter.com/zextras)

This repository contains the source code of Carbonio Mailbox which consists of several subcomponents
and their roles:


- **common**: provides classes of common use (utilities, constants, etc.)
- **client**: client package to interact with the mailbox
- **soap**: describes SOAP APIs and contains wsdl documentation
- **store**: the mailbox service (API handlers, Milter, IMAP, POP3, CLI and others)

## Building Carbonio Mailbox from source
Requirements:
- JDK version 17. Confirm by running: `javac -version`
- gcc to build `native` module

Build the Code by running:
   ```shell
   $ mvn clean install -DskipTests
   ```

## Generating SOAP DOCS
> mvn antrun:run@generate-soap-docs

## Local Mailbox (Development)

Run the main class [SampleLocalMailbox](store/src/test/java/com/zextras/mailbox).  
This will start a Mailbox with in-memory LDAP and an HSQLDB database.

## RC
Managed with **Github Actions**.

## Contribute to Carbonio Mailbox

All contributions are accepted! Please refer to the CONTRIBUTING file (if present in this repository)
for more detail on how to contribute. If the repository has a Code of Conduct, 
we kindly ask to follow that as well.


## License(s)

See [COPYING](COPYING) file for detail.
