#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

java -Dfile.encoding=UTF-8 -server \
-Dhttps.protocols=TLSv1.2,TLSv1.3 \
-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 \
-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true \
-Dsun.net.inetaddr.ttl=60 -Dorg.apache.jasper.compiler.disablejsr199=true \
-XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:+UnlockExperimentalVMOptions \
-XX:G1NewSizePercent=15 -XX:G1MaxNewSizePercent=45 -XX:-OmitStackTraceInFastThrow \
-verbose:gc \
-Xlog:gc*=info,safepoint=info:file=/opt/zextras/log/gc.log:time:filecount=20,filesize=10m \
-Djava.security.egd=file:/dev/./urandom \
--add-opens java.base/java.lang=ALL-UNNAMED \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
-Xss256k -Dlog4j.configurationFile=/opt/zextras/conf/log4j.properties \
-Xms1996m -Xmx1996m -Djava.io.tmpdir=/opt/zextras/mailboxd/work \
-Djava.library.path=/opt/zextras/lib \
-Dzimbra.config=/opt/zextras/conf/localconfig.xml \
-cp /opt/zextras/mailbox/jars/mailbox.jar:/opt/zextras/mailbox/jars/* com.zextras.mailbox.Mailbox