ARG UBUNTU_VERSION

FROM ubuntu:${UBUNTU_VERSION}
ARG DEBIAN_FRONTEND=noninteractive
LABEL maintainer="gianluca.boiano@zextras.com"

RUN echo "resolvconf resolvconf/linkify-resolvconf boolean false" | debconf-set-selections; \
    apt-get -y update; \
    apt-get install --no-install-recommends -y \
    ant \
    ant-contrib \
    ant-optional \
    build-essential \
    clang-10 \
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
    openjdk-11-jdk-headless \
    openssh-client \
    rsync \
    ruby \
    software-properties-common \
    sudo \
    wget;

ADD docker/build_all.sh /usr/bin
ADD docker/clang-switch.sh /usr/bin
ADD build.config /usr/bin
ENTRYPOINT ["/bin/bash"]
