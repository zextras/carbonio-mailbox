#!/bin/bash

# SPDX-FileCopyrightText: 2022 Synacor, Inc.
# SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: GPL-2.0-only

if [ "$1" = "" ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  echo "USAGE: Exports LDAP databases"
  echo "Main database: zmslapcat <DIR>"
  echo "Config database: zmslapcat -c <DIR>"
  echo "Accesslog database: zmslapcat -a <DIR>"
  exit 1
fi

D=$(date +%Y%m%d%H%M%S)
CONFIG=no
FILE=ldap.bak.${D}
NFILE=ldap.bak
if [ "$1" = "-c" ]; then
  CONFIG=yes
  FILE=ldap-config.bak.${D}
  NFILE=ldap-config.bak
  DEST=$2
elif [ "$1" = "-a" ]; then
  ALOG=yes
  FILE=ldap-accesslog.bak.${D}
  NFILE=ldap-accesslog.bak
  DEST=$2
else
  DEST=$1
fi

mkdir -p "$DEST"
RC=0
if [ $CONFIG = "yes" ]; then
  /opt/zextras/common/sbin/slapcat -F /opt/zextras/data/ldap/config -n 0 -l "${DEST}/${FILE}"
  RC=$?
elif [ "$ALOG" = "yes" ]; then
  if [ -d /opt/zextras/data/ldap/accesslog/db ]; then
    /opt/zextras/common/sbin/slapcat -F /opt/zextras/data/ldap/config -b "cn=accesslog" -l "${DEST}/${FILE}"
    RC=$?
  else
    exit $RC
  fi
else
  /opt/zextras/common/sbin/slapcat -F /opt/zextras/data/ldap/config -b "" -l "${DEST}/${FILE}"
  RC=$?
fi

cp -f "${DEST}/${FILE}" "${DEST}/${NFILE}"
exit $RC

# /opt/zextras/common/sbin/slapcat -F /opt/zextras/data/ldap/config -b "" -n 1 -l ./backup.ldif
