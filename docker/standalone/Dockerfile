FROM ibm-semeru-runtimes:open-17-jdk

COPY store/src/test/resources/timezones-test.ics /opt/zextras/conf/timezones.ics
COPY store-conf/conf/msgs/* /opt/zextras/conf/msgs/

COPY pre-package/jars/* /opt/zextras/mailbox/jars/
COPY docker/standalone/entrypoint.sh .

RUN mkdir -p /opt/zextras/mailboxd/etc/

RUN keytool -genkey -keyalg RSA \
    -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown" \
    -keystore /opt/zextras/mailboxd/etc/keystore -keysize 2048 -validity 1461 \
    -keypass testkey1 -storepass teststore1

ENTRYPOINT ["./entrypoint.sh"]
