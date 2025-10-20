This image contains the mailbox main process with some default configuration.

The default configuration works with specific network connections and container 
names.
For example, in order to have a working docker-compose setup, you need to 
define containers with these names:
- carbonio-ldap -> openldap 
- carbonio-mta -> postfix
- carbonio-mariadb -> mariadb

You can customize the local configuration of the mailbox by replacing /localconfig/localconfig.xml
At startup the localconfig will be replaced with some 
ENVIRONMENT variables that you can find in the container build file.
For example the SERVER_HOSTNAME field is replaced by ${HOSTNAME}.

Mailbox provisioning CLI is available as "zmprov".

Startup options of the mailbox (e.g.: memory) can be overridden by setting 
the environment variable MAILBOXD_JAVA_OPTS.

Traces can be sent by setting TRACING_OPTIONS, e.g.:  
`TRACING_OPTIONS=-Dotel.service.name=mailbox -Dotel.metrics.exporter=none -Dotel.traces.exporter=zipkin -Dotel.exporter.zipkin.endpoint=http://zipkin:9411/api/v2/spans`