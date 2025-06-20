This image contains the mailbox main process with some default configuration.

The default configuration works with specific network connections and container 
names.
For example, in order to have a working docker-compose setup, you need to 
define containers with these names:
- carbonio-ldap -> openldap 
- carbonio-mariadb -> mariadb
- carbonio-mariadb -> mariadb

You can customize the local configuration of the mailbox by replacing /localconfig/localconfig.xml
By default the localconfig of the container will be replaced with some 
ENVIRONMENT variables that you can find in the container build file.
In particular the SERVER_HOSTNAME field is replaced by ${HOSTNAME} 
automatically.