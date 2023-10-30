#!/bin/bash
#
# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

# Start services
source /opt/zextras/.bashrc

zmcontrol start

# Populate with some data
/opt/zextras/prepare_data.sh

tail -f /dev/null

