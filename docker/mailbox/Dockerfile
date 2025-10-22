FROM maven:3-eclipse-temurin-17-alpine AS build

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

FROM eclipse-temurin:17-jre-jammy

RUN mkdir -p /opt/zextras/mailbox/jars/ && \
    mkdir -p /opt/zextras/lib/ext/nginx-lookup/ && \
    mkdir -p /opt/zextras/store && \
    mkdir -p /opt/zextras/mailboxd/work && \
    mkdir -p /opt/zextras/log

COPY --from=build /build/store/src/test/resources/timezones-test.ics /opt/zextras/conf/timezones.ics
COPY --from=build /build/store/conf/msgs/* /opt/zextras/conf/msgs/
# Mime detection
COPY --from=build /build/store/conf/magic /opt/zextras/conf/magic
COPY --from=build /build/store/conf/magic.zimbra /opt/zextras/conf/magic.zimbra
COPY --from=build /build/store/conf/globs2 /opt/zextras/conf/msgs/globs2
COPY --from=build /build/store/conf/globs2.zimbra /opt/zextras/conf/msgs/globs2.zimbra
COPY --from=build /build/store/conf/owasp_policy.xml /opt/zextras/conf/owasp_policy.xml
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar \
    /opt/zextras/opentelemetry-javaagent.jar

COPY --from=build /build/store/target/zm-store.jar /opt/zextras/mailbox/jars/mailbox.jar
COPY --from=build /build/store/target/dependencies/* /opt/zextras/mailbox/jars/
COPY docker/mailbox/log4j.properties /opt/zextras/conf/log4j.properties
COPY --from=build /build/store/conf/datasource.xml /opt/zextras/conf/

ENV LDAP_URL="ldap://openldap:1389"
ENV LDAP_ROOT_PASSWORD=qh6hWZvc
ENV LDAP_ADMIN_PASSWORD=password
ENV MARIADB_ROOT_PASSWORD=password
ENV MARIADB_URL=mariadb
ENV MARIADB_PORT=3306
ENV CARBONIO_FILES_SERVICE_URL="http://carbonio-files:10000"
ENV CARBONIO_PREVIEW_SERVICE_URL="http://carbonio-preview:10000"
ENV MAILBOXD_JAVA_OPTS="-Xss256k -Xms1996m -Xmx1996m"
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

ARG JAVA_CLI_ARGS="${BASE_JAVA_ARGS} \
              com.zimbra.cs.account.ProvUtil \"\$@\""
RUN echo "java ${JAVA_CLI_ARGS}" > /usr/bin/zmprov && chmod +x /usr/bin/zmprov && mkdir -p /opt/zextras/db

ARG JAVA_ZMGSA_ARGS="${BASE_JAVA_ARGS} \
              com.zimbra.cs.util.GalSyncAccountUtil \"\$@\""
RUN echo "java ${JAVA_ZMGSA_ARGS}" > /usr/bin/zmgsautil && chmod +x /usr/bin/zmgsautil

ARG JAVA_ZMMAILBOX_ARG="${BASE_JAVA_ARGS} \
              com.zimbra.cs.zclient.ZMailboxUtil \"\$@\""
RUN echo "java ${JAVA_ZMMAILBOX_ARG}" > /usr/bin/zmmailbox && chmod +x /usr/bin/zmmailbox

# example: -Dotel.service.name=your-service-name -Dotel.traces.exporter=zipkin
ENV TRACING_OPTIONS=""

COPY store/db/create_database.sql /opt/zextras/db/

COPY docker/mailbox/localconfig/localconfig.xml /localconfig/localconfig.xml

RUN mkdir -p /opt/zextras/mailboxd/etc/ && keytool -genkey -keyalg RSA \
    -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" \
    -keystore /opt/zextras/mailboxd/etc/keystore -keysize 2048 -validity 1461 \
    -keypass testkey1 -storepass teststore1

COPY docker/mailbox/entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
