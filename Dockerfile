FROM carbonio/ce-single-u20

RUN apt update
COPY artifacts/*amd64.deb /packages/
RUN dpkg -i /packages/*deb
