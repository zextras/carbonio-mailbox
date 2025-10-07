FROM maven:3-eclipse-temurin-17-alpine AS dependencies

RUN apk add --no-cache make gcc g++ musl-dev
WORKDIR /build
# Copy all project files
COPY pom.xml                                  pom.xml
COPY store                            store
COPY native                           native
COPY attribute-manager                attribute-manager
COPY mailbox-ldap-lib                 mailbox-ldap-lib
COPY soap                             soap
COPY store-extra-runtime-dependencies store-extra-runtime-dependencies
COPY jython-libs                      jython-libs
COPY common                           common
COPY right-manager                    right-manager
COPY client                           client
COPY store-conf                       store-conf

RUN mvn clean install -DskipTests

FROM maven:3-eclipse-temurin-17-alpine AS builder

RUN apk add --no-cache make gcc g++ musl-dev

WORKDIR /build
ARG MAVEN_OPTS=""
COPY --from=dependencies /root/.m2 /root/.m2
COPY milter-conf milter-conf
COPY packages packages
COPY --from=dependencies /build .
#RUN sh -c 'export MAVEN_OPTS="${MAVEN_OPTS}" && mvn install -DskipTests && mkdir -p out'
RUN mkdir /out
RUN cp -a store* milter* attribute-manager right-manager \
    mailbox-ldap-lib native client common packages soap jython-libs \
    /out/
RUN tar -czf /out/mailbox.tar.gz -C ./ .

FROM scratch AS package

COPY --from=builder /out/mailbox.tar.gz .

FROM maven:3-eclipse-temurin-17-alpine AS test
RUN apk add --no-cache make gcc g++ musl-dev

WORKDIR /app

COPY --from=dependencies /root/.m2 /root/.m2
COPY --from=dependencies /build .

RUN mvn verify

