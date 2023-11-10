-- SPDX-FileCopyrightText: 2021 Synacor, Inc.
-- SPDX-FileCopyrightText: 2021 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only
-- SPDX-License-Identifier: GPL-2.0-only

-- **DO NOT USE IN PRODUCTION**
-- INTENDED TO BE USED BY DOCKER LOCAL RUN SETUP ONLY

-- This script updates db and index version values in order to make the mailbox start
-- See Versions.java file for reference on current accepted values

USE zimbra;

INSERT INTO config (name, value, description, modified)
       VALUES ("db.version", "111", "DB version", CURRENT_TIMESTAMP()),
               ("index.version", "2", "Index version", CURRENT_TIMESTAMP());
