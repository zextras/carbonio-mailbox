#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

sed -i -e "s/LDAP_ROOT_PASSWORD/${LDAP_ROOT_PASSWORD}/g" /opt/zextras/conf/*.cf

/opt/zextras/common/sbin/postconf maillog_file=/dev/stdout
exec /opt/zextras/common/sbin/postfix start-fg