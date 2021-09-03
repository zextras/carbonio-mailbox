ARG UBUNTU_VERSION

FROM ubuntu:${UBUNTU_VERSION}
ARG DEBIAN_FRONTEND=noninteractive
LABEL maintainer="gianluca.boiano@zextras.com"

RUN echo "resolvconf resolvconf/linkify-resolvconf boolean false" | debconf-set-selections; \
    # Set the apt HTTP proxy
    echo 'Acquire::HTTP::Proxy "http://aptproxy.local.studiostorti.com:3142";' > /etc/apt/apt.conf.d/01proxy; \
    echo 'Acquire::HTTPS::Proxy "false";' >> /etc/apt/apt.conf.d/01proxy; \
    apt-get -y update; \
    apt-get install --no-install-recommends -y \
    ant \
    ant-contrib \
    ant-optional \
    build-essential \
    debhelper \
    git \
    libdata-uuid-perl \
    libdbi-perl \
    libfile-grep-perl \
    libnet-ldap-perl \
    libnet-dns-perl \
    libnet-http-perl \
    libnetaddr-ip-perl \
    libunix-syslog-perl \
    libxml-parser-perl \
    libxml-sax-perl \
    libxml-sax-base-perl \
    libxml-simple-perl \
    lsb-release \ 
    maven \
    openjdk-8-jdk-headless \
    openssh-client \
    rsync \
    ruby \
    software-properties-common \
    sudo \
    wget;

# Proxy cache within cluster (let's speed up all da sheet:)
ENV http_proxy=http://build-proxy.dev.zextras.com
ADD docker/build_all.sh /usr/bin
ADD build.config /usr/bin
ENTRYPOINT ["/bin/bash"]
