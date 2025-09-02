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

YAP_BUILD_PATH="/tmp/project_build"
echo "Building for OS: $OS"
if [[ $OS == "ubuntu-jammy" ]]
then
  docker run -it --rm \
    --entrypoint=yap \
    -v $(pwd)/artifacts/ubuntu-jammy:/artifacts \
    -v $(pwd):"${YAP_BUILD_PATH}" \
    docker.io/m0rf30/yap-ubuntu-jammy:1.8 \
    build ubuntu-focal "${YAP_BUILD_PATH}"/packages
elif [[ $OS == "ubuntu-focal" ]]
then
  docker run -it --rm \
    --entrypoint=yap \
    -v $(pwd)/artifacts/ubuntu-focal:/artifacts \
    -v $(pwd):"${YAP_BUILD_PATH}" \
    docker.io/m0rf30/yap-ubuntu-focal:1.8 \
    build ubuntu-focal "${YAP_BUILD_PATH}"/packages
elif [[ $OS == "rocky-8" ]]
then
  docker run -it --rm \
    --entrypoint=yap \
    -v $(pwd)/artifacts/rocky-8:/artifacts \
    -v $(pwd):"${YAP_BUILD_PATH}" \
    docker.io/m0rf30/yap-rocky-8:1.10 \
    build rocky-8 "${YAP_BUILD_PATH}"/packages
fi
