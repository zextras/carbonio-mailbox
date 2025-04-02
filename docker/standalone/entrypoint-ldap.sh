#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

# See PKGBUILD of directory-server
mkdir -p /opt/zextras/data/ldap/state/run
mkdir -p /opt/zextras/data/ldap/config
mkdir -p /opt/zextras/data/ldap/mdb/db


# Generate 10 years self-signed certificate and key
mkdir -p /opt/zextras/conf/ca
openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 \
-nodes -keyout /opt/zextras/conf/slapd.key \
-out /opt/zextras/conf/slapd.crt -subj "/CN=example.com" \
-addext "subjectAltName=DNS:example.com,DNS:*.example.com,IP:10.0.0.1"

sh /ldap-utils/zmldapschema
cp -r /opt/zextras/common/etc/openldap/zimbra/config/* /opt/zextras/data/ldap/config/


mkdir -p /run/carbonio

/opt/zextras/common/libexec/slapd -l LOCAL0 -h ldap://0.0.0.0:1389 \
-F /opt/zextras/data/ldap/config -d 256