-- SPDX-FileCopyrightText: 2021 Synacor, Inc.
-- SPDX-FileCopyrightText: 2021 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: AGPL-3.0-only
-- SPDX-License-Identifier: GPL-2.0-only

CREATE DATABASE ${DATABASE_NAME}
DEFAULT CHARACTER SET utf8;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.mail_item (
                                                          mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                          id            INTEGER UNSIGNED NOT NULL,
                                                          type          TINYINT NOT NULL,           -- 1 = folder, 5 = message, etc.
                                                          parent_id     INTEGER UNSIGNED,
                                                          folder_id     INTEGER UNSIGNED,
                                                          prev_folders  TEXT,                       -- e.g. "101:2;110:5", before mod_metadata 101, this item was in folder 2, before 110, it was in 5
                                                          index_id      INTEGER UNSIGNED,
                                                          imap_id       INTEGER UNSIGNED,
                                                          date          INTEGER UNSIGNED NOT NULL,  -- stored as a UNIX-style timestamp
                                                          size          BIGINT UNSIGNED NOT NULL,
                                                          locator       VARCHAR(1024),
    blob_digest   VARCHAR(44) BINARY,         -- reference to blob
    unread        INTEGER UNSIGNED,           -- stored separately from the other flags so we can index it
    flags         INTEGER NOT NULL DEFAULT 0,
    tags          BIGINT NOT NULL DEFAULT 0,
    tag_names     TEXT,
    sender        VARCHAR(128),
    recipients    VARCHAR(128),
    subject       TEXT,
    name          VARCHAR(255),               -- namespace entry for item (e.g. tag name, folder name, document filename)
    metadata      MEDIUMTEXT,
    mod_metadata  INTEGER UNSIGNED NOT NULL,  -- change number for last row modification
    change_date   INTEGER UNSIGNED,           -- UNIX-style timestamp for last row modification
    mod_content   INTEGER UNSIGNED NOT NULL,  -- change number for last change to "content" (e.g. blob)
    uuid          VARCHAR(127),               -- e.g. "d94e42c4-1636-11d9-b904-4dd689d02402"

    PRIMARY KEY (mailbox_id, id),
    INDEX i_type (mailbox_id, type),          -- for looking up folders and tags
    INDEX i_parent_id (mailbox_id, parent_id),-- for looking up a parent\'s children
    INDEX i_folder_id_date (mailbox_id, folder_id, date), -- for looking up by folder and sorting by date
    INDEX i_index_id (mailbox_id, index_id),  -- for looking up based on search results
    INDEX i_date (mailbox_id, date),          -- fallback index in case other constraints are not specified
    INDEX i_mod_metadata (mailbox_id, mod_metadata),      -- used by the sync code
    INDEX i_uuid (mailbox_id, uuid),          -- for looking up by uuid

    UNIQUE INDEX i_name_folder_id (mailbox_id, folder_id, name),   -- for namespace uniqueness

    CONSTRAINT fk_mail_item_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id),
    CONSTRAINT fk_mail_item_parent_id FOREIGN KEY (mailbox_id, parent_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id),
    CONSTRAINT fk_mail_item_folder_id FOREIGN KEY (mailbox_id, folder_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id)
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.mail_item_dumpster (
                                                                   mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                                   id            INTEGER UNSIGNED NOT NULL,
                                                                   type          TINYINT NOT NULL,           -- 1 = folder, 5 = message, etc.
                                                                   parent_id     INTEGER UNSIGNED,
                                                                   folder_id     INTEGER UNSIGNED,
                                                                   prev_folders  TEXT,                       -- e.g. "101:2;110:5", before mod_metadata 101, this item was in folder 2, before 110, it was in 5
                                                                   index_id      INTEGER UNSIGNED,
                                                                   imap_id       INTEGER UNSIGNED,
                                                                   date          INTEGER UNSIGNED NOT NULL,  -- stored as a UNIX-style timestamp
                                                                   size          BIGINT UNSIGNED NOT NULL,
                                                                   locator       VARCHAR(1024),
    blob_digest   VARCHAR(44) BINARY,         -- reference to blob
    unread        INTEGER UNSIGNED,           -- stored separately from the other flags so we can index it
    flags         INTEGER NOT NULL DEFAULT 0,
    tags          BIGINT NOT NULL DEFAULT 0,
    tag_names     TEXT,
    sender        VARCHAR(128),
    recipients    VARCHAR(128),
    subject       TEXT,
    name          VARCHAR(255),               -- namespace entry for item (e.g. tag name, folder name, document filename)
    metadata      MEDIUMTEXT,
    mod_metadata  INTEGER UNSIGNED NOT NULL,  -- change number for last row modification
    change_date   INTEGER UNSIGNED,           -- UNIX-style timestamp for last row modification
    mod_content   INTEGER UNSIGNED NOT NULL,  -- change number for last change to "content" (e.g. blob)
    uuid          VARCHAR(127),               -- e.g. "d94e42c4-1636-11d9-b904-4dd689d02402"

    PRIMARY KEY (mailbox_id, id),
    INDEX i_type (mailbox_id, type),          -- for looking up folders and tags
    INDEX i_parent_id (mailbox_id, parent_id),-- for looking up a parent\'s children
    INDEX i_folder_id_date (mailbox_id, folder_id, date), -- for looking up by folder and sorting by date
    INDEX i_index_id (mailbox_id, index_id),  -- for looking up based on search results
    INDEX i_date (mailbox_id, date),          -- fallback index in case other constraints are not specified
    INDEX i_mod_metadata (mailbox_id, mod_metadata),      -- used by the sync code
    INDEX i_uuid (mailbox_id, uuid),          -- for looking up by uuid

-- Must not enforce unique index on (mailbox_id, folder_id, name) for the dumpster version!

    CONSTRAINT fk_mail_item_dumpster_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id)
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.revision (
                                                         mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                         item_id       INTEGER UNSIGNED NOT NULL,
                                                         version       INTEGER UNSIGNED NOT NULL,
                                                         date          INTEGER UNSIGNED NOT NULL,  -- stored as a UNIX-style timestamp
                                                         size          BIGINT UNSIGNED NOT NULL,
                                                         locator       VARCHAR(1024),
    blob_digest   VARCHAR(44) BINARY,         -- reference to blob
    name          VARCHAR(255),               -- namespace entry for item (e.g. tag name, folder name, document filename)
    metadata      MEDIUMTEXT,
    mod_metadata  INTEGER UNSIGNED NOT NULL,  -- change number for last row modification
    change_date   INTEGER UNSIGNED,           -- UNIX-style timestamp for last row modification
    mod_content   INTEGER UNSIGNED NOT NULL,  -- change number for last change to "content" (e.g. blob)

    PRIMARY KEY (mailbox_id, item_id, version),

    CONSTRAINT fk_revision_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id),
    CONSTRAINT fk_revision_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.revision_dumpster (
                                                                  mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                                  item_id       INTEGER UNSIGNED NOT NULL,
                                                                  version       INTEGER UNSIGNED NOT NULL,
                                                                  date          INTEGER UNSIGNED NOT NULL,  -- stored as a UNIX-style timestamp
                                                                  size          BIGINT UNSIGNED NOT NULL,
                                                                  locator       VARCHAR(1024),
    blob_digest   VARCHAR(44) BINARY,         -- reference to blob
    name          VARCHAR(255),               -- namespace entry for item (e.g. tag name, folder name, document filename)
    metadata      MEDIUMTEXT,
    mod_metadata  INTEGER UNSIGNED NOT NULL,  -- change number for last row modification
    change_date   INTEGER UNSIGNED,           -- UNIX-style timestamp for last row modification
    mod_content   INTEGER UNSIGNED NOT NULL,  -- change number for last change to "content" (e.g. blob)

    PRIMARY KEY (mailbox_id, item_id, version),

    CONSTRAINT fk_revision_dumpster_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id),
    CONSTRAINT fk_revision_dumpster_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item_dumpster(mailbox_id, id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.tag (
                                                    mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                    id            INTEGER NOT NULL,
                                                    name          VARCHAR(128) NOT NULL,
    color         BIGINT,
    item_count    INTEGER NOT NULL DEFAULT 0,
    unread        INTEGER NOT NULL DEFAULT 0,
    listed        BOOLEAN NOT NULL DEFAULT FALSE,
    sequence      INTEGER UNSIGNED NOT NULL,  -- change number for rename/recolor/etc.
    policy        VARCHAR(1024),

    PRIMARY KEY (mailbox_id, id),
    UNIQUE INDEX i_tag_name (mailbox_id, name),
    CONSTRAINT fk_tag_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id)
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.tagged_item (
                                                            mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                            tag_id        INTEGER NOT NULL,
                                                            item_id       INTEGER UNSIGNED NOT NULL,

                                                            UNIQUE INDEX i_tagged_item_unique (mailbox_id, tag_id, item_id),
    CONSTRAINT fk_tagged_item_tag FOREIGN KEY (mailbox_id, tag_id) REFERENCES ${DATABASE_NAME}.tag(mailbox_id, id) ON DELETE CASCADE,
    CONSTRAINT fk_tagged_item_item FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.open_conversation (
                                                                  mailbox_id  INTEGER UNSIGNED NOT NULL,
                                                                  hash        CHAR(28) BINARY NOT NULL,
    conv_id     INTEGER UNSIGNED NOT NULL,

    PRIMARY KEY (mailbox_id, hash),
    INDEX i_conv_id (mailbox_id, conv_id),
    CONSTRAINT fk_open_conversation_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id),
    CONSTRAINT fk_open_conversation_conv_id FOREIGN KEY (mailbox_id, conv_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.appointment (
                                                            mailbox_id  INTEGER UNSIGNED NOT NULL,
                                                            uid         VARCHAR(255) NOT NULL,
    item_id     INTEGER UNSIGNED NOT NULL,
    start_time  DATETIME NOT NULL,
    end_time    DATETIME,

    PRIMARY KEY (mailbox_id, uid),
    CONSTRAINT fk_appointment_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id),
    CONSTRAINT fk_appointment_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE UNIQUE INDEX i_item_id ON ${DATABASE_NAME}.appointment (mailbox_id, item_id);

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.appointment_dumpster (
                                                                     mailbox_id  INTEGER UNSIGNED NOT NULL,
                                                                     uid         VARCHAR(255) NOT NULL,
    item_id     INTEGER UNSIGNED NOT NULL,
    start_time  DATETIME NOT NULL,
    end_time    DATETIME,

    PRIMARY KEY (mailbox_id, uid),
    CONSTRAINT fk_appointment_dumpster_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id),
    CONSTRAINT fk_appointment_dumpster_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item_dumpster(mailbox_id, id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE UNIQUE INDEX i_item_id ON ${DATABASE_NAME}.appointment_dumpster (mailbox_id, item_id);

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.tombstone (
                                                          mailbox_id  INTEGER UNSIGNED NOT NULL,
                                                          sequence    INTEGER UNSIGNED NOT NULL,  -- change number for deletion
                                                          date        INTEGER UNSIGNED NOT NULL,  -- deletion date as a UNIX-style timestamp
                                                          type        TINYINT,                    -- 1 = folder, 3 = tag, etc.
                                                          ids         TEXT,

                                                          INDEX i_sequence (mailbox_id, sequence),
    CONSTRAINT fk_tombstone_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id)
    ) ENGINE = InnoDB;

-- Tracks UID's of messages on remote POP3 servers
CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.pop3_message (
                                                             mailbox_id     INTEGER UNSIGNED NOT NULL,
                                                             data_source_id CHAR(36) NOT NULL,
    uid            VARCHAR(255) BINARY NOT NULL,
    item_id        INTEGER UNSIGNED NOT NULL,

    PRIMARY KEY (mailbox_id, item_id),
    CONSTRAINT fk_pop3_message_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id)
    ) ENGINE = InnoDB;

CREATE UNIQUE INDEX i_uid_pop3_id ON ${DATABASE_NAME}.pop3_message (uid, data_source_id);

-- Tracks folders on remote IMAP servers
CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.imap_folder (
                                                            mailbox_id         INTEGER UNSIGNED NOT NULL,
                                                            item_id            INTEGER UNSIGNED NOT NULL,
                                                            data_source_id     CHAR(36) NOT NULL,
    local_path         VARCHAR(1000) NOT NULL,
    remote_path        VARCHAR(1000) NOT NULL,
    uid_validity       INTEGER UNSIGNED,

    PRIMARY KEY (mailbox_id, item_id),
    CONSTRAINT fk_imap_folder_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE UNIQUE INDEX i_local_path
    ON ${DATABASE_NAME}.imap_folder (local_path(200), data_source_id, mailbox_id);

CREATE UNIQUE INDEX i_remote_path
    ON ${DATABASE_NAME}.imap_folder (remote_path(200), data_source_id, mailbox_id);

-- Tracks messages on remote IMAP servers
CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.imap_message (
                                                             mailbox_id     INTEGER UNSIGNED NOT NULL,
                                                             imap_folder_id INTEGER UNSIGNED NOT NULL,
                                                             uid            BIGINT NOT NULL,
                                                             item_id        INTEGER UNSIGNED NOT NULL,
                                                             flags          INTEGER NOT NULL DEFAULT 0,

                                                             PRIMARY KEY (mailbox_id, item_id),
    CONSTRAINT fk_imap_message_mailbox_id FOREIGN KEY (mailbox_id)
    REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
    CONSTRAINT fk_imap_message_imap_folder_id FOREIGN KEY (mailbox_id, imap_folder_id)
    REFERENCES ${DATABASE_NAME}.imap_folder(mailbox_id, item_id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE UNIQUE INDEX i_uid_imap_id ON ${DATABASE_NAME}.imap_message (mailbox_id, imap_folder_id, uid);

/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

-- Tracks local MailItem created from remote objects via DataSource
CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.data_source_item (
                                                                 mailbox_id     INTEGER UNSIGNED NOT NULL,
                                                                 data_source_id CHAR(36) NOT NULL,
    item_id        INTEGER UNSIGNED NOT NULL,
    folder_id      INTEGER UNSIGNED NOT NULL DEFAULT 0,
    remote_id      VARCHAR(255) BINARY NOT NULL,
    metadata       MEDIUMTEXT,

    PRIMARY KEY (mailbox_id, item_id),
    UNIQUE INDEX i_remote_id (mailbox_id, data_source_id, remote_id),   -- for reverse lookup
    CONSTRAINT fk_data_source_item_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.purged_conversations (
                                                                     mailbox_id     INTEGER UNSIGNED NOT NULL,
                                                                     data_source_id CHAR(36) NOT NULL,
    item_id        INTEGER UNSIGNED NOT NULL,
    hash           CHAR(28) BINARY NOT NULL,

    PRIMARY KEY (mailbox_id, data_source_id, hash),
    CONSTRAINT fk_purged_conversation_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.purged_messages (
                                                                mailbox_id       INTEGER UNSIGNED NOT NULL,
                                                                data_source_id   CHAR(36) NOT NULL,
    item_id          INTEGER UNSIGNED NOT NULL,
    parent_id        INTEGER UNSIGNED,
    remote_id        VARCHAR(255) BINARY NOT NULL,
    remote_folder_id VARCHAR(255) BINARY NOT NULL,
    purge_date       INTEGER UNSIGNED,

    PRIMARY KEY (mailbox_id, data_source_id, item_id),
    CONSTRAINT fk_purged_message_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.event (
                                                      mailbox_id    INTEGER UNSIGNED NOT NULL,
                                                      account_id    VARCHAR(36) NOT NULL,  -- user performing the action (email address or guid)
    item_id       INTEGER NOT NULL,  -- itemId for the event
    folder_id     INTEGER NOT NULL,  -- folderId for the item in the event
    op            TINYINT NOT NULL,  -- operation
    ts            INTEGER NOT NULL,  -- timestamp
    version       INTEGER,           -- version of the item
    user_agent    VARCHAR(128),      -- identifier of device if available
    arg           VARCHAR(10240),    -- operation specific argument

    CONSTRAINT fk_event_mailbox_id FOREIGN KEY (mailbox_id) REFERENCES zimbra.mailbox(id) ON DELETE CASCADE
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ${DATABASE_NAME}.watch (
                                                      mailbox_id   INTEGER UNSIGNED NOT NULL,
                                                      target       VARCHAR(36) NOT NULL,  -- watch target account id
    item_id      INTEGER NOT NULL,  -- target item id

    PRIMARY KEY (mailbox_id, target, item_id)
    ) ENGINE = InnoDB;