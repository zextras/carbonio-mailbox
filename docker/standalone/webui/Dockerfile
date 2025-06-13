FROM ubuntu/nginx:1.18-20.04_beta

USER root
RUN apt update && apt install -y gnupg2 ca-certificates && apt clean
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 52FD40243E584A21
RUN echo deb https://repo.zextras.io/release/ubuntu jammy main > /etc/apt/sources.list.d/zextras.list
RUN apt update && apt install -y carbonio-webui openssl && apt clean
RUN mkdir -p /opt/zextras/conf
RUN openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 \
-nodes -keyout /opt/zextras/conf/nginx-carbonio.key \
-out /opt/zextras/conf/nginx-carbonio.crt -subj "/CN=example.com" \
-addext "subjectAltName=DNS:example.com,DNS:*.example.com,IP:10.0.0.1"
COPY ./webui/nginx-webui.conf /etc/nginx/conf.d/nginx-webui.conf
COPY ./webui/nginx.carbonio.admin.conf /etc/nginx/conf.d/nginx.carbonio.admin.conf
