FROM ubuntu:jammy

USER root
RUN apt update && apt install -y gnupg2 ca-certificates && apt clean \
&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 52FD40243E584A21 \
&& echo deb https://repo.zextras.io/release/ubuntu jammy main > /etc/apt/sources.list.d/zextras.list \
&& apt update && apt install -y carbonio-nginx carbonio-webui openssl netcat && apt clean

RUN mkdir -p /opt/zextras/conf
RUN mkdir -p /opt/zextras/data/tmp/nginx/client
RUN mkdir -p /run/carbonio
RUN mkdir -p /opt/zextras/common/conf

RUN openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 \
-nodes -keyout /opt/zextras/conf/nginx-carbonio.key \
-out /opt/zextras/conf/nginx-carbonio.crt -subj "/CN=example.com" \
-addext "subjectAltName=DNS:example.com,DNS:*.example.com,IP:10.0.0.1"

COPY ./webui/nginx.conf.zmlookup /opt/zextras/conf/nginx.conf.zmlookup
COPY ./webui/mime.types /opt/zextras/common/conf/mime.types
COPY ./webui/nginx.conf /opt/zextras/conf/nginx.conf
COPY ./webui/nginx-webui.conf /opt/zextras/conf/nginx-webui.conf
COPY ./webui/nginx-mail.conf /opt/zextras/conf/nginx-mail.conf
COPY ./webui/nginx.carbonio.admin.conf /opt/zextras/conf/nginx.carbonio.admin.conf

ENTRYPOINT ["/opt/zextras/common/sbin/nginx", "-c", "/opt/zextras/conf/nginx.conf"]
