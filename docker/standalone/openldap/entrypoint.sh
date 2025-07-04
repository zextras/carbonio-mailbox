#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

echo "Check existing installation..."

startLDAP() {
  echo "Starting LDAP"
    /opt/zextras/common/libexec/slapd -l LOCAL0 -h ldap://0.0.0.0:1389 \
    -F /opt/zextras/data/ldap/config -d 256 >> /tmp/openldap.log &
    echo "Waiting for OpenLDAP to start..."
    while ! netcat -z localhost 1389; do
      sleep 0.5
    done
    echo "OpenLDAP started."
}

applySchema() {
  sh /ldap-utils/zmldapschema
  cp -r /opt/zextras/common/etc/openldap/zimbra/config/* /opt/zextras/data/ldap/config/
}

if [ -f /opt/zextras/data/ldap/config/cn\=config.ldif ]; then
  echo "Found existing installation"
  startLDAP

  echo "Running cleanup of remove attributes."
  /ldap-utils/cleanup.sh ldap://localhost:1389 qh6hWZvc

  echo "Stopping LDAP and applying new schema"
  kill "$(cat /run/carbonio/slapd.pid)"
  applySchema

  startLDAP
  tail -f /dev/null
fi

echo "No installation found, proceeding with bootstrap"

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

applySchema
mkdir -p /run/carbonio

echo "Replacing cn=config password"
sh /ldap-utils/replace-olcRootPw

/opt/zextras/common/libexec/slapd -l LOCAL0 -h ldap://0.0.0.0:1389 \
-F /opt/zextras/data/ldap/config -d 256 >> /tmp/openldap.log &

startLDAP

echo "Root LDAP password is: ${LDAP_ROOT_PASSWORD}."
echo "Zimbra/Admin LDAP password is: ${LDAP_ADMIN_PASSWORD}."
echo "Use test@demo.zextras.io - 'password' to login as standard user."
echo "Use admin@demo.zextras.io - 'password' to login as admin user."

sh /ldap-utils/add_initial_data

tail -f /tmp/openldap.log
