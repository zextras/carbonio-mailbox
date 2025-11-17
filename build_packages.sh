#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#
OS=${1:-"ubuntu-jammy"}

echo "Building for OS: $OS"

docker run -it --rm \
    --entrypoint=yap \
    -v "$(pwd)/artifacts/${OS}":/artifacts \
    -v "$(pwd)":/tmp/build \
    "docker.io/m0rf30/yap-${OS}:1.8" \
    build "${OS}" /tmp/build/packages
