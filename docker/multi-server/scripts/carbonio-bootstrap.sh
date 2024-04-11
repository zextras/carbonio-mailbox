#!/bin/bash

# is carbonio already configured?
if ! grep -q ldap_root_password "/opt/zextras/conf/localconfig.xml"; then
  # if not, let's bootstrap it
  carbonio-bootstrap -c /opt/container.config || exit 1
fi

user_id=$(id -u)

if [[ ${user_id} -ne 0 ]]; then
  /opt/zextras/bin/zmcontrol start || exit 1
else
  su - zextras -c "/opt/zextras/bin/zmcontrol start || exit 1"
fi
