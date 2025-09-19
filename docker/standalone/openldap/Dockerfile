FROM ubuntu:jammy

USER root
RUN apt update && apt install -y gnupg2 ca-certificates && apt clean \
&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 52FD40243E584A21 \
&& echo deb https://repo.zextras.io/release/ubuntu jammy main > /etc/apt/sources.list.d/zextras.list \
&& apt update && apt install -y carbonio-openldap netcat && apt clean

ENV LDAP_ROOT_PASSWORD=qh6hWZvc
ENV LDAP_ADMIN_PASSWORD=password
EXPOSE 1389

COPY docker/standalone/openldap/ldap-utils/ /ldap-utils/
COPY mailbox-ldap-lib/ldap/generated/ /opt/zextras/common/etc/openldap/zimbra/
RUN mkdir -p /opt/zextras/ldap/custom-ldifs \
            /opt/zextras/data/ldap/state/run \
            /opt/zextras/data/ldap/config \
            /opt/zextras/data/ldap/mdb/db \
            /run/carbonio \
            /opt/zextras/conf/ca

COPY docker/standalone/openldap/entrypoint.sh .
ENTRYPOINT ["./entrypoint.sh"]