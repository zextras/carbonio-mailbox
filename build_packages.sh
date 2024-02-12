#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#


docker run --entrypoint=yap -it -v $(pwd)/artifacts:/artifacts -v $(pwd):/tmp/staging docker.io/m0rf30/yap-ubuntu-focal:1.6 build ubuntu-focal /tmp/staging/packages