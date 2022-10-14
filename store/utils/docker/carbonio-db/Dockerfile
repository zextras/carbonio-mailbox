FROM mariadb:10.9-jammy

ENV MYSQL_DATABASE=zextras \
    MYSQL_USER=zextras \
    MYSQL_PASSWORD=zextras \
    MYSQL_ROOT_PASSWORD=password

ADD scripts /docker-entrypoint-initdb.d

EXPOSE 3306
