# Mailbox in Container

Steps:

1. `mvn clean install -DskipTests=true`
2. cd into [docker/standalone](.) and run `docker compose up 
--build` \
 If you are using a Mac you can build using 
   `DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose up
   --build`

This runs a dev environnment with:

- Mailbox
- MariaDB
- LDAP
- Postfix
- Nginx with:
  - webui:
    - https://localhost (Standard User 1) `test@demo.zextras.io` - `password`
    - https://localhost (Standard User 2) `test2@demo2.zextras.io` - `password`
    - http://localhost:6071 (Admin) `admin@demo.zextras.io` - `password`
  - IMAP/IMAPS (needs zm-nginx-lookup-store to be in mailbox container 
    under /opt/zextras/lib/ext/nginx-lookup/):
    - localhost:143 no TLS, plain text authentication
    - localhost:993 TLS, plain text authentication

You can also interact with the cli by running:
 - `docker compose exec mailbox1 sh -c "/opt/zextras/mailbox/cli ga 
   test@demo.zextras.io"`
 - `docker compose exec mailbox1 sh -c "/opt/zextras/mailbox/cli"` (interactive)
or from within the container.
You can attach the JVM debugger to localhost:5005 to debug mailbox1, 5006 
for mailbox2.
