# Mailbox in Container

Steps:

1. `mvn clean install -DskipTests=true`
2. cd into [docker/standalone](./) and run `docker compose up 
--build`

This runs a dev environnment with:

- Mailbox
- MariaDB
- LDAP
- Postfix
- Nginx with webui:
  - https://localhost (Standard User) `test@demo.zextras.io` - `password`
  - http://localhost:6071 (Admin) `admin@demo.zextras.io` - `password`

You can attach the JVM debugger to localhost:5000 to debug the Mailbox.
