-- SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: GPL-2.0-only

-- PostgreSQL port of ../create_database.sql: one schema per mailbox group. Type mapping as in
-- db.sql. Index names are schema-wide in PostgreSQL, so they carry the table name.
-- Name uniqueness is case-insensitive in MariaDB (utf8_general_ci); upper(name) keeps that for
-- case. utf8_general_ci also folds accents, PostgreSQL does not.

CREATE SCHEMA ${DATABASE_NAME};

CREATE TABLE ${DATABASE_NAME}.mail_item (
   mailbox_id    INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   id            INTEGER NOT NULL,
   type          SMALLINT NOT NULL,
   parent_id     INTEGER,
   folder_id     INTEGER,
   prev_folders  TEXT,
   index_id      INTEGER,
   imap_id       INTEGER,
   date          BIGINT NOT NULL,
   size          BIGINT NOT NULL,
   locator       VARCHAR(1024),
   blob_digest   VARCHAR(44) COLLATE "C",
   unread        INTEGER,
   flags         INTEGER NOT NULL DEFAULT 0,
   tags          BIGINT NOT NULL DEFAULT 0,
   tag_names     TEXT,
   sender        VARCHAR(128),
   recipients    VARCHAR(128),
   subject       TEXT,
   name          VARCHAR(255),
   metadata      TEXT,
   mod_metadata  INTEGER NOT NULL,
   change_date   BIGINT,
   mod_content   INTEGER NOT NULL,
   uuid          VARCHAR(127),

   PRIMARY KEY (mailbox_id, id),
   CONSTRAINT fk_mail_item_parent_id FOREIGN KEY (mailbox_id, parent_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id),
   CONSTRAINT fk_mail_item_folder_id FOREIGN KEY (mailbox_id, folder_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id)
);
CREATE INDEX i_mail_item_type ON ${DATABASE_NAME}.mail_item (mailbox_id, type);
CREATE INDEX i_mail_item_parent_id ON ${DATABASE_NAME}.mail_item (mailbox_id, parent_id);
CREATE INDEX i_mail_item_folder_id_date ON ${DATABASE_NAME}.mail_item (mailbox_id, folder_id, date);
CREATE INDEX i_mail_item_index_id ON ${DATABASE_NAME}.mail_item (mailbox_id, index_id);
CREATE INDEX i_mail_item_date ON ${DATABASE_NAME}.mail_item (mailbox_id, date);
CREATE INDEX i_mail_item_mod_metadata ON ${DATABASE_NAME}.mail_item (mailbox_id, mod_metadata);
CREATE INDEX i_mail_item_locator ON ${DATABASE_NAME}.mail_item (mailbox_id, locator);
CREATE INDEX i_mail_item_uuid ON ${DATABASE_NAME}.mail_item (mailbox_id, uuid);
CREATE UNIQUE INDEX i_name_folder_id ON ${DATABASE_NAME}.mail_item (mailbox_id, folder_id, upper(name));

CREATE TABLE ${DATABASE_NAME}.mail_item_dumpster (
   mailbox_id    INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   id            INTEGER NOT NULL,
   type          SMALLINT NOT NULL,
   parent_id     INTEGER,
   folder_id     INTEGER,
   prev_folders  TEXT,
   index_id      INTEGER,
   imap_id       INTEGER,
   date          BIGINT NOT NULL,
   size          BIGINT NOT NULL,
   locator       VARCHAR(1024),
   blob_digest   VARCHAR(44) COLLATE "C",
   unread        INTEGER,
   flags         INTEGER NOT NULL DEFAULT 0,
   tags          BIGINT NOT NULL DEFAULT 0,
   tag_names     TEXT,
   sender        VARCHAR(128),
   recipients    VARCHAR(128),
   subject       TEXT,
   name          VARCHAR(255),
   metadata      TEXT,
   mod_metadata  INTEGER NOT NULL,
   change_date   BIGINT,
   mod_content   INTEGER NOT NULL,
   uuid          VARCHAR(127),

   PRIMARY KEY (mailbox_id, id)
   -- no (mailbox_id, folder_id, name) uniqueness in the dumpster
);
CREATE INDEX i_mail_item_dumpster_type ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, type);
CREATE INDEX i_mail_item_dumpster_parent_id ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, parent_id);
CREATE INDEX i_mail_item_dumpster_folder_id_date ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, folder_id, date);
CREATE INDEX i_mail_item_dumpster_index_id ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, index_id);
CREATE INDEX i_mail_item_dumpster_date ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, date);
CREATE INDEX i_mail_item_dumpster_mod_metadata ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, mod_metadata);
CREATE INDEX i_mail_item_dumpster_locator ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, locator);
CREATE INDEX i_mail_item_dumpster_uuid ON ${DATABASE_NAME}.mail_item_dumpster (mailbox_id, uuid);

CREATE TABLE ${DATABASE_NAME}.revision (
   mailbox_id    INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   item_id       INTEGER NOT NULL,
   version       INTEGER NOT NULL,
   date          BIGINT NOT NULL,
   size          BIGINT NOT NULL,
   locator       VARCHAR(1024),
   blob_digest   VARCHAR(44) COLLATE "C",
   name          VARCHAR(255),
   metadata      TEXT,
   mod_metadata  INTEGER NOT NULL,
   change_date   BIGINT,
   mod_content   INTEGER NOT NULL,

   PRIMARY KEY (mailbox_id, item_id, version),
   CONSTRAINT fk_revision_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
);

CREATE TABLE ${DATABASE_NAME}.revision_dumpster (
   mailbox_id    INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   item_id       INTEGER NOT NULL,
   version       INTEGER NOT NULL,
   date          BIGINT NOT NULL,
   size          BIGINT NOT NULL,
   locator       VARCHAR(1024),
   blob_digest   VARCHAR(44) COLLATE "C",
   name          VARCHAR(255),
   metadata      TEXT,
   mod_metadata  INTEGER NOT NULL,
   change_date   BIGINT,
   mod_content   INTEGER NOT NULL,

   PRIMARY KEY (mailbox_id, item_id, version),
   CONSTRAINT fk_revision_dumpster_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item_dumpster(mailbox_id, id) ON DELETE CASCADE
);

CREATE TABLE ${DATABASE_NAME}.tag (
   mailbox_id    INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   id            INTEGER NOT NULL,
   name          VARCHAR(128) NOT NULL,
   color         BIGINT,
   item_count    INTEGER NOT NULL DEFAULT 0,
   unread        INTEGER NOT NULL DEFAULT 0,
   listed        BOOLEAN NOT NULL DEFAULT FALSE,
   sequence      INTEGER NOT NULL,
   policy        VARCHAR(1024),

   PRIMARY KEY (mailbox_id, id)
);
CREATE UNIQUE INDEX i_tag_name ON ${DATABASE_NAME}.tag (mailbox_id, upper(name));

CREATE TABLE ${DATABASE_NAME}.tagged_item (
   mailbox_id    INTEGER NOT NULL,
   tag_id        INTEGER NOT NULL,
   item_id       INTEGER NOT NULL,

   CONSTRAINT i_tagged_item_unique UNIQUE (mailbox_id, tag_id, item_id),
   CONSTRAINT fk_tagged_item_tag FOREIGN KEY (mailbox_id, tag_id) REFERENCES ${DATABASE_NAME}.tag(mailbox_id, id) ON DELETE CASCADE,
   CONSTRAINT fk_tagged_item_item FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
);

CREATE TABLE ${DATABASE_NAME}.open_conversation (
   mailbox_id  INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   hash        CHAR(28) COLLATE "C" NOT NULL,
   conv_id     INTEGER NOT NULL,

   PRIMARY KEY (mailbox_id, hash),
   CONSTRAINT fk_open_conversation_conv_id FOREIGN KEY (mailbox_id, conv_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
);
CREATE INDEX i_open_conversation_conv_id ON ${DATABASE_NAME}.open_conversation (mailbox_id, conv_id);

CREATE TABLE ${DATABASE_NAME}.appointment (
   mailbox_id  INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   uid         VARCHAR(255) NOT NULL,
   item_id     INTEGER NOT NULL,
   start_time  TIMESTAMP NOT NULL,
   end_time    TIMESTAMP,

   PRIMARY KEY (mailbox_id, uid),
   CONSTRAINT fk_appointment_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item(mailbox_id, id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX i_appointment_item_id ON ${DATABASE_NAME}.appointment (mailbox_id, item_id);

CREATE TABLE ${DATABASE_NAME}.appointment_dumpster (
   mailbox_id  INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   uid         VARCHAR(255) NOT NULL,
   item_id     INTEGER NOT NULL,
   start_time  TIMESTAMP NOT NULL,
   end_time    TIMESTAMP,

   PRIMARY KEY (mailbox_id, uid),
   CONSTRAINT fk_appointment_dumpster_item_id FOREIGN KEY (mailbox_id, item_id) REFERENCES ${DATABASE_NAME}.mail_item_dumpster(mailbox_id, id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX i_appointment_dumpster_item_id ON ${DATABASE_NAME}.appointment_dumpster (mailbox_id, item_id);

CREATE TABLE ${DATABASE_NAME}.tombstone (
   mailbox_id  INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   sequence    INTEGER NOT NULL,
   date        BIGINT NOT NULL,
   type        SMALLINT,
   ids         TEXT
);
CREATE INDEX i_tombstone_sequence ON ${DATABASE_NAME}.tombstone (mailbox_id, sequence);

CREATE TABLE ${DATABASE_NAME}.pop3_message (
   mailbox_id     INTEGER NOT NULL REFERENCES zimbra.mailbox(id),
   data_source_id CHAR(36) NOT NULL,
   uid            VARCHAR(255) COLLATE "C" NOT NULL,
   item_id        INTEGER NOT NULL,

   PRIMARY KEY (mailbox_id, item_id)
);
CREATE UNIQUE INDEX i_uid_pop3_id ON ${DATABASE_NAME}.pop3_message (uid, data_source_id);

CREATE TABLE ${DATABASE_NAME}.imap_folder (
   mailbox_id         INTEGER NOT NULL REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
   item_id            INTEGER NOT NULL,
   data_source_id     CHAR(36) NOT NULL,
   local_path         VARCHAR(1000) NOT NULL,
   remote_path        VARCHAR(1000) NOT NULL,
   uid_validity       BIGINT,

   PRIMARY KEY (mailbox_id, item_id)
);
CREATE UNIQUE INDEX i_local_path ON ${DATABASE_NAME}.imap_folder (local_path, data_source_id, mailbox_id);
CREATE UNIQUE INDEX i_remote_path ON ${DATABASE_NAME}.imap_folder (remote_path, data_source_id, mailbox_id);

CREATE TABLE ${DATABASE_NAME}.imap_message (
   mailbox_id     INTEGER NOT NULL REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
   imap_folder_id INTEGER NOT NULL,
   uid            BIGINT NOT NULL,
   item_id        INTEGER NOT NULL,
   flags          INTEGER NOT NULL DEFAULT 0,

   PRIMARY KEY (mailbox_id, item_id),
   CONSTRAINT fk_imap_message_imap_folder_id FOREIGN KEY (mailbox_id, imap_folder_id)
      REFERENCES ${DATABASE_NAME}.imap_folder(mailbox_id, item_id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX i_uid_imap_id ON ${DATABASE_NAME}.imap_message (mailbox_id, imap_folder_id, uid);

CREATE TABLE ${DATABASE_NAME}.data_source_item (
   mailbox_id     INTEGER NOT NULL REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
   data_source_id CHAR(36) NOT NULL,
   item_id        INTEGER NOT NULL,
   folder_id      INTEGER NOT NULL DEFAULT 0,
   remote_id      VARCHAR(255) COLLATE "C" NOT NULL,
   metadata       TEXT,

   PRIMARY KEY (mailbox_id, item_id),
   CONSTRAINT i_remote_id UNIQUE (mailbox_id, data_source_id, remote_id)
);

CREATE TABLE ${DATABASE_NAME}.purged_conversations (
   mailbox_id     INTEGER NOT NULL REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
   data_source_id CHAR(36) NOT NULL,
   item_id        INTEGER NOT NULL,
   hash           CHAR(28) COLLATE "C" NOT NULL,

   PRIMARY KEY (mailbox_id, data_source_id, hash)
);

CREATE TABLE ${DATABASE_NAME}.purged_messages (
   mailbox_id       INTEGER NOT NULL REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
   data_source_id   CHAR(36) NOT NULL,
   item_id          INTEGER NOT NULL,
   parent_id        INTEGER,
   remote_id        VARCHAR(255) COLLATE "C" NOT NULL,
   remote_folder_id VARCHAR(255) COLLATE "C" NOT NULL,
   purge_date       BIGINT,

   PRIMARY KEY (mailbox_id, data_source_id, item_id)
);

CREATE TABLE ${DATABASE_NAME}.event (
   mailbox_id    INTEGER NOT NULL REFERENCES zimbra.mailbox(id) ON DELETE CASCADE,
   account_id    VARCHAR(36) NOT NULL,
   item_id       INTEGER NOT NULL,
   folder_id     INTEGER NOT NULL,
   op            SMALLINT NOT NULL,
   ts            INTEGER NOT NULL,
   version       INTEGER,
   user_agent    VARCHAR(128),
   arg           VARCHAR(10240)
);
