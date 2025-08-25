#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

startLDAP() {
  echo "Starting LDAP..."
    /opt/zextras/common/libexec/slapd -l LOCAL0 -h ldap://0.0.0.0:1389 \
    -F /opt/zextras/data/ldap/config -d 256 >> /tmp/openldap.log &
    while ! netcat -z localhost 1389; do
      sleep 0.5
    done
    echo "OpenLDAP started."
}

applySchema() {
  echo "Applying schema"
  sh /ldap-utils/zmldapschema
}

applyLdapConfig() {
  echo "Applying LDAP config (cn=config)"
   cp -r /opt/zextras/common/etc/openldap/zimbra/config/* /opt/zextras/data/ldap/config/
   echo "Replacing cn=config password"
   sh /ldap-utils/replace-olcRootPw
}

stopLDAP() {
  if [ -f /run/carbonio/slapd.pid ]; then
    kill "$(cat /run/carbonio/slapd.pid)"
  fi
}

waitLogs() {
  tail -f /tmp/openldap.log
}

echo "Checking for existing installation..."
if [ -f /opt/zextras/data/ldap/config/cn\=config.ldif ]; then
  echo "Found existing installation"
  applySchema
  startLDAP
  waitLogs
fi

echo "No installation found, proceeding with bootstrap"

# Generate 10 years self-signed certificate and key

openssl req -x509 -newkey rsa:4096 -sha256 -days 3650 \
-nodes -keyout /opt/zextras/conf/slapd.key \
-out /opt/zextras/conf/slapd.crt -subj "/CN=example.com" \
-addext "subjectAltName=DNS:example.com,DNS:*.example.com,IP:10.0.0.1"


applySchema
applyLdapConfig

startLDAP

echo "Root LDAP password is: ${LDAP_ROOT_PASSWORD}."
echo "Zimbra/Admin LDAP password is: ${LDAP_ADMIN_PASSWORD}."
echo "Use test@demo.zextras.io - 'password' to login as standard user."
echo "Use admin@demo.zextras.io - 'password' to login as admin user."

sh /ldap-utils/add_initial_data

waitLogs
