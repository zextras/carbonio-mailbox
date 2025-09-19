#!/bin/bash

# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

set -e

TARGET_MODULE_CONF_FILE="/opt/zextras/data/ldap/config/cn=config/cn=module{0}.ldif"
SOURCE_MODULE_CONF_FILE="/opt/zextras/common/etc/openldap/zimbra/config/cn=config/cn=module{0}.ldif"
BACKUP_DIR="/opt/zextras/data/ldap-post-install-backup/"
BACKUP_MODULE_CONF_FILE="$BACKUP_DIR/cn=module{0}.ldif.bak"

backup_module_config() {
  echo "* Creating backup of module config file at $BACKUP_MODULE_CONF_FILE"
  mkdir -p "$BACKUP_DIR"
  cp "$TARGET_MODULE_CONF_FILE" "$BACKUP_MODULE_CONF_FILE"
  chown -R "zextras:" "$BACKUP_DIR"
}

update_module_config() {
  echo "* Updating module config $TARGET_MODULE_CONF_FILE"
  cp "$SOURCE_MODULE_CONF_FILE" "$TARGET_MODULE_CONF_FILE"
}

main() {
  if [ ! -f "$TARGET_MODULE_CONF_FILE" ]; then
    echo "Error: Target module config file $TARGET_MODULE_CONF_FILE does not exist."
    exit 1
  fi

  if [ ! -f "$SOURCE_MODULE_CONF_FILE" ]; then
    echo "Error: Source module config file $SOURCE_MODULE_CONF_FILE does not exist."
    exit 1
  fi

  if ! cmp -s "$TARGET_MODULE_CONF_FILE" "$SOURCE_MODULE_CONF_FILE"; then
    backup_module_config
    update_module_config
  fi
}

main
