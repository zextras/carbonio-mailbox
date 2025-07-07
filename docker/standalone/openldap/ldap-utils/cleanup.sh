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
file_content=$(</ldap-utils/deprecated_attrs.txt)

# Convert to array, preserving newlines
IFS=$'\n' read -r -d '' -a deprecated <<< "$file_content"

write_ldif_modify_to_file() {
  fileName=$1
  input=$2
  if [ -n "$input" ]; then
  echo "Found entry: $input"
  echo "$input" |
        grep '^dn: ' | while read -r line; do
          DN="${line#dn: }"
          cat <<EOF
dn: $DN
changetype: modify
delete: $item

EOF
done > "$fileName"
  echo "Applying ldif $fileName changes:"
  cat "$fileName"
  /opt/zextras/common/bin/ldapmodify -D cn=config -w "${LDAP_ROOT_PASSWORD}" -H "${LDAP_URL}" -f "$fileName"
  echo "Changes applied"
  fi
}

# Iterate over each line
for item in "${deprecated[@]}"; do
    echo "Searching for attribute: $item"
    write_ldif_modify_to_file "/tmp/delete_zimbra_attribute_$item.ldif" \
    "$(/opt/zextras/common/bin/ldapsearch -x -LLL  -D cn=config -w "${LDAP_ROOT_PASSWORD}" -H "${LDAP_URL}" -b "cn=zimbra" "$item=*" dn)"

    write_ldif_modify_to_file "/tmp/delete_config_attribute_$item.ldif" \
    "$(/opt/zextras/common/bin/ldapsearch -x -LLL  -D cn=config -w "${LDAP_ROOT_PASSWORD}" -H "${LDAP_URL}" -b "cn=config" "$item=*" dn)"
done

echo "All deprecated attributes have been deleted"