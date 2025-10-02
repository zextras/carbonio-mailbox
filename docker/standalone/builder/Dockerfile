FROM maven:3-eclipse-temurin-17-alpine AS builder

RUN apk add --no-cache make gcc g++ musl-dev

WORKDIR /build
ARG MAVEN_OPTS=""

# Copy all project files
COPY . .

RUN sh -c 'export MAVEN_OPTS="${MAVEN_OPTS}" && mvn clean install -DskipTests && mkdir -p out'
RUN cp -a store* milter* attribute-manager right-manager \
    mailbox-ldap-lib native client common packages soap jython-libs \
    out/

RUN tar -czf mailbox.tar.gz -C ./out .