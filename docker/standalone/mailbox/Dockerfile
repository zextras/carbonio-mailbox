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

RUN mkdir -p /opt/zextras/db
COPY store/db/create_database.sql /opt/zextras/db/

COPY docker/standalone/mailbox/localconfig/localconfig.xml /localconfig/localconfig.xml

RUN mkdir -p /opt/zextras/mailboxd/etc/ && keytool -genkey -keyalg RSA \
    -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" \
    -keystore /opt/zextras/mailboxd/etc/keystore -keysize 2048 -validity 1461 \
    -keypass testkey1 -storepass teststore1

COPY docker/standalone/mailbox/entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
