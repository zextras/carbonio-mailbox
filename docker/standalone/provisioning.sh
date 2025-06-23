#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

echo "Provisioning containers"

docker compose exec mailbox1 bash -c "> /tmp/prov.ls && cat > /tmp/prov.ls <<EOF
cd demo.zextras.io
ca test@demo.zextras.io password
ca admin@demo.zextras.io password zimbraIsAdminAccount TRUE
EOF
zmprov < /tmp/prov.ls"

docker compose exec mailbox2 bash -c "> /tmp/prov2.ls && cat > /tmp/prov2.ls <<EOF
cd demo2.zextras.io
ca test2@demo2.zextras.io password
ca admin2@demo2.zextras.io password zimbraIsAdminAccount TRUE
EOF
zmprov < /tmp/prov2.ls"

echo "Provisioning completed"