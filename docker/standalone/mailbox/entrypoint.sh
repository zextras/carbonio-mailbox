#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

sed -i -e "s#LDAP_URL#${LDAP_URL}#g" /localconfig/localconfig.xml
sed -i -e "s/LDAP_ROOT_PASSWORD/${LDAP_ROOT_PASSWORD}/g" /localconfig/localconfig.xml
sed -i -e "s/LDAP_ADMIN_PASSWORD/${LDAP_ADMIN_PASSWORD}/g" /localconfig/localconfig.xml
sed -i -e "s/MARIADB_ROOT_PASSWORD/${MARIADB_ROOT_PASSWORD}/g" /localconfig/localconfig.xml
sed -i -e "s/MARIADB_URL/${MARIADB_URL}/g" /localconfig/localconfig.xml
sed -i -e "s/MARIADB_PORT/${MARIADB_PORT}/g" /localconfig/localconfig.xml
sed -i -e "s/SERVER_HOSTNAME/${HOSTNAME}/g" /localconfig/localconfig.xml

java ${JAVA_ARGS}