# Mailbox in Container

Steps to run the mailbox in a container along with LDAP and MariaDB:
1. `mvn clean install -DskipTests=true`
2. cd into [docker/standalone](./) and run `docker compose up 
   --build`