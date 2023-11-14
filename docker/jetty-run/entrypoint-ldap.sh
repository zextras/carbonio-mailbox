#!/bin/bash

# The container 'carbonio/ce-ldap-u20' is already bootstrapped with:
# - server: "ldap.mail.local" configured with zimbraServiceEnabled 'directory-server' and some other stuff.
# - domain: "mail.local"
# We only need to:
# - update the schemas and the configurations with those we have just built
# - create an appserver, a domain and a couple of accounts

touch /.dockerenv
su - zextras -c "/opt/zextras/bin/ldap stop"
su - zextras -c "/opt/zextras/libexec/zmldapschema 2>/dev/null"
su - zextras -c "/opt/zextras/libexec/zmldapupdateldif"
su - zextras -c "/opt/zextras/libexec/ldapattributeupdate"
su - zextras -c "/opt/zextras/bin/ldap start"
su - zextras -c "/opt/zextras/bin/zmprov createDomain demo.zextras.io"
su - zextras -c "/opt/zextras/bin/zmprov createServer docker.app.local zimbraServiceEnabled service zimbraServiceEnabled mailbox"
su - zextras -c "/opt/zextras/bin/zmprov createAccount docker-admin@demo.zextras.io admin-password zimbraMailHost docker.app.local zimbraIsAdminAccount TRUE"
su - zextras -c "/opt/zextras/bin/zmprov createAccount docker-user@demo.zextras.io user-password zimbraMailHost docker.app.local"
monit -c /etc/monitrc
