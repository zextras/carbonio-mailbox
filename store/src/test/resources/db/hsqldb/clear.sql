-- SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
-- SPDX-FileCopyrightText: 2022 Synacor, Inc.
--
-- SPDX-License-Identifier: GPL-2.0-only

DELETE FROM *{DATABASE_NAME}.tagged_item;
DELETE FROM *{DATABASE_NAME}.tag;
DELETE FROM *{DATABASE_NAME}.mail_item;
DELETE FROM *{DATABASE_NAME}.mail_item_dumpster;
DELETE FROM *{DATABASE_NAME}.revision;
DELETE FROM *{DATABASE_NAME}.revision_dumpster;
DELETE FROM *{DATABASE_NAME}.open_conversation;
DELETE FROM *{DATABASE_NAME}.appointment;
DELETE FROM *{DATABASE_NAME}.appointment_dumpster;
DELETE FROM *{DATABASE_NAME}.tombstone;
DELETE FROM *{DATABASE_NAME}.pop3_message;
DELETE FROM *{DATABASE_NAME}.imap_folder;
DELETE FROM *{DATABASE_NAME}.imap_message;
DELETE FROM *{DATABASE_NAME}.data_source_item;

DELETE FROM ZIMBRA.mailbox;
DELETE FROM ZIMBRA.current_volumes;
DELETE FROM ZIMBRA.deleted_account;
DELETE FROM ZIMBRA.mailbox_metadata;
DELETE FROM ZIMBRA.out_of_office;
DELETE FROM ZIMBRA.config;
DELETE FROM ZIMBRA.table_maintenance;
DELETE FROM ZIMBRA.service_status;
DELETE FROM ZIMBRA.scheduled_task;
DELETE FROM ZIMBRA.mobile_devices;
DELETE FROM ZIMBRA.pending_acl_push;

INSERT INTO current_volumes (message_volume_id, index_volume_id, next_mailbox_id) VALUES (1, 2, 1);
