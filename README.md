# Carbonio Mailbox

![Contributors](https://img.shields.io/github/contributors/zextras/carbonio-mailbox "Contributors") ![Activity](https://img.shields.io/github/commit-activity/m/zextras/carbonio-mailbox "Activity") ![License](https://img.shields.io/badge/license-AGPL%203-green
"License") ![Project](https://img.shields.io/badge/project-carbonio-informational
"Project") [![Twitter](https://img.shields.io/twitter/url/https/twitter.com/zextras.svg?style=social&label=Follow%20%40zextras)](https://twitter.com/zextras)

Mailbox is the core component of Carbonio.

This repository contains the source code of Carbonio Mailbox which consists of several subcomponents and their roles are as follows

- **client**: package to interact with the mailbox
- **common**: package providing classes of common use, like utilities, clients and common parameters
- **native**: package to load native libraries
- **soap**: package describing SOAP APIs
- **store**: package with core implementations and integrations like SOAP APIs, LDAP, Krb5, IMAP, POP3 and CLI functions

## Building Carbonio Mailbox from source
- Build Requirements:
	 - JDK version 11, to confirm run `javac -version`
	 - ant
	 - apache-ant-contrib


- Clone the carbonio-mailbox repository:

    `git clone https://github.com/Zextras/carbonio-mailbox.git`

- Enter into source directory:

    `cd carbonio-mailbox/`

- Build the code:

	`ant all -Dcarbonio.buildinfo.version=22.6.1_ZEXTRAS_202206`

	Tips:
	- You can list all ant build targets present in build.xml file using `ant -p build.xml`
	- You can avoid passing `carbonio.buildinfo.version` on every time you invoke ant build command by exporting it as ANT_OPTS environment varibale with `export ANT_OPTS="-Dcarbonio.buildinfo.version=22.6.1_ZEXTRAS_202206"`


## Contribute to Carbonio Mailbox
All contributions are accepted! Please refer to the CONTRIBUTING file (if present in this repository) for more details on how to contribute. If the repository has a Code of Conduct, we kindly ask to follow that as well.

## License

See [COPYING](COPYING) file for details
