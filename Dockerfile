FROM carbonio/ce-single-u20:23.10.0

USER root

RUN apt update

COPY artifacts/*amd64.deb /packages/
COPY prepare_data.sh /opt/zextras/
RUN chown -R zextras:zextras /opt/zextras/prepare_data.sh

RUN dpkg -i /packages/*deb
