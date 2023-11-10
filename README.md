# Carbonio Mailbox

![Contributors](https://img.shields.io/github/contributors/zextras/carbonio-mailbox "Contributors") ![Activity](https://img.shields.io/github/commit-activity/m/zextras/carbonio-mailbox "Activity") ![License](https://img.shields.io/badge/license-AGPL%203-green
"License") ![Project](https://img.shields.io/badge/project-carbonio-informational
"Project") [![Twitter](https://img.shields.io/twitter/url/https/twitter.com/zextras.svg?style=social&label=Follow%20%40zextras)](https://twitter.com/zextras)

Mailbox is the core component of Carbonio.

This repository contains the source code of Carbonio Mailbox which consists of several subcomponents and their roles are as follows

- **client**: client package to interact with the mailbox
- **common**: package providing classes of common use, like utilities, clients and common parameters
- **native**: package to load native libraries
- **soap**: package describing SOAP APIs and tools to generate wsdl documentation
- **store**: WAR package that defines the service. It also includes core functionalities like SOAP APIs, LDAP, Krb5, IMAP, POP3 and CLI functions

## Building Carbonio Mailbox from source

- Build Requirements:
  - JDK version 11, to confirm run `javac -version`
- Clone the carbonio-mailbox repository:
  `git clone https://github.com/Zextras/carbonio-mailbox.git`
- Enter into source directory:
  `cd carbonio-mailbox/`
- Build the code:
 `mvn install`


## Run Carbonio Mailbox locally (for Development)
Following guide provides two different ways to run Carbonio Mailbox locally for Development:

### 1. Full Carbonio Mailbox
This option compiles the code, packages the artifacts, installs all the built Carbonio Mailbox
packages, and then starts the services.

1. Build the project:
   ```shell
   $ mvn install -DskipTests
   ```
2. Build packages using [build_packages.sh](build_packages.sh):
    ```shell
    $ ./build_packages.sh
    ```
3. Enter in [docker/single-server](docker/single-server) directory:
    ```shell
    $ cd ./docker/single-server
    ```
4. Run it:
   ```shell
   $ docker compose up
   ```

### 2. Minimal Jetty Instance
This option compiles the code and launches the mailbox with a minimal setup, ideal for developing
and testing SOAP and REST APIs exposed by Mailbox.

1. Build the project using local mvn command (skipping tests `-DskipTests`):
   ```shell
   $ mvn install -DskipTests
   ```
   or build the project using docker container:
   ```shell
   $ docker compose -f './docker/jetty-run/docker-compose.yml' run --rm build
   ```
2. Enter in [docker/jetty-run](docker/jetty-run) directory:
    ```shell
    $ cd ./docker/jetty-run
    ```
3. Run it:
    ```shell
    $ docker compose up
    ```

## Contribute to Carbonio Mailbox

All contributions are accepted! Please refer to the CONTRIBUTING file (if present in this repository) for more details on how to contribute. If the repository has a Code of Conduct, we kindly ask to follow that as well.

## License

See [COPYING](COPYING-AGPL-3.0-only) file for details
