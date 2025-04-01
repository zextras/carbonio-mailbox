FROM ibm-semeru-runtimes:open-23.0.2_7-jre-noble

COPY docker/standalone/localconfig.xml /opt/zextras/conf/localconfig.xml
COPY store/src/test/resources/timezones-test.ics /opt/zextras/conf/timezones.ics
COPY store-conf/conf/msgs/* /opt/zextras/conf/msgs/

COPY pre-package/jars/* /opt/zextras/mailbox/jars/
COPY docker/standalone/entrypoint.sh .
ENTRYPOINT ["./entrypoint.sh"]