#!/usr/bin/env bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

echo "Provisioning containers"
docker compose exec mailbox1 bash -c "zmprov cd demo.zextras.io;
zmprov ca test@demo.zextras.io password;
zmprov ca admin@demo.zextras.io password zimbraIsAdminAccount TRUE"

docker compose exec mailbox2 bash -c "zmprov cd demo2.zextras.io;
zmprov ca test2@demo2.zextras.io password;
zmprov ca admin2@demo2.zextras.io password zimbraIsAdminAccount TRUE"