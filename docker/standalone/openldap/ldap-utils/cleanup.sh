#!/bin/bash

#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

# === CONFIGURATION ===
# TODO: write deprecated attributes to a file

LDAP_URL=$1
LDAP_ROOT_PASSWORD=$2
file_content=$(</tmp/deprecated_attrs.txt)

# Convert to array, preserving newlines
IFS=$'\n' read -r -d '' -a deprecated <<< "$file_content"

# Iterate over each line
for item in "${deprecated[@]}"; do
    echo "Deprecated attribute in cn=zimbra: $item"
    /opt/zextras/common/bin/ldapsearch -x -LLL  -D cn=config -w "${LDAP_ROOT_PASSWORD}" \
    -H "${LDAP_URL}" -b "cn=zimbra" "$item=*" dn |
      grep '^dn: ' | while read -r line; do
        DN="${line#dn: }"
        cat <<EOF
dn: $DN
changetype: modify
delete: $item
EOF
done > "/tmp/delete_zimbra_attribute_$item.ldif"
    /opt/zextras/common/bin/ldapmodify -D cn=config -w "${LDAP_ROOT_PASSWORD}" -H "${LDAP_URL}" -f "/tmp/delete_zimbra_attribute_$item.ldif"

    echo "Deprecated attribute in cn=config: $item"
    /opt/zextras/common/bin/ldapsearch -x -LLL  -D cn=config -w "${LDAP_ROOT_PASSWORD}" \
        -H "${LDAP_URL}" -b "cn=config" "$item=*" dn |
          grep '^dn: ' | while read -r line; do
            DN="${line#dn: }"
            cat <<EOF
dn: $DN
changetype: modify
delete: $item
EOF
    done > "/tmp/delete_config_attribute_$item.ldif"
    /opt/zextras/common/bin/ldapmodify -D cn=config -w "${LDAP_ROOT_PASSWORD}" -H "${LDAP_URL}" -f "/tmp/delete_config_attribute_$item.ldif"
done
echo "All deprecated attributes have been deleted"