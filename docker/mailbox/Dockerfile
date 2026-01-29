FROM --platform=$BUILDPLATFORM maven:3-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy all project files
COPY pom.xml                          pom.xml
COPY store                            store
COPY soap                             soap
COPY store-extra-runtime-dependencies store-extra-runtime-dependencies
COPY jython-libs                      jython-libs
COPY common                           common
COPY right-manager                    right-manager
COPY client                           client

RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests

# Prepare directory structure and scripts in build stage
RUN mkdir -p /staging/opt/zextras/mailbox/jars/ && \
    mkdir -p /staging/opt/zextras/lib/ext/nginx-lookup/ && \
    mkdir -p /staging/opt/zextras/store && \
    mkdir -p /staging/opt/zextras/mailboxd/work && \
    mkdir -p /staging/opt/zextras/log && \
    mkdir -p /staging/opt/zextras/db && \
    mkdir -p /staging/opt/zextras/mailboxd/etc/

# Copy built artifacts to staging
RUN cp /build/store/target/zm-store.jar /staging/opt/zextras/mailbox/jars/mailbox.jar && \
    cp -r /build/store/target/dependencies/* /staging/opt/zextras/mailbox/jars/ && \
    mkdir -p /staging/opt/zextras/conf/msgs && \
    cp /build/store/src/test/resources/timezones-test.ics /staging/opt/zextras/conf/timezones.ics && \
    cp /build/store/conf/msgs/* /staging/opt/zextras/conf/msgs/ && \
    cp /build/store/conf/magic /staging/opt/zextras/conf/magic && \
    cp /build/store/conf/magic.zimbra /staging/opt/zextras/conf/magic.zimbra && \
    cp /build/store/conf/globs2 /staging/opt/zextras/conf/msgs/globs2 && \
    cp /build/store/conf/globs2.zimbra /staging/opt/zextras/conf/msgs/globs2.zimbra && \
    cp /build/store/conf/owasp_policy.xml /staging/opt/zextras/conf/owasp_policy.xml && \
    cp /build/store/conf/datasource.xml /staging/opt/zextras/conf/ && \
    cp /build/store/db/create_database.sql /staging/opt/zextras/db/

# Generate CLI scripts in build stage
ARG BASE_JAVA_ARGS="-Dfile.encoding=UTF-8 -server \
    -Dhttps.protocols=TLSv1.2,TLSv1.3 \
    -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 \
    -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true \
    -Dsun.net.inetaddr.ttl=60 -Dorg.apache.jasper.compiler.disablejsr199=true \
    -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:+UnlockExperimentalVMOptions \
    -XX:G1NewSizePercent=15 -XX:G1MaxNewSizePercent=45 -XX:-OmitStackTraceInFastThrow \
    -Djava.security.egd=file:/dev/./urandom \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    -Xss256k \
    -Xms1996m -Xmx1996m -Djava.io.tmpdir=/opt/zextras/mailboxd/work \
    -Djava.library.path=/opt/zextras/lib \
    -Dzimbra.config=/localconfig/localconfig.xml \
    -Dzimbra.native.required=false \
    -Dlog4j.configurationFile=/opt/zextras/conf/log4j.properties \
    -cp /opt/zextras/mailbox/jars/mailbox.jar:/opt/zextras/mailbox/jars/*"

RUN mkdir -p /staging/usr/bin && \
    printf '#!/bin/sh\njava %s com.zimbra.cs.account.ProvUtil "$@"\n' "${BASE_JAVA_ARGS}" > /staging/usr/bin/zmprov && \
    chmod +x /staging/usr/bin/zmprov && \
    printf '#!/bin/sh\njava %s com.zimbra.cs.util.GalSyncAccountUtil "$@"\n' "${BASE_JAVA_ARGS}" > /staging/usr/bin/zmgsautil && \
    chmod +x /staging/usr/bin/zmgsautil && \
    printf '#!/bin/sh\njava %s com.zimbra.cs.zclient.ZMailboxUtil "$@"\n' "${BASE_JAVA_ARGS}" > /staging/usr/bin/zmmailbox && \
    chmod +x /staging/usr/bin/zmmailbox

# Generate keystore using Alpine-based builder stage
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jre-alpine AS keystore-builder
RUN mkdir -p /keystore && \
    keytool -genkey -keyalg RSA \
    -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" \
    -keystore /keystore/keystore -keysize 2048 -validity 1461 \
    -keypass testkey1 -storepass teststore1

FROM eclipse-temurin:21-jre-jammy

# Copy entire staged directory structure
COPY --from=build /staging /

# Copy pre-generated keystore
COPY --from=keystore-builder /keystore/keystore /opt/zextras/mailboxd/etc/keystore

# Add OpenTelemetry agent
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar \
    /opt/zextras/opentelemetry-javaagent.jar

# Copy additional config files
COPY docker/mailbox/log4j.properties /opt/zextras/conf/log4j.properties
COPY docker/mailbox/localconfig/localconfig.xml /localconfig/localconfig.xml

ENV LDAP_URL="ldap://openldap:1389" \
    LDAP_ROOT_PASSWORD=qh6hWZvc \
    LDAP_ADMIN_PASSWORD=password \
    MARIADB_ROOT_PASSWORD=password \
    MARIADB_URL=mariadb \
    MARIADB_PORT=3306 \
    CARBONIO_FILES_SERVICE_URL="http://carbonio-files:10000" \
    CARBONIO_PREVIEW_SERVICE_URL="http://carbonio-preview:10000" \
    MAILBOXD_JAVA_OPTS="-Xss256k -Xms1996m -Xmx1996m"

COPY docker/mailbox/entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
