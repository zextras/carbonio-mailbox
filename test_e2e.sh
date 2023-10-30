#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

docker run --entrypoint=yap -it -v $(pwd):/tmp/staging --network=host  registry.dev.zextras.com/jenkins/pacur/ubuntu-20.04:v2 build ubuntu-focal /tmp/staging/packages

docker build . -t carbonio-ce:mailbox-dev

docker run carbonio-ce:mailbox-dev