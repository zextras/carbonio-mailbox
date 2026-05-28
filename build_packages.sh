#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#
OS=${1:-"ubuntu-jammy"}

echo "Building for OS: $OS"

# Inject the Carbonio public repo (area51/devel) to resolve runtime depends
# (carbonio-core, service-discover, service-discover-base, pending-setups).
BASE_URL="https://repo.area51-zextras.com/devel"
GPG_KEY_URL="https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x5dc7680bc4378c471a7fa80f52fd40243e584a21"
case "${OS}" in
    ubuntu-*)
        SUITE="${OS#ubuntu-}"  # e.g. "jammy" from "ubuntu-jammy"
        REPO_FLAG="--repo name=carbonio,url=${BASE_URL}/ubuntu,suite=${SUITE},components=main,format=deb,keyURL=${GPG_KEY_URL}"
        ;;
    rocky-8*)
        REPO_FLAG="--repo name=carbonio,url=${BASE_URL}/rhel8,format=rpm,keyURL=https://repo.zextras.io/repomd.xml.key"
        ;;
    rocky-9*)
        REPO_FLAG="--repo name=carbonio,url=${BASE_URL}/rhel9,format=rpm,keyURL=https://repo.zextras.io/repomd.xml.key"
        ;;
    *)
        REPO_FLAG=""
        ;;
esac

docker run -it --rm \
    --user root \
    --entrypoint=yap \
    -v "$(pwd)/artifacts/${OS}":/artifacts \
    -v "$(pwd)":/tmp/build \
    "docker.io/m0rf30/yap-${OS}:2.1.10" \
    build ${REPO_FLAG} "${OS}" /tmp/build/packages
