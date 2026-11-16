// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.Namespace;
import org.dom4j.QName;

public final class BackupConstants {
    public static final String NAMESPACE_STR = AdminConstants.NAMESPACE_STR;
    public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);

    public static final String E_BACKUP_REQUEST = "BackupRequest";
    public static final String E_BACKUP_RESPONSE = "BackupResponse";
    public static final String E_BACKUP_QUERY_REQUEST = "BackupQueryRequest";
    public static final String E_BACKUP_QUERY_RESPONSE = "BackupQueryResponse";
    public static final String E_BACKUP_ACCOUNT_QUERY_REQUEST = "BackupAccountQueryRequest";
    public static final String E_BACKUP_ACCOUNT_QUERY_RESPONSE = "BackupAccountQueryResponse";
    public static final String E_RESTORE_REQUEST = "RestoreRequest";
    public static final String E_RESTORE_RESPONSE = "RestoreResponse";
    public static final String E_ROLLOVER_REDOLOG_REQUEST = "RolloverRedoLogRequest";
    public static final String E_ROLLOVER_REDOLOG_RESPONSE = "RolloverRedoLogResponse";
    public static final String E_PURGE_MOVED_MAILBOX_REQUEST = "PurgeMovedMailboxRequest";
    public static final String E_PURGE_MOVED_MAILBOX_RESPONSE = "PurgeMovedMailboxResponse";
    public static final String E_SCHEDULE_BACKUPS_REQUEST = "ScheduleBackupsRequest";
    public static final String E_SCHEDULE_BACKUPS_RESPONSE = "ScheduleBackupsResponse";
    public static final String E_MOVE_MAILBOX_REQUEST = "MoveMailboxRequest";
    public static final String E_MOVE_MAILBOX_RESPONSE = "MoveMailboxResponse";
    public static final String E_GET_MAILBOX_VERSION_REQUEST = "GetMailboxVersionRequest";
    public static final String E_GET_MAILBOX_VERSION_RESPONSE = "GetMailboxVersionResponse";
    public static final String E_GET_MAILBOX_VOLUMES_REQUEST = "GetMailboxVolumesRequest";
    public static final String E_GET_MAILBOX_VOLUMES_RESPONSE = "GetMailboxVolumesResponse";
    public static final String E_UNLOAD_MAILBOX_REQUEST = "UnloadMailboxRequest";
    public static final String E_UNLOAD_MAILBOX_RESPONSE = "UnloadMailboxResponse";
    public static final String E_RELOAD_ACCOUNT_REQUEST = "ReloadAccountRequest";
    public static final String E_RELOAD_ACCOUNT_RESPONSE = "ReloadAccountResponse";
    public static final String E_REGISTER_MAILBOX_MOVE_OUT_REQUEST = "RegisterMailboxMoveOutRequest";
    public static final String E_REGISTER_MAILBOX_MOVE_OUT_RESPONSE = "RegisterMailboxMoveOutResponse";
    public static final String E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST = "UnregisterMailboxMoveOutRequest";
    public static final String E_UNREGISTER_MAILBOX_MOVE_OUT_RESPONSE = "UnregisterMailboxMoveOutResponse";
    public static final String E_QUERY_MAILBOX_MOVE_REQUEST = "QueryMailboxMoveRequest";
    public static final String E_QUERY_MAILBOX_MOVE_RESPONSE = "QueryMailboxMoveResponse";

    public static final QName BACKUP_REQUEST = QName.get(E_BACKUP_REQUEST, NAMESPACE);
    public static final QName BACKUP_RESPONSE = QName.get(E_BACKUP_RESPONSE, NAMESPACE);
    public static final QName BACKUP_QUERY_REQUEST = QName.get(E_BACKUP_QUERY_REQUEST, NAMESPACE);
    public static final QName BACKUP_QUERY_RESPONSE = QName.get(E_BACKUP_QUERY_RESPONSE, NAMESPACE);
    public static final QName BACKUP_ACCOUNT_QUERY_REQUEST = QName.get(E_BACKUP_ACCOUNT_QUERY_REQUEST, NAMESPACE);
    public static final QName BACKUP_ACCOUNT_QUERY_RESPONSE = QName.get(E_BACKUP_ACCOUNT_QUERY_RESPONSE, NAMESPACE);
    public static final QName RESTORE_REQUEST = QName.get(E_RESTORE_REQUEST, NAMESPACE);
    public static final QName RESTORE_RESPONSE = QName.get(E_RESTORE_RESPONSE, NAMESPACE);
    public static final QName ROLLOVER_REDOLOG_REQUEST = QName.get(E_ROLLOVER_REDOLOG_REQUEST, NAMESPACE);
    public static final QName ROLLOVER_REDOLOG_RESPONSE = QName.get(E_ROLLOVER_REDOLOG_RESPONSE, NAMESPACE);

    public static final QName PURGE_MOVED_MAILBOX_REQUEST = QName.get(E_PURGE_MOVED_MAILBOX_REQUEST, NAMESPACE);
    public static final QName PURGE_MOVED_MAILBOX_RESPONSE = QName.get(E_PURGE_MOVED_MAILBOX_RESPONSE, NAMESPACE);

    public static final QName SCHEDULE_BACKUPS_REQUEST = QName.get(E_SCHEDULE_BACKUPS_REQUEST, NAMESPACE);
    public static final QName SCHEDULE_BACKUPS_RESPONSE = QName.get(E_SCHEDULE_BACKUPS_RESPONSE, NAMESPACE);

    public static final QName MOVE_MAILBOX_REQUEST = QName.get(E_MOVE_MAILBOX_REQUEST, NAMESPACE);
    public static final QName MOVE_MAILBOX_RESPONSE = QName.get(E_MOVE_MAILBOX_RESPONSE, NAMESPACE);
    public static final QName GET_MAILBOX_VERSION_REQUEST = QName.get(E_GET_MAILBOX_VERSION_REQUEST, NAMESPACE);
    public static final QName GET_MAILBOX_VERSION_RESPONSE = QName.get(E_GET_MAILBOX_VERSION_RESPONSE, NAMESPACE);
    public static final QName GET_MAILBOX_VOLUMES_REQUEST = QName.get(E_GET_MAILBOX_VOLUMES_REQUEST, NAMESPACE);
    public static final QName GET_MAILBOX_VOLUMES_RESPONSE = QName.get(E_GET_MAILBOX_VOLUMES_RESPONSE, NAMESPACE);
    public static final QName UNLOAD_MAILBOX_REQUEST = QName.get(E_UNLOAD_MAILBOX_REQUEST, NAMESPACE);
    public static final QName UNLOAD_MAILBOX_RESPONSE = QName.get(E_UNLOAD_MAILBOX_RESPONSE, NAMESPACE);
    public static final QName RELOAD_ACCOUNT_REQUEST = QName.get(E_RELOAD_ACCOUNT_REQUEST, NAMESPACE);
    public static final QName RELOAD_ACCOUNT_RESPONSE = QName.get(E_RELOAD_ACCOUNT_RESPONSE, NAMESPACE);
    public static final QName REGISTER_MAILBOX_MOVE_OUT_REQUEST = QName.get(E_REGISTER_MAILBOX_MOVE_OUT_REQUEST, NAMESPACE);
    public static final QName REGISTER_MAILBOX_MOVE_OUT_RESPONSE = QName.get(E_REGISTER_MAILBOX_MOVE_OUT_RESPONSE, NAMESPACE);
    public static final QName UNREGISTER_MAILBOX_MOVE_OUT_REQUEST = QName.get(E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST, NAMESPACE);
    public static final QName UNREGISTER_MAILBOX_MOVE_OUT_RESPONSE = QName.get(E_UNREGISTER_MAILBOX_MOVE_OUT_RESPONSE, NAMESPACE);
    public static final QName QUERY_MAILBOX_MOVE_REQUEST = QName.get(E_QUERY_MAILBOX_MOVE_REQUEST, NAMESPACE);
    public static final QName QUERY_MAILBOX_MOVE_RESPONSE = QName.get(E_QUERY_MAILBOX_MOVE_RESPONSE, NAMESPACE);

    public static final String E_ACCOUNT = "account";

    public static final String A_NAME = "name";

    // mailbox move
    public static final String A_TARGET = "dest";

    // FileCopier options

    // include/exclude full backup components
}
