FROM maven:3-eclipse-temurin-17-alpine AS build
RUN apk add --no-cache make gcc g++ musl-dev

WORKDIR /build

COPY pom.xml                          pom.xml
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

RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests

FROM ubuntu:jammy

USER root
RUN apt update && apt install -y gnupg2 ca-certificates && apt clean \
&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 52FD40243E584A21 \
&& echo deb https://repo.zextras.io/release/ubuntu jammy main > /etc/apt/sources.list.d/zextras.list \
&& apt update && apt install -y carbonio-openldap netcat && apt clean

ENV LDAP_ROOT_PASSWORD=qh6hWZvc
ENV LDAP_ADMIN_PASSWORD=password
EXPOSE 1389

COPY docker/standalone/openldap/ldap-utils/ /ldap-utils/
COPY --from=build /build/mailbox-ldap-lib/ldap/generated/ /opt/zextras/common/etc/openldap/zimbra/
RUN mkdir -p /opt/zextras/ldap/custom-ldifs \
            /opt/zextras/data/ldap/state/run \
            /opt/zextras/data/ldap/config \
            /opt/zextras/data/ldap/mdb/db \
            /run/carbonio \
            /opt/zextras/conf/ca

COPY docker/standalone/openldap/entrypoint.sh .
ENTRYPOINT ["./entrypoint.sh"]