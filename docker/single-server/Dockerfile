FROM carbonio/ce-single-u20:23.10.0

USER root

COPY artifacts/*amd64.deb /packages/
RUN dpkg -i /packages/*deb

COPY docker/single-server/prepare_data.sh /opt/zextras/
COPY docker/single-server/entrypoint.sh /opt/zextras/

RUN chown -R zextras:zextras /opt/zextras/prepare_data.sh
RUN chown -R zextras:zextras /opt/zextras/entrypoint.sh