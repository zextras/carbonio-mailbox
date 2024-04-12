#!/bin/bash
apt update && apt install dpkg-dev -y

mkdir -p /tmp/local_repo
cp -r /artifacts/*deb /tmp/local_repo

cd /tmp/local_repo && dpkg-scanpackages -m . > Packages

echo "deb [trusted=yes] https://repo.zextras.io/release/ubuntu focal main" > /etc/apt/sources.list.d/zextras.list
echo "deb [trusted=yes] file:/tmp/local_repo/ /" >> /etc/apt/sources.list.d/zextras.list

apt update && apt full-upgrade -y
apt autoremove -y