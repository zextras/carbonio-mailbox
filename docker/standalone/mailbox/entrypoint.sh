#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

LOCALCONFIG_FILE="/opt/zextras/conf/localconfig.xml"
sed -i -e "s#LDAP_URL#${LDAP_URL}#g" "${LOCALCONFIG_FILE}"
sed -i -e "s/LDAP_ROOT_PASSWORD/${LDAP_ROOT_PASSWORD}/g" "${LOCALCONFIG_FILE}"
sed -i -e "s/LDAP_ADMIN_PASSWORD/${LDAP_ADMIN_PASSWORD}/g" "${LOCALCONFIG_FILE}"
sed -i -e "s/MARIADB_ROOT_PASSWORD/${MARIADB_ROOT_PASSWORD}/g" "${LOCALCONFIG_FILE}"
sed -i -e "s/MARIADB_URL/${MARIADB_URL}/g" "${LOCALCONFIG_FILE}"
sed -i -e "s/MARIADB_PORT/${MARIADB_PORT}/g" "${LOCALCONFIG_FILE}"
sed -i -e "s/SERVER_HOSTNAME/${HOSTNAME}/g" "${LOCALCONFIG_FILE}"

SERVER_EXISTS=$(zmprov -l gs "${HOSTNAME}" 2>&1)
if [[ $SERVER_EXISTS == *"account.NO_SUCH_SERVER"* ]]; then
  echo "Creating server ${HOSTNAME}"
  zmprov -l cs "${HOSTNAME}" zimbraServiceInstalled mailbox zimbraServiceEnabled mailbox zimbraServiceEnabled service
  echo "Server ${HOSTNAME} created"
fi

mailbox