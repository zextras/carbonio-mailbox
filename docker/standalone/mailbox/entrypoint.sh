#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

sed -i -e "s#LDAP_URL#${LDAP_URL}#g" /localconfig/localconfig.xml
sed -i -e "s/LDAP_ROOT_PASSWORD/${LDAP_ROOT_PASSWORD}/g" /localconfig/localconfig.xml
sed -i -e "s/LDAP_ADMIN_PASSWORD/${LDAP_ADMIN_PASSWORD}/g" /localconfig/localconfig.xml
sed -i -e "s/MARIADB_ROOT_PASSWORD/${MARIADB_ROOT_PASSWORD}/g" /localconfig/localconfig.xml
sed -i -e "s/MARIADB_URL/${MARIADB_URL}/g" /localconfig/localconfig.xml
sed -i -e "s/MARIADB_PORT/${MARIADB_PORT}/g" /localconfig/localconfig.xml
sed -i -e "s/SERVER_HOSTNAME/${HOSTNAME}/g" /localconfig/localconfig.xml
sed -i -e "s#CARBONIO_FILES_SERVICE_URL#${CARBONIO_FILES_SERVICE_URL}#g" /localconfig/localconfig.xml
sed -i -e "s#CARBONIO_PREVIEW_SERVICE_URL#${CARBONIO_PREVIEW_SERVICE_URL}#g" /localconfig/localconfig.xml

SERVER_EXISTS=$(/usr/bin/zmprov -l gs "${HOSTNAME}" 2>&1)
if [[ $SERVER_EXISTS == *"account.NO_SUCH_SERVER"* ]]; then
  echo "Creating server ${HOSTNAME}"
  /usr/bin/zmprov -l cs "${HOSTNAME}" zimbraServiceInstalled mailbox \
                                      zimbraServiceEnabled mailbox \
                                      zimbraServiceEnabled service \
                                      zimbraReverseProxyLookupTarget TRUE
  echo "Server ${HOSTNAME} created"
fi
JAVA_OPTS="-Dfile.encoding=UTF-8 -server \
                         -Dhttps.protocols=TLSv1.2,TLSv1.3 \
                         -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 \
                         -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true \
                         -Dsun.net.inetaddr.ttl=60 -Dorg.apache.jasper.compiler.disablejsr199=true \
                         -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:+UnlockExperimentalVMOptions \
                         -XX:G1NewSizePercent=15 -XX:G1MaxNewSizePercent=45 -XX:-OmitStackTraceInFastThrow \
                         -Djava.security.egd=file:/dev/./urandom \
                         --add-opens java.base/java.lang=ALL-UNNAMED \
                         ${MAILBOXD_JAVA_OPTS} -Djava.io.tmpdir=/opt/zextras/mailboxd/work \
                         -Djava.library.path=/opt/zextras/lib \
                         -Dzimbra.config=/localconfig/localconfig.xml \
                         -Dzimbra.native.required=false \
                         -Dlog4j.configurationFile=/opt/zextras/conf/log4j.properties \
                         -cp /opt/zextras/mailbox/jars/mailbox.jar:/opt/zextras/mailbox/jars/*"
if [ -z "${TRACING_OPTIONS}" ]; then
  java ${JAVA_OPTS} \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
    com.zextras.mailbox.Mailbox
else
  java -javaagent:/opt/zextras/opentelemetry-javaagent.jar \
    ${JAVA_OPTS} \
    ${TRACING_OPTIONS} \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
    com.zextras.mailbox.Mailbox
fi
