#!/usr/bin/env bash

OS=${2:-"ubuntu-jammy"}
HOST=$1

TMP_FOLDER="/tmp/$(date +%s)"
DEB_PACKAGES_TO_INSTALL="carbonio-mailbox*_amd64.deb"

rsync -acz --progress --stats artifacts/${OS}/${DEB_PACKAGES_TO_INSTALL} root@${HOST}:${TMP_FOLDER}/
ssh root@${HOST} "dpkg -i ${TMP_FOLDER}/*.deb"
ssh -t root@${HOST} "pending-setups -a"
echo ""
ssh -q root@${HOST} "/bin/bash" <<EOF
su - zextras --
echo -n -e "\033[1;31m"
zmmailboxdctl restart
echo -n -e "\033[0m"
exit
EOF

