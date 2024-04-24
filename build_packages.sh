#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#

docker run -it --rm \
  --entrypoint=yap \
  -v $(pwd)/artifacts:/artifacts \
  -v $(pwd):/tmp/staging \
  docker.io/m0rf30/yap-ubuntu-jammy:1.8 \
  build ubuntu-jammy /tmp/staging/packages

