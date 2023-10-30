FROM carbonio/ce-single-u20:23.10.0

USER root

RUN apt update

COPY artifacts/*amd64.deb /packages/

RUN dpkg -i /packages/*deb
