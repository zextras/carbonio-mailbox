#!/usr/bin/env bash

HOST=$1
TMP_FOLDER="/tmp/$(date +%s)"
DEB_PACKAGES_TO_INSTALL="carbonio-common-core*focal_amd64.deb"

rsync -acz --progress --stats artifacts/${DEB_PACKAGES_TO_INSTALL} root@${HOST}:${TMP_FOLDER}
ssh root@${HOST} "dpkg -i ${TMP_FOLDER}/*.deb"
ssh -t root@${HOST} "pending-setups -a"

