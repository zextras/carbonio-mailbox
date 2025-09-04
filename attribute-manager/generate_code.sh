#!/bin/bash
#
# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only
#

mvn compile exec:java -Dexec.args="-a generateGetters -c account -i ../store/src/main/resources/conf/attrs -r ../store/src/main/java/com/zimbra/cs/account/ZAttrAccount.java"