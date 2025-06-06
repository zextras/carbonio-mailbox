FROM ibm-semeru-runtimes:open-17-jdk

COPY store/src/test/resources/timezones-test.ics /opt/zextras/conf/timezones.ics
COPY store-conf/conf/msgs/* /opt/zextras/conf/msgs/

RUN mkdir -p /opt/zextras/mailbox/jars/
COPY store/target/zm-store.jar /opt/zextras/mailbox/jars/
COPY store/target/dependencies/* /opt/zextras/mailbox/jars/
COPY native/target/zm-native*.jar /opt/zextras/mailbox/jars/
COPY soap/target/zm-soap*.jar /opt/zextras/mailbox/jars/
COPY common/target/zm-common*.jar /opt/zextras/mailbox/jars/

ENV LDAP_URL="ldap://openldap:1389"
ENV LDAP_ROOT_PASSWORD=qh6hWZvc
ENV LDAP_ADMIN_PASSWORD=password
ENV MARIADB_ROOT_PASSWORD=password
ENV MARIADB_URL=mariadb
ENV MARIADB_PORT=3306

ENV JAVA_ARGS="-Dfile.encoding=UTF-8 -server \
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
              -Dzimbra.config=/localconfig/localconfig.xml \
              -Dzimbra.native.required=false \
              -cp /opt/zextras/mailbox/jars/mailbox.jar:/opt/zextras/mailbox/jars/* com.zextras.mailbox.Mailbox"

RUN mkdir -p /opt/zextras/db
COPY store/db/create_database.sql /opt/zextras/db/

COPY docker/standalone/mailbox/localconfig/localconfig.xml /localconfig/localconfig.xml

RUN mkdir -p /opt/zextras/mailboxd/etc/ && keytool -genkey -keyalg RSA \
    -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" \
    -keystore /opt/zextras/mailboxd/etc/keystore -keysize 2048 -validity 1461 \
    -keypass testkey1 -storepass teststore1

COPY docker/standalone/mailbox/entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
