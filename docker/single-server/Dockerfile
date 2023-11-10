FROM carbonio/ce-single-u20:23.10.0

USER root

RUN apt update
RUN apt install dpkg-dev -y

COPY artifacts/*amd64.deb /packages/
RUN cd /packages && dpkg-scanpackages -m . > Packages

RUN echo deb [trusted=yes] file:/packages/ / > /etc/apt/sources.list.d/zextras.list

RUN apt update && apt upgrade -y

COPY docker/single-server/mailbox_health_check.sh /opt/zextras/
COPY docker/single-server/prepare_data.sh /opt/zextras/
COPY docker/single-server/entrypoint.sh /opt/zextras/

RUN chown -R zextras:zextras /opt/zextras/mailbox_health_check.sh
RUN chown -R zextras:zextras /opt/zextras/prepare_data.sh
RUN chown -R zextras:zextras /opt/zextras/entrypoint.sh
