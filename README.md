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
- **store**: WAR package that defines the service. It also includes core functionalities like SOAP
APIs, LDAP, Krb5, IMAP, POP3 and CLI functions

## Generating Rights and ZAttr classes
Whenever you make changes to [store/conf/attrs/attrs.xml](store/conf/attrs/attrs.xml)
you can generate rights and ZAttr* classes by running:
> mvn antrun:run@generate-zattr-rights

## Generating SOAP DOCS
> mvn antrun:run@generate-soap-docs

## Building Carbonio Mailbox from source

### 1. Build using local mvn command

1. Build Requirements:
Ensure you have JDK version 11 installed. Confirm by running:
   ```shell
   $ javac -version
   ```
  
2. Clone the Carbonio Mailbox Repository:
   ```shell
   $ git clone https://github.com/Zextras/carbonio-mailbox.git
   ```

3. Navigate to the Source Directory:
   ```shell
   $ cd carbonio-mailbox/
   ```

4. Build the Code:
   ```shell
   $ mvn install
   ```

### 2. Build the source code inside a docker container
```shell
$ UID=${UID} GID=${GID} docker compose -f './docker/jetty-run/docker-compose.yml' run --rm build
```

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
   The following commands will build the packages for ubuntu-focal, see 
   [build_packages.sh](build_packages.sh) for other supported targets.
   ```shell
    $ ./build_packages.sh ubuntu-focal
    ```
3. Navigate to the [docker/single-server](docker/single-server) directory:
    ```shell
    $ cd ./docker/single-server
    ```
4. Start Carbonio-CE `Ubuntu Focal` docker image with built Carbonio Mailbox packages:
   ```shell
   $ docker compose --profile ubuntu-focal up --build
   ```
   **NB**: don't forget to use the `--build` flag, otherwise it will not load the new changes

### 2. Minimal Jetty Instance
This option compiles the code and launches the mailbox with a minimal setup, ideal for developing
and testing SOAP and REST APIs exposed by Mailbox.

1. Build the project using local mvn command (skipping tests `-DskipTests`):
   ```shell
   $ mvn install -DskipTests
   ```
   or build the project using docker container:
   ```shell
   $ UID=${UID} GID=${GID} docker compose -f './docker/jetty-run/docker-compose.yml' run --rm build
   ```
2. Navigate to the [docker/jetty-run](docker/jetty-run) directory:
    ```shell
    $ cd ./docker/jetty-run
    ```
3. Run it:
    ```shell
    $ docker compose up
    ```
4. Check if the mailbox is up and running:
   ```shell
   $ curl localhost:7070/service/health
    ```
## Contribute to Carbonio Mailbox

All contributions are accepted! Please refer to the CONTRIBUTING file (if present in this repository)
for more detail on how to contribute. If the repository has a Code of Conduct, 
we kindly ask to follow that as well.

## License(s)

See [COPYING](COPYING) file for detail.
