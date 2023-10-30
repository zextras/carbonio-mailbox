#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#
set -x

cd /opt/zextras/bin || exit

DOMAIN="carbonio-system.svc.cluster.local"
# Update zextras password
carbonio prov sp zextras password

# Create test user
carbonio prov ca test@"${DOMAIN}" password

# Create admin user
carbonio prov ca admin@"${DOMAIN}" password zimbraIsAdminAccount TRUE

# Create shared account
carbonio prov ca shared@"${DOMAIN}" password displayName "Shared"

carbonio prov grantRight account shared@"${DOMAIN}" usr test@"${DOMAIN}" sendAs
zmmailbox -z -m shared@"${DOMAIN}" mfg / account test@"${DOMAIN}" rwidx
MOUNTPOINT_ID=$(zmmailbox -z -m test@"${DOMAIN}" cm /shared@"${DOMAIN}" shared@"${DOMAIN}" /)
carbonio prov createIdentity shared "Shared" zimbraPrefFromAddress shared@"${DOMAIN}" zimbraPrefFromDisplay "Shared" zimbraPrefWhenInFolderIds "${MOUNTPOINT_ID}" zimbraPrefWhenInFoldersEnabled TRUE zimbraPrefWhenSentToEnabled TRUE
