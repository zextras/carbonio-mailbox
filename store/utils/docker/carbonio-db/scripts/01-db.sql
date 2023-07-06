-- SPDX-FileCopyrightText: 2021 Synacor, Inc.
-- SPDX-FileCopyrightText: 2021 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only
-- SPDX-License-Identifier: GPL-2.0-only

CREATE DATABASE zimbra;
ALTER DATABASE zimbra DEFAULT CHARACTER SET utf8;

USE zimbra;

GRANT ALL ON zimbra.* TO 'zextras' IDENTIFIED BY 'zextras';
GRANT ALL ON zimbra.* TO 'zextras'@'localhost' IDENTIFIED BY 'zextras';
GRANT ALL ON zimbra.* TO 'zextras'@'localhost.localdomain' IDENTIFIED BY 'zextras';
GRANT ALL ON zimbra.* TO 'root'@'localhost.localdomain' IDENTIFIED BY 'zextras';

-- The zextras user needs to be able to create and drop databases and perform
-- backup and restore operations.  Give
-- zimbra root access for now to keep things simple until there is a need
-- to add more security.
GRANT ALL ON *.* TO 'zextras' WITH GRANT OPTION;
GRANT ALL ON *.* TO 'zextras'@'localhost' WITH GRANT OPTION;
GRANT ALL ON *.* TO 'zextras'@'localhost.localdomain' WITH GRANT OPTION;
GRANT ALL ON *.* TO 'root'@'localhost.localdomain' WITH GRANT OPTION;

-- -----------------------------------------------------------------------
-- volumes
-- -----------------------------------------------------------------------

-- list of known volumes
CREATE TABLE volume (
   id                     TINYINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
   type                   TINYINT NOT NULL,   -- 1 = primary msg, 2 = secondary msg, 10 = index
   name                   VARCHAR(255) NOT NULL,
   path                   TEXT NOT NULL,
   file_bits              SMALLINT NOT NULL,
   file_group_bits        SMALLINT NOT NULL,
   mailbox_bits           SMALLINT NOT NULL,
   mailbox_group_bits     SMALLINT NOT NULL,
   compress_blobs         BOOLEAN NOT NULL,
   compression_threshold  BIGINT NOT NULL,
   metadata               MEDIUMTEXT,

   UNIQUE INDEX i_name (name),
   UNIQUE INDEX i_path (path(255))   -- Index prefix length of 255 is the max prior to MySQL 4.1.2.  Should be good enough.
) ENGINE = InnoDB;

-- This table has only one row.  It points to message and index volumes
-- to use for newly provisioned mailboxes.
CREATE TABLE current_volumes (
   message_volume_id            TINYINT UNSIGNED NOT NULL,
   secondary_message_volume_id  TINYINT UNSIGNED,
   index_volume_id              TINYINT UNSIGNED NOT NULL,
   next_mailbox_id              INTEGER UNSIGNED NOT NULL,

   INDEX i_message_volume_id (message_volume_id),
   INDEX i_secondary_message_volume_id (secondary_message_volume_id),
   INDEX i_index_volume_id (index_volume_id),

   CONSTRAINT fk_current_volumes_message_volume_id FOREIGN KEY (message_volume_id) REFERENCES volume(id),
   CONSTRAINT fk_current_volumes_secondary_message_volume_id FOREIGN KEY (secondary_message_volume_id) REFERENCES volume(id),
   CONSTRAINT fk_current_volumes_index_volume_id FOREIGN KEY (index_volume_id) REFERENCES volume(id)
) ENGINE = InnoDB;

INSERT INTO volume (id, type, name, path, file_bits, file_group_bits,
    mailbox_bits, mailbox_group_bits, compress_blobs, compression_threshold)
  VALUES (1, 1, 'message1', '/opt/zextras/store', 12, 8, 12, 8, 0, 4096);
INSERT INTO volume (id, type, name, path, file_bits, file_group_bits,
    mailbox_bits, mailbox_group_bits, compress_blobs, compression_threshold)
  VALUES (2, 10, 'index1',   '/opt/zextras/index', 12, 8, 12, 8, 0, 4096);

INSERT INTO current_volumes (message_volume_id, index_volume_id, next_mailbox_id) VALUES (1, 2, 1);
COMMIT;

create table volume_blobs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  volume_id TINYINT NOT NULL,
  mailbox_id INTEGER NOT NULL,
  item_id INTEGER NOT NULL,
  revision INTEGER NOT NULL,
  blob_digest VARCHAR(44),
  processed BOOLEAN default false,

  INDEX i_blob_digest (blob_digest),

  CONSTRAINT uc_blobinfo UNIQUE (volume_id,mailbox_id,item_id,revision)
  -- FK constraints disabled for now; maybe enable them in 9.0 when we have time to deal with delete cases
  -- CONSTRAINT fk_volume_blobs_volume_id FOREIGN KEY (volume_id) REFERENCES volume(id),
  -- CONSTRAINT fk_volume_blobs_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES mailbox(id)
);

-- -----------------------------------------------------------------------
-- mailbox info
-- -----------------------------------------------------------------------

CREATE TABLE mailbox (
   id                  INTEGER UNSIGNED NOT NULL PRIMARY KEY,
   group_id            INTEGER UNSIGNED NOT NULL,  -- mailbox group
   account_id          VARCHAR(127) NOT NULL,      -- e.g. "d94e42c4-1636-11d9-b904-4dd689d02402"
   index_volume_id     TINYINT UNSIGNED NOT NULL,
   item_id_checkpoint  INTEGER UNSIGNED NOT NULL DEFAULT 0,
   contact_count       INTEGER UNSIGNED DEFAULT 0,
   size_checkpoint     BIGINT UNSIGNED NOT NULL DEFAULT 0,
   change_checkpoint   INTEGER UNSIGNED NOT NULL DEFAULT 0,
   tracking_sync       INTEGER UNSIGNED NOT NULL DEFAULT 0,
   tracking_imap       BOOLEAN NOT NULL DEFAULT 0,
   last_backup_at      INTEGER UNSIGNED,           -- last full backup time, UNIX-style timestamp
   comment             VARCHAR(255),               -- usually the main email address originally associated with the mailbox
   last_soap_access    INTEGER UNSIGNED NOT NULL DEFAULT 0,
   new_messages        INTEGER UNSIGNED NOT NULL DEFAULT 0,
   idx_deferred_count  INTEGER NOT NULL DEFAULT 0, -- deprecated
   highest_indexed     VARCHAR(21), -- deprecated
   version             VARCHAR(16),
   last_purge_at       INTEGER UNSIGNED NOT NULL DEFAULT 0,
   itemcache_checkpoint       INTEGER UNSIGNED NOT NULL DEFAULT 0,

   UNIQUE INDEX i_account_id (account_id),
   INDEX i_index_volume_id (index_volume_id),
   INDEX i_last_backup_at (last_backup_at, id),

   CONSTRAINT fk_mailbox_index_volume_id FOREIGN KEY (index_volume_id) REFERENCES volume(id)
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------
-- deleted accounts
-- -----------------------------------------------------------------------

CREATE TABLE deleted_account (
   email       VARCHAR(255) NOT NULL PRIMARY KEY,
   account_id  VARCHAR(127) NOT NULL,
   mailbox_id  INTEGER UNSIGNED NOT NULL,
   deleted_at  INTEGER UNSIGNED NOT NULL      -- UNIX-style timestamp
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------
-- mailbox metadata info
-- -----------------------------------------------------------------------

CREATE TABLE mailbox_metadata (
   mailbox_id  INTEGER UNSIGNED NOT NULL,
   section     VARCHAR(64) NOT NULL,       -- e.g. "imap"
   metadata    MEDIUMTEXT,

   PRIMARY KEY (mailbox_id, section),

   CONSTRAINT fk_metadata_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES mailbox(id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------
-- out-of-office reply history
-- -----------------------------------------------------------------------

CREATE TABLE out_of_office (
   mailbox_id  INTEGER UNSIGNED NOT NULL,
   sent_to     VARCHAR(255) NOT NULL,
   sent_on     DATETIME NOT NULL,

   PRIMARY KEY (mailbox_id, sent_to),
   INDEX i_sent_on (sent_on),

   CONSTRAINT fk_out_of_office_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES mailbox(id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- -----------------------------------------------------------------------
-- etc.
-- -----------------------------------------------------------------------

-- table for global config params
CREATE TABLE config (
   name         VARCHAR(255) NOT NULL PRIMARY KEY,
   value        TEXT,
   description  TEXT,
   modified     TIMESTAMP
) ENGINE = InnoDB;

-- table for tracking database table maintenance
CREATE TABLE table_maintenance (
   database_name       VARCHAR(64) NOT NULL,
   table_name          VARCHAR(64) NOT NULL,
   maintenance_date    DATETIME NOT NULL,
   last_optimize_date  DATETIME,
   num_rows            INTEGER UNSIGNED NOT NULL,

   PRIMARY KEY (table_name, database_name)
) ENGINE = InnoDB;

CREATE TABLE service_status (
   server   VARCHAR(255) NOT NULL,
   service  VARCHAR(255) NOT NULL,
   time     DATETIME,
   status   BOOLEAN,

   UNIQUE INDEX i_server_service (server(100), service(100))
) ENGINE = MyISAM;

-- Tracks scheduled tasks
CREATE TABLE scheduled_task (
   class_name       VARCHAR(255) BINARY NOT NULL,
   name             VARCHAR(255) NOT NULL,
   mailbox_id       INTEGER UNSIGNED NOT NULL,
   exec_time        DATETIME,
   interval_millis  INTEGER UNSIGNED,
   metadata         MEDIUMTEXT,

   PRIMARY KEY (name, mailbox_id, class_name),
   CONSTRAINT fk_st_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES mailbox(id) ON DELETE CASCADE,
   INDEX i_mailbox_id (mailbox_id)
) ENGINE = InnoDB;

-- Tracks ACLs to be pushed to LDAP
CREATE TABLE pending_acl_push (
   mailbox_id  INTEGER UNSIGNED NOT NULL,
   item_id     INTEGER UNSIGNED NOT NULL,
   date        BIGINT UNSIGNED NOT NULL,

   PRIMARY KEY (mailbox_id, item_id, date),
   CONSTRAINT fk_pending_acl_push_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES mailbox(id) ON DELETE CASCADE,
   INDEX i_date (date)
) ENGINE = InnoDB;

CREATE TABLE current_sessions (
	id				INTEGER UNSIGNED NOT NULL,
	server_id		VARCHAR(127) NOT NULL,
	PRIMARY KEY (id, server_id)
) ENGINE = InnoDB;

-- ZMG Devices
CREATE TABLE zmg_devices (
   mailbox_id          INTEGER UNSIGNED NOT NULL,
   app_id              VARCHAR(64) NOT NULL,
   reg_id              VARCHAR(255) NOT NULL,
   push_provider       VARCHAR(8) NOT NULL,
   os_name             VARCHAR(16),
   os_version          VARCHAR(8),
   max_payload_size    INTEGER UNSIGNED,

   PRIMARY KEY (mailbox_id, app_id),
   CONSTRAINT uk_zmg_reg_id UNIQUE KEY (reg_id),
   CONSTRAINT fk_zmg_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES mailbox(id) ON DELETE CASCADE,
   INDEX i_mailbox_id (mailbox_id),
   INDEX i_reg_id (reg_id)
) ENGINE = InnoDB;

-- Support for Zimbra Chat
CREATE DATABASE `chat`
DEFAULT CHARACTER SET utf8;

CREATE TABLE `chat`.`USER` (
   `ID` int(11) NOT NULL AUTO_INCREMENT,
   `ADDRESS` varchar(256) NOT NULL,
PRIMARY KEY (`ID`)
) ENGINE = InnoDB;

CREATE TABLE `chat`.`RELATIONSHIP` (
   `USERID` int(11) NOT NULL,
   `TYPE` tinyint(4) NOT NULL,
   `BUDDYADDRESS` varchar(256) NOT NULL,
   `BUDDYNICKNAME` varchar(128) NOT NULL,
   `GROUP` varchar(256) NOT NULL DEFAULT ''
) ENGINE = InnoDB;

CREATE TABLE `chat`.`EVENTMESSAGE` (
   `ID` int(11) NOT NULL AUTO_INCREMENT,
   `USERID` int(11) NOT NULL,
   `EVENTID` varchar(36) DEFAULT NULL,
   `SENDER` varchar(256) NOT NULL,
   `TIMESTAMP` bigint(20) DEFAULT NULL,
   `MESSAGE` text,
PRIMARY KEY (`ID`)
) ENGINE = InnoDB;

CREATE TABLE `chat`.`MESSAGE` (
   `ID`             VARCHAR(48)   NOT NULL,
   `SENT_TIMESTAMP` BIGINT        NOT NULL,
   `EDIT_TIMESTAMP` BIGINT        DEFAULT 0,
   `MESSAGE_TYPE`   TINYINT       DEFAULT 0,
   `TARGET_TYPE`    TINYINT       DEFAULT 0,
   `INDEX_STATUS`   TINYINT       DEFAULT 0,
   `SENDER`         VARCHAR(256) NOT NULL,
   `DESTINATION`    VARCHAR(256) NOT NULL,
   `TEXT`           VARCHAR(4096),
   `REACTIONS`      VARCHAR(4096),
   `TYPE_EXTRAINFO` VARCHAR(4096)
) ENGINE = InnoDB;

CREATE INDEX INDEX_SENT
   ON chat.`MESSAGE` (SENT_TIMESTAMP);
CREATE INDEX INDEX_EDIT
   ON chat.`MESSAGE` (EDIT_TIMESTAMP);
CREATE INDEX INDEX_FROM
   ON chat.`MESSAGE` (SENDER);
CREATE INDEX INDEX_TO
   ON chat.`MESSAGE` (DESTINATION);
CREATE INDEX INDEX_TEXT
   ON chat.`MESSAGE` (`TEXT`(191));

CREATE TABLE `chat`.`MESSAGE_READ` (
   `SENDER`                VARCHAR(256) NOT NULL,
   `DESTINATION`           VARCHAR(256) NOT NULL,
   `TIMESTAMP`             BIGINT       NOT NULL,
   `MESSAGE_ID`            VARCHAR(48)
) ENGINE = InnoDB;

CREATE INDEX INDEX_MESSAGE_READ
   ON chat.`MESSAGE_READ` (`SENDER`(191), `DESTINATION`(191));
CREATE INDEX INDEX_READ_TIMESTAMP
   ON chat.`MESSAGE_READ` (TIMESTAMP);

CREATE TABLE `chat`.`SPACE` (
   `ADDRESS`  VARCHAR(256) NOT NULL,
   `TOPIC`    VARCHAR(256),
   `RESOURCE` VARCHAR(4096),
   `NAME` VARCHAR(256) DEFAULT '' NOT NULL
) ENGINE = InnoDB;

CREATE INDEX INDEX_SPACE
   ON chat.`SPACE` (`ADDRESS`(191));

CREATE TABLE `chat`.`CHANNEL` (
   `ADDRESS`        VARCHAR(256) NOT NULL,
   `CHANNEL_NAME`   VARCHAR(128) NOT NULL,
   `TOPIC`          VARCHAR(256),
   `IS_INVITE_ONLY` BOOLEAN
) ENGINE = InnoDB;

CREATE INDEX INDEX_CHANNEL
   ON chat.`CHANNEL` (`ADDRESS`(191));

CREATE TABLE `chat`.`GROUP` (
   `ADDRESS`      VARCHAR(256) NOT NULL,
   `TOPIC`        VARCHAR(256),
   `IMAGE`        MEDIUMBLOB DEFAULT NULL,
   `IMAGE_UPDATE` BIGINT DEFAULT NULL
) ENGINE = InnoDB;
CREATE INDEX INDEX_GROUP
   ON chat.`GROUP` (`ADDRESS`(191));

CREATE TABLE `chat`.`SUBSCRIPTION` (
   `ADDRESS`            VARCHAR(256) NOT NULL,
   `GROUP_ADDRESS`      VARCHAR(256) NOT NULL,
   `JOINED_TIMESTAMP`   BIGINT,
   `LEFT_TIMESTAMP`     BIGINT,
   `BANNED`             BOOLEAN,
   `CAN_ACCESS_ARCHIVE` BOOLEAN
) ENGINE = InnoDB;

CREATE INDEX INDEX_SUBSCRIPTION
   ON chat.`SUBSCRIPTION` (`ADDRESS`(191), `GROUP_ADDRESS`(191));

CREATE TABLE `chat`.`OWNER` (
   `ADDRESS`            VARCHAR(256) NOT NULL,
   `GROUP_ADDRESS`      VARCHAR(256) NOT NULL
) ENGINE = InnoDB;

CREATE INDEX INDEX_OWNER
   ON chat.`OWNER` (`ADDRESS`(191), `GROUP_ADDRESS`(191));

CREATE TABLE `chat`.`USERV3` (
  `ID`             VARCHAR(256) NOT NULL,
  `LAST_SEEN`      BIGINT       DEFAULT NULL,
  `STATUS_MESSAGE` VARCHAR(256) DEFAULT '' NOT NULL,
  `IMAGE`          MEDIUMBLOB   DEFAULT NULL,
  `IMAGE_UPDATE`   BIGINT       DEFAULT NULL
) ENGINE = InnoDB;

CREATE INDEX `INDEX_USERV3` ON `chat`.`USERV3` (`ID`);

CREATE TABLE `chat`.`FIREBASE` (
  `TOKEN`       VARCHAR(256) NOT NULL,
  `ACCOUNT_ID`  VARCHAR(256) NOT NULL,
  `API_VERSION` INTEGER NOT NULL
) ENGINE = InnoDB;
CREATE INDEX `INDEX_FIREBASE` ON `chat`.`FIREBASE` (`TOKEN`);

CREATE TABLE `chat`.`USER_ROOM_JOIN` (
  `ACCOUNT_ID`    VARCHAR(256) NOT NULL,
  `ROOM_ADDRESS`  VARCHAR(256) NOT NULL,
  `DELETED_ON`    BIGINT DEFAULT NULL
) ENGINE = InnoDB;
CREATE INDEX `INDEX_USER_ROOM_JOIN` ON `chat`.`USER_ROOM_JOIN` (`ACCOUNT_ID`, `ROOM_ADDRESS`);

CREATE TABLE `chat`.`CHANNELV3` (
  `ADDRESS`        VARCHAR(256) NOT NULL,
  `SPACE_ID`       VARCHAR(256) NOT NULL,
  `NAME`           VARCHAR(128) NOT NULL,
  `TOPIC`          VARCHAR(256),
  `IS_INVITE_ONLY` BOOLEAN DEFAULT FALSE,
  `IMAGE`          MEDIUMBLOB DEFAULT NULL,
  `IMAGE_UPDATE`   BIGINT DEFAULT NULL
) ENGINE = InnoDB;
CREATE INDEX `INDEX_CHANNELV3_ADDRESS` ON `chat`.`CHANNELV3` (`ADDRESS`);
CREATE INDEX `INDEX_CHANNELV3_SPACE_ID` ON `chat`.`CHANNELV3` (`SPACE_ID`);

CREATE TABLE `chat`.`VISIBILITY` (
  `ADDRESS`         VARCHAR(256) NOT NULL,
  `ACCOUNT_ID`      VARCHAR(256) NOT NULL,
  `START_TIMESTAMP` BIGINT       NOT NULL
) ENGINE = InnoDB;
CREATE INDEX `INDEX_VISIBILITY` ON `chat`.`VISIBILITY` (`ACCOUNT_ID`,`ADDRESS`);
CREATE INDEX `INDEX_VISIBILITY_ADDRESS` ON `chat`.`VISIBILITY` (`ADDRESS`);

CREATE TABLE `chat`.`INSTANT_MEETING` (
  `ADDRESS`   VARCHAR(256) NOT NULL,
  `NAME`      VARCHAR(256),
  `PASSWORD`  VARCHAR(256)
) ENGINE = InnoDB;
CREATE INDEX `INDEX_INSTANT_MEETING` ON `chat`.`INSTANT_MEETING` (`ADDRESS`);

CREATE TABLE `chat`.`CONVERSATION_OPTIONS` (
  `ACCOUNT_ID`      VARCHAR(256) NOT NULL,
  `ADDRESS`         VARCHAR(256) NOT NULL,
  `NOTIFICATIONS`   BOOLEAN DEFAULT TRUE
) ENGINE = InnoDB;
CREATE INDEX `INDEX_CONVERSATION_OPTIONS` ON `chat`.`CONVERSATION_OPTIONS` (`ACCOUNT_ID`,`ADDRESS`);
CREATE INDEX `INDEX_CONVERSATION_OPTIONS_ACCOUNT_ID` ON `chat`.`CONVERSATION_OPTIONS` (`ACCOUNT_ID`);
CREATE INDEX `INDEX_CONVERSATION_OPTIONS_ADDRESS` ON `chat`.`CONVERSATION_OPTIONS` (`ADDRESS`);
