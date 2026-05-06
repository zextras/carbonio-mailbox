#!/bin/bash
#
# SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only
#

docker build . -f docker/mailbox/Dockerfile -t carbonio-mailbox:custom