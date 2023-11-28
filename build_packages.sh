#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#

TARGET=$1

if [[ ${TARGET} == '' ||  ${TARGET} == 'ubuntu-focal' ]]
then
  docker run --entrypoint=yap -it -v $(pwd)/artifacts:/artifacts -v $(pwd):/tmp/staging registry.dev.zextras.com/jenkins/pacur/ubuntu-20.04:v2 build ubuntu-focal /tmp/staging/packages
else
  if [[ ${TARGET} == 'rocky-8' ]]
  then
    docker run --entrypoint=yap -it -v $(pwd)/artifacts:/artifacts -v $(pwd):/tmp/staging registry.dev.zextras.com/jenkins/pacur/rocky-8:v2 build rocky-8 /tmp/staging/packages
  fi
fi
