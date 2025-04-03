#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

/opt/zextras/common/sbin/postconf maillog_file=/var/log/postfix.log

/opt/zextras/common/sbin/postfix start

tail -f /var/log/postfix.log