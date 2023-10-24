#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

touch /.dockerenv
su - zextras -c "/opt/zextras/bin/ldap stop"
su - zextras -c "/opt/zextras/libexec/zmldapschema 2>/dev/null"
su - zextras -c "/opt/zextras/libexec/zmldapupdateldif"
su - zextras -c "/opt/zextras/libexec/ldapattributeupdate"
su - zextras -c "/opt/zextras/bin/ldap start"
su - zextras -c "/opt/zextras/bin/zmprov cs docker.app.local zimbraServiceEnabled service"
su - zextras -c "/opt/zextras/bin/zmprov ca docker-zextras@mail.local account-password zimbraIsAdminAccount TRUE zimbraMailTransport fake"
monit -c /etc/monitrc
