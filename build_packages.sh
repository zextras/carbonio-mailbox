#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#
OS=${1:-"ubuntu-jammy"}

if [[ -z $OS ]]
then
  echo "Please provide an OS as argument: (ubuntu-jammy, rocky-8)"
  exit 1
fi

echo "Building for OS: $OS"

if [[ $OS == "ubuntu-jammy" ]]
then
  docker run -it --rm \
    --entrypoint=yap \
    -v $(pwd)/artifacts/ubuntu-jammy:/artifacts \
    -v $(pwd):/tmp/staging \
    docker.io/m0rf30/yap-ubuntu-jammy:1.8 \
    build ubuntu-jammy /tmp/staging/packages
elif [[ $OS == "ubuntu-focal" ]]
then
  docker run -it --rm \
    --entrypoint=yap \
    -v $(pwd)/artifacts/ubuntu-focal:/artifacts \
    -v $(pwd):/tmp/staging \
    docker.io/m0rf30/yap-ubuntu-focal:1.8 \
    build ubuntu-focal /tmp/staging/packages
elif [[ $OS == "rocky-8" ]]
then
  docker run -it --rm \
    --entrypoint=yap \
    -v $(pwd)/artifacts/rocky-8:/artifacts \
    -v $(pwd):/tmp/staging \
    docker.io/m0rf30/yap-rocky-8:1.10 \
    build rocky-8 /tmp/staging/packages
fi
