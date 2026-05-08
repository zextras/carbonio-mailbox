#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#
OS="ubuntu-jammy"

echo "Building for OS: $OS"


docker run -it --rm \
    --entrypoint=/bin/bash \
    -v "$(pwd)/artifacts/${OS}":/artifacts \
    -v "$(pwd)":/project \
    "docker.io/m0rf30/yap-${OS}:1.53" \
    -c "
        set -e
        apt update
        apt install -y --no-install-recommends gnupg wget ca-certificates
        wget -O- 'https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x5dc7680bc4378c471a7fa80f52fd40243e584a21' | gpg --dearmor > /usr/share/keyrings/zextras.gpg
        chmod 644 /usr/share/keyrings/zextras.gpg
        echo 'deb [arch=amd64 signed-by=/usr/share/keyrings/zextras.gpg] https://repo.zextras.io/release/ubuntu jammy main' > /etc/apt/sources.list.d/zextras.list
        apt-get update -qq
        yap build ${OS} /project/packages
    "
