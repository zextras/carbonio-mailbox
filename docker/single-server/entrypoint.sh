#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

# Start services
source /opt/zextras/.bashrc

ldap start

zmprov -l ms $(zmhostname) -zimbraServiceEnabled antivirus -zimbraServiceEnabled amavis -zimbraServiceEnabled stats
zmprov -l mcf zimbraSmtpPort 25
zmprov -l mcf zimbraSmtpHostname 127.0.0.1

zmcontrol start

# Wait until mailbox up

# Populate with some data
/opt/zextras/prepare_data.sh

echo "Keeping the container up..."

tail -f /dev/null
