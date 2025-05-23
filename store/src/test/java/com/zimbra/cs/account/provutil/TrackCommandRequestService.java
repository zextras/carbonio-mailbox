// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.provutil;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;
import io.vavr.control.Try;
import org.dom4j.QName;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class TrackCommandRequestService implements DocumentService {

  static final List<QName> handlerNames = Arrays.asList(AdminConstants.PING_REQUEST
          , AdminConstants.CHECK_HEALTH_REQUEST
          , AdminConstants.GET_ALL_LOCALES_REQUEST
          , AdminConstants.AUTH_REQUEST
          , AdminConstants.CREATE_ACCOUNT_REQUEST
          , AdminConstants.CREATE_GAL_SYNC_ACCOUNT_REQUEST
          , AdminConstants.ADD_GAL_SYNC_DATASOURCE_REQUEST
          , AdminConstants.DELEGATE_AUTH_REQUEST
          , AdminConstants.DELETE_GAL_SYNC_ACCOUNT_REQUEST
          , AdminConstants.GET_ACCOUNT_REQUEST
          , AdminConstants.GET_ACCOUNT_INFO_REQUEST
          , AdminConstants.GET_ALL_ACCOUNTS_REQUEST
          , AdminConstants.GET_ALL_ADMIN_ACCOUNTS_REQUEST
          , AdminConstants.MODIFY_ACCOUNT_REQUEST
          , AdminConstants.DELETE_ACCOUNT_REQUEST
          , AdminConstants.SET_PASSWORD_REQUEST
          , AdminConstants.CHECK_PASSWORD_STRENGTH_REQUEST
          , AdminConstants.ADD_ACCOUNT_ALIAS_REQUEST
          , AdminConstants.REMOVE_ACCOUNT_ALIAS_REQUEST
          , AdminConstants.SEARCH_ACCOUNTS_REQUEST
          , AdminConstants.RENAME_ACCOUNT_REQUEST
          , AdminConstants.CHANGE_PRIMARY_EMAIL_REQUEST
          , AdminConstants.RESET_ACCOUNT_PASSWORD_REQUEST
          , AdminConstants.SEARCH_DIRECTORY_REQUEST
          , AdminConstants.GET_ACCOUNT_MEMBERSHIP_REQUEST
          , AdminConstants.ISSUE_CERT_REQUEST
          , AdminConstants.CREATE_DOMAIN_REQUEST
          , AdminConstants.GET_DOMAIN_REQUEST
          , AdminConstants.GET_DOMAIN_INFO_REQUEST
          , AdminConstants.GET_ALL_DOMAINS_REQUEST
          , AdminConstants.MODIFY_DOMAIN_REQUEST
          , AdminConstants.DELETE_DOMAIN_REQUEST
          , AdminConstants.CREATE_COS_REQUEST
          , AdminConstants.COPY_COS_REQUEST
          , AdminConstants.GET_COS_REQUEST
          , AdminConstants.GET_ALL_COS_REQUEST
          , AdminConstants.MODIFY_COS_REQUEST
          , AdminConstants.DELETE_COS_REQUEST
          , AdminConstants.RENAME_COS_REQUEST
          , AdminConstants.CREATE_SERVER_REQUEST
          , AdminConstants.GET_SERVER_REQUEST
          , AdminConstants.GET_ALL_SERVERS_REQUEST
          , AdminConstants.MODIFY_SERVER_REQUEST
          , AdminConstants.DELETE_SERVER_REQUEST
          , AdminConstants.GET_CONFIG_REQUEST
          , AdminConstants.GET_ALL_CONFIG_REQUEST
          , AdminConstants.MODIFY_CONFIG_REQUEST
          , AdminConstants.GET_SERVICE_STATUS_REQUEST
          , AdminConstants.PURGE_MESSAGES_REQUEST
          , AdminConstants.DELETE_MAILBOX_REQUEST
          , AdminConstants.GET_MAILBOX_REQUEST
          , AdminConstants.RUN_UNIT_TESTS_REQUEST
          , AdminConstants.CHECK_AUTH_CONFIG_REQUEST
          , AdminConstants.CHECK_GAL_CONFIG_REQUEST
          , AdminConstants.CHECK_HOSTNAME_RESOLVE_REQUEST
          , AdminConstants.CHECK_EXCHANGE_AUTH_REQUEST
          , AdminConstants.CREATE_VOLUME_REQUEST
          , AdminConstants.GET_VOLUME_REQUEST
          , AdminConstants.GET_ALL_VOLUMES_REQUEST
          , AdminConstants.MODIFY_VOLUME_REQUEST
          , AdminConstants.DELETE_VOLUME_REQUEST
          , AdminConstants.GET_CURRENT_VOLUMES_REQUEST
          , AdminConstants.SET_CURRENT_VOLUME_REQUEST
          , AdminConstants.CHECK_BLOB_CONSISTENCY_REQUEST
          , AdminConstants.EXPORT_AND_DELETE_ITEMS_REQUEST
          , AdminConstants.DEDUPE_BLOBS_REQUEST
          , AdminConstants.CREATE_DISTRIBUTION_LIST_REQUEST
          , AdminConstants.GET_DISTRIBUTION_LIST_REQUEST
          , AdminConstants.GET_ALL_DISTRIBUTION_LISTS_REQUEST
          , AdminConstants.ADD_DISTRIBUTION_LIST_MEMBER_REQUEST
          , AdminConstants.REMOVE_DISTRIBUTION_LIST_MEMBER_REQUEST
          , AdminConstants.MODIFY_DISTRIBUTION_LIST_REQUEST
          , AdminConstants.DELETE_DISTRIBUTION_LIST_REQUEST
          , AdminConstants.ADD_DISTRIBUTION_LIST_ALIAS_REQUEST
          , AdminConstants.REMOVE_DISTRIBUTION_LIST_ALIAS_REQUEST
          , AdminConstants.RENAME_DISTRIBUTION_LIST_REQUEST
          , AdminConstants.GET_DISTRIBUTION_LIST_MEMBERSHIP_REQUEST
          , AdminConstants.AUTO_PROV_ACCOUNT_REQUEST
          , AdminConstants.AUTO_PROV_TASK_CONTROL_REQUEST
          , AdminConstants.SEARCH_AUTO_PROV_DIRECTORY_REQUEST
          , AdminConstants.GET_VERSION_INFO_REQUEST
          , AdminConstants.GET_ATTRIBUTE_INFO_REQUEST
          , AdminConstants.REINDEX_REQUEST
          , AdminConstants.COMPACT_INDEX_REQUEST
          , AdminConstants.GET_INDEX_STATS_REQUEST
          , AdminConstants.VERIFY_INDEX_REQUEST
          , AdminConstants.RECALCULATE_MAILBOX_COUNTS_REQUEST
          , AdminConstants.GET_ZIMLET_REQUEST
          , AdminConstants.CREATE_ZIMLET_REQUEST
          , AdminConstants.DELETE_ZIMLET_REQUEST
          , AdminConstants.GET_ADMIN_EXTENSION_ZIMLETS_REQUEST
          , AdminConstants.GET_ZIMLET_STATUS_REQUEST
          , AdminConstants.GET_ALL_ZIMLETS_REQUEST
          , AdminConstants.DEPLOY_ZIMLET_REQUEST
          , AdminConstants.UNDEPLOY_ZIMLET_REQUEST
          , AdminConstants.CONFIGURE_ZIMLET_REQUEST
          , AdminConstants.MODIFY_ZIMLET_REQUEST
          , AdminConstants.DUMP_SESSIONS_REQUEST
          , AdminConstants.GET_SESSIONS_REQUEST
          , AdminConstants.CREATE_CALENDAR_RESOURCE_REQUEST
          , AdminConstants.DELETE_CALENDAR_RESOURCE_REQUEST
          , AdminConstants.MODIFY_CALENDAR_RESOURCE_REQUEST
          , AdminConstants.RENAME_CALENDAR_RESOURCE_REQUEST
          , AdminConstants.GET_CALENDAR_RESOURCE_REQUEST
          , AdminConstants.GET_ALL_CALENDAR_RESOURCES_REQUEST
          , AdminConstants.SEARCH_CALENDAR_RESOURCES_REQUEST
          , AdminConstants.GET_QUOTA_USAGE_REQUEST
          , AdminConstants.COMPUTE_AGGR_QUOTA_USAGE_REQUEST
          , AdminConstants.GET_AGGR_QUOTA_USAGE_ON_SERVER_REQUEST
          , AdminConstants.GET_ALL_MAILBOXES_REQUEST
          , AdminConstants.GET_MAILBOX_STATS_REQUEST
          , AdminConstants.GET_MAIL_QUEUE_INFO_REQUEST
          , AdminConstants.GET_MAIL_QUEUE_REQUEST
          , AdminConstants.MAIL_QUEUE_ACTION_REQUEST
          , AdminConstants.MAIL_QUEUE_FLUSH_REQUEST
          , AdminConstants.AUTO_COMPLETE_GAL_REQUEST
          , AdminConstants.SEARCH_GAL_REQUEST
          , AdminConstants.GET_DATA_SOURCES_REQUEST
          , AdminConstants.CREATE_DATA_SOURCE_REQUEST
          , AdminConstants.MODIFY_DATA_SOURCE_REQUEST
          , AdminConstants.DELETE_DATA_SOURCE_REQUEST
          , AdminConstants.FIX_CALENDAR_TZ_REQUEST
          , AdminConstants.FIX_CALENDAR_END_TIME_REQUEST
          , AdminConstants.FIX_CALENDAR_PRIORITY_REQUEST
          , AdminConstants.GET_ADMIN_SAVED_SEARCHES_REQUEST
          , AdminConstants.MODIFY_ADMIN_SAVED_SEARCHES_REQUEST
          , AdminConstants.ADD_ACCOUNT_LOGGER_REQUEST
          , AdminConstants.REMOVE_ACCOUNT_LOGGER_REQUEST
          , AdminConstants.GET_ACCOUNT_LOGGERS_REQUEST
          , AdminConstants.GET_ALL_ACCOUNT_LOGGERS_REQUEST
          , AdminConstants.RESET_ALL_LOGGERS_REQUEST
          , AdminConstants.CHECK_DIRECTORY_REQUEST
          , AdminConstants.FLUSH_CACHE_REQUEST
          , AdminConstants.COUNT_ACCOUNT_REQUEST
          , AdminConstants.GET_SHARE_INFO_REQUEST
          , AdminConstants.GET_SERVER_NIFS_REQUEST
          , AdminConstants.GET_ALL_FREE_BUSY_PROVIDERS_REQUEST
          , AdminConstants.GET_FREE_BUSY_QUEUE_INFO_REQUEST
          , AdminConstants.PUSH_FREE_BUSY_REQUEST
          , AdminConstants.PURGE_FREE_BUSY_QUEUE_REQUEST
          , AdminConstants.PURGE_ACCOUNT_CALENDAR_CACHE_REQUEST
          , AdminConstants.GET_DELEGATED_ADMIN_CONSTRAINTS_REQUEST
          , AdminConstants.GET_RIGHTS_DOC_REQUEST
          , AdminConstants.GET_RIGHT_REQUEST
          , AdminConstants.GET_ADMIN_CONSOLE_UI_COMP_REQUEST
          , AdminConstants.GET_ALL_EFFECTIVE_RIGHTS_REQUEST
          , AdminConstants.GET_ALL_RIGHTS_REQUEST
          , AdminConstants.GET_EFFECTIVE_RIGHTS_REQUEST
          , AdminConstants.GET_CREATE_OBJECT_ATTRS_REQUEST
          , AdminConstants.GET_GRANTS_REQUEST
          , AdminConstants.CHECK_RIGHT_REQUEST
          , AdminConstants.GRANT_RIGHT_REQUEST
          , AdminConstants.MODIFY_DELEGATED_ADMIN_CONSTRAINTS_REQUEST
          , AdminConstants.REVOKE_RIGHT_REQUEST
          , AdminConstants.ADMIN_CREATE_WAIT_SET_REQUEST
          , AdminConstants.ADMIN_WAIT_SET_REQUEST
          , AdminConstants.ADMIN_DESTROY_WAIT_SET_REQUEST
          , AdminConstants.QUERY_WAIT_SET_REQUEST
          , AdminConstants.GET_SERVER_STATS_REQUEST
          , AdminConstants.GET_LOGGER_STATS_REQUEST
          , AdminConstants.SYNC_GAL_ACCOUNT_REQUEST
          , AdminConstants.RELOAD_MEMCACHED_CLIENT_CONFIG_REQUEST
          , AdminConstants.GET_MEMCACHED_CLIENT_CONFIG_REQUEST
          , AdminConstants.RELOAD_LOCAL_CONFIG_REQUEST
          , AdminConstants.NO_OP_REQUEST
          , AdminConstants.CLEAR_COOKIE_REQUEST
          , AdminConstants.LOCKOUT_MAILBOX_REQUEST
          , AdminConstants.REFRESH_REGISTERED_AUTHTOKENS_REQUEST
          , AdminConstants.GET_SYSTEM_RETENTION_POLICY_REQUEST
          , AdminConstants.CREATE_SYSTEM_RETENTION_POLICY_REQUEST
          , AdminConstants.MODIFY_SYSTEM_RETENTION_POLICY_REQUEST
          , AdminConstants.DELETE_SYSTEM_RETENTION_POLICY_REQUEST
          , AdminConstants.VERIFY_STORE_MANAGER_REQUEST
          , AdminConstants.GET_FILTER_RULES_REQUEST
          , AdminConstants.MODIFY_FILTER_RULES_REQUEST
          , AdminConstants.GET_OUTGOING_FILTER_RULES_REQUEST
          , AdminConstants.MODIFY_OUTGOING_FILTER_RULES_REQUEST
          , AdminConstants.CONTACT_BACKUP_REQUEST
          , AccountConstants.AUTH_REQUEST
          , AccountConstants.CHANGE_PASSWORD_REQUEST
          , AccountConstants.END_SESSION_REQUEST
          , AccountConstants.CLIENT_INFO_REQUEST
          , AccountConstants.GET_PREFS_REQUEST
          , AccountConstants.MODIFY_PREFS_REQUEST
          , AccountConstants.GET_INFO_REQUEST
          , AccountConstants.GET_ACCOUNT_INFO_REQUEST
          , AccountConstants.AUTO_COMPLETE_GAL_REQUEST
          , AccountConstants.SEARCH_CALENDAR_RESOURCES_REQUEST
          , AccountConstants.SEARCH_GAL_REQUEST
          , AccountConstants.SYNC_GAL_REQUEST
          , AccountConstants.MODIFY_PROPERTIES_REQUEST
          , AccountConstants.MODIFY_ZIMLET_PREFS_REQUEST
          , AccountConstants.GET_ALL_LOCALES_REQUEST
          , AccountConstants.GET_AVAILABLE_LOCALES_REQUEST
          , AccountConstants.GET_AVAILABLE_CSV_FORMATS_REQUEST
          , AccountConstants.CREATE_IDENTITY_REQUEST
          , AccountConstants.MODIFY_IDENTITY_REQUEST
          , AccountConstants.DELETE_IDENTITY_REQUEST
          , AccountConstants.GET_IDENTITIES_REQUEST
          , AccountConstants.CREATE_SIGNATURE_REQUEST
          , AccountConstants.MODIFY_SIGNATURE_REQUEST
          , AccountConstants.DELETE_SIGNATURE_REQUEST
          , AccountConstants.GET_SIGNATURES_REQUEST
          , AccountConstants.GET_SHARE_INFO_REQUEST
          , AccountConstants.GET_WHITE_BLACK_LIST_REQUEST
          , AccountConstants.MODIFY_WHITE_BLACK_LIST_REQUEST
          , AccountConstants.CREATE_DISTRIBUTION_LIST_REQUEST
          , AccountConstants.DISTRIBUTION_LIST_ACTION_REQUEST
          , AccountConstants.GET_ACCOUNT_DISTRIBUTION_LISTS_REQUEST
          , AccountConstants.GET_DISTRIBUTION_LIST_REQUEST
          , AccountConstants.GET_DISTRIBUTION_LIST_MEMBERS_REQUEST
          , AccountConstants.CHECK_RIGHTS_REQUEST
          , AccountConstants.DISCOVER_RIGHTS_REQUEST
          , AccountConstants.GET_RIGHTS_REQUEST
          , AccountConstants.GRANT_RIGHTS_REQUEST
          , AccountConstants.REVOKE_RIGHTS_REQUEST
          , AccountConstants.GET_VERSION_INFO_REQUEST
          , AccountConstants.RESET_PASSWORD_REQUEST
          , AccountConstants.SEARCH_USERS_BY_FEATURE_REQUEST
          , MailConstants.NO_OP_REQUEST
          , MailConstants.GENERATE_UUID_REQUEST
          , MailConstants.BROWSE_REQUEST
          , MailConstants.SEARCH_REQUEST
          , MailConstants.SEARCH_CONV_REQUEST
          , MailConstants.EMPTY_DUMPSTER_REQUEST
          , MailConstants.GET_ITEM_REQUEST
          , MailConstants.ITEM_ACTION_REQUEST
          , MailConstants.GET_METADATA_REQUEST
          , MailConstants.SET_METADATA_REQUEST
          , MailConstants.GET_MAILBOX_METADATA_REQUEST
          , MailConstants.SET_MAILBOX_METADATA_REQUEST
          , MailConstants.MODIFY_MAILBOX_METADATA_REQUEST
          , MailConstants.GET_CONV_REQUEST
          , MailConstants.CONV_ACTION_REQUEST
          , MailConstants.GET_MSG_REQUEST
          , MailConstants.GET_MSG_METADATA_REQUEST
          , MailConstants.MSG_ACTION_REQUEST
          , MailConstants.SEND_MSG_REQUEST
          , MailConstants.SEND_SECURE_MSG_REQUEST
          , MailConstants.SEND_REPORT_REQUEST
          , MailConstants.SEND_SHARE_NOTIFICATION_REQUEST
          , MailConstants.BOUNCE_MSG_REQUEST
          , MailConstants.ADD_MSG_REQUEST
          , MailConstants.SAVE_DRAFT_REQUEST
          , MailConstants.REMOVE_ATTACHMENTS_REQUEST
          , MailConstants.GET_FOLDER_REQUEST
          , MailConstants.CREATE_FOLDER_REQUEST
          , MailConstants.FOLDER_ACTION_REQUEST
          , MailConstants.GET_TAG_REQUEST
          , MailConstants.CREATE_TAG_REQUEST
          , MailConstants.TAG_ACTION_REQUEST
          , MailConstants.GET_SEARCH_FOLDER_REQUEST
          , MailConstants.CREATE_SEARCH_FOLDER_REQUEST
          , MailConstants.MODIFY_SEARCH_FOLDER_REQUEST
          , MailConstants.CREATE_MOUNTPOINT_REQUEST
          , MailConstants.ENABLE_SHARED_REMINDER_REQUEST
          , MailConstants.GET_CONTACTS_REQUEST
          , MailConstants.CREATE_CONTACT_REQUEST
          , MailConstants.MODIFY_CONTACT_REQUEST
          , MailConstants.CONTACT_ACTION_REQUEST
          , MailConstants.EXPORT_CONTACTS_REQUEST
          , MailConstants.IMPORT_CONTACTS_REQUEST
          , MailConstants.SYNC_REQUEST
          , MailConstants.GET_FILTER_RULES_REQUEST
          , MailConstants.MODIFY_FILTER_RULES_REQUEST
          , MailConstants.APPLY_FILTER_RULES_REQUEST
          , MailConstants.GET_OUTGOING_FILTER_RULES_REQUEST
          , MailConstants.MODIFY_OUTGOING_FILTER_RULES_REQUEST
          , MailConstants.APPLY_OUTGOING_FILTER_RULES_REQUEST
          , MailConstants.GET_APPT_SUMMARIES_REQUEST
          , MailConstants.GET_APPOINTMENT_REQUEST
          , MailConstants.SET_APPOINTMENT_REQUEST
          , MailConstants.CREATE_APPOINTMENT_REQUEST
          , MailConstants.CREATE_APPOINTMENT_EXCEPTION_REQUEST
          , MailConstants.MODIFY_APPOINTMENT_REQUEST
          , MailConstants.CANCEL_APPOINTMENT_REQUEST
          , MailConstants.FORWARD_APPOINTMENT_REQUEST
          , MailConstants.FORWARD_APPOINTMENT_INVITE_REQUEST
          , MailConstants.ADD_APPOINTMENT_INVITE_REQUEST
          , MailConstants.COUNTER_APPOINTMENT_REQUEST
          , MailConstants.DECLINE_COUNTER_APPOINTMENT_REQUEST
          , MailConstants.IMPORT_APPOINTMENTS_REQUEST
          , MailConstants.GET_CALITEM_SUMMARIES_REQUEST
          , MailConstants.SEND_INVITE_REPLY_REQUEST
          , MailConstants.ICAL_REPLY_REQUEST
          , MailConstants.GET_FREE_BUSY_REQUEST
          , MailConstants.GET_WORKING_HOURS_REQUEST
          , MailConstants.GET_ICAL_REQUEST
          , MailConstants.ANNOUNCE_ORGANIZER_CHANGE_REQUEST
          , MailConstants.DISMISS_CALITEM_ALARM_REQUEST
          , MailConstants.SNOOZE_CALITEM_ALARM_REQUEST
          , MailConstants.GET_MINI_CAL_REQUEST
          , MailConstants.GET_RECUR_REQUEST
          , MailConstants.EXPAND_RECUR_REQUEST
          , MailConstants.CHECK_RECUR_CONFLICTS_REQUEST
          , MailConstants.CHECK_SPELLING_REQUEST
          , MailConstants.GET_SPELL_DICTIONARIES_REQUEST
          , MailConstants.GET_DATA_SOURCES_REQUEST
          , MailConstants.CREATE_DATA_SOURCE_REQUEST
          , MailConstants.MODIFY_DATA_SOURCE_REQUEST
          , MailConstants.TEST_DATA_SOURCE_REQUEST
          , MailConstants.DELETE_DATA_SOURCE_REQUEST
          , MailConstants.IMPORT_DATA_REQUEST
          , MailConstants.GET_IMPORT_STATUS_REQUEST
          , MailConstants.GET_DATA_SOURCE_USAGE_REQUEST
          , MailConstants.CREATE_WAIT_SET_REQUEST
          , MailConstants.WAIT_SET_REQUEST
          , MailConstants.DESTROY_WAIT_SET_REQUEST
          , MailConstants.GET_PERMISSION_REQUEST
          , MailConstants.CHECK_PERMISSION_REQUEST
          , MailConstants.GRANT_PERMISSION_REQUEST
          , MailConstants.REVOKE_PERMISSION_REQUEST
          , MailConstants.GET_EFFECTIVE_FOLDER_PERMS_REQUEST
          , MailConstants.AUTO_COMPLETE_REQUEST
          , MailConstants.FULL_AUTO_COMPLETE_REQUEST
          , MailConstants.RANKING_ACTION_REQUEST
          , MailConstants.GET_SHARE_NOTIFICATIONS_REQUEST
          , MailConstants.GET_SYSTEM_RETENTION_POLICY_REQUEST
          , MailConstants.GET_IMAP_RECENT_REQUEST
          , MailConstants.GET_IMAP_RECENT_CUTOFF_REQUEST
          , MailConstants.RECORD_IMAP_SESSION_REQUEST
          , MailConstants.IMAP_COPY_REQUEST
          , MailConstants.SAVE_IMAP_SUBSCRIPTIONS_REQUEST
          , MailConstants.LIST_IMAP_SUBSCRIPTIONS_REQUEST
          , MailConstants.OPEN_IMAP_FOLDER_REQUEST
          , MailConstants.BEGIN_TRACKING_IMAP_REQUEST
          , MailConstants.GET_LAST_ITEM_ID_IN_MAILBOX_REQUEST
          , MailConstants.GET_MODIFIED_ITEMS_IDS_REQUEST
          , MailConstants.RESET_RECENT_MESSAGE_COUNT_REQUEST
          , MailConstants.SEARCH_ACTION_REQUEST
          , MailConstants.RECOVER_ACCOUNT_REQUEST
          , MailConstants.SET_RECOVERY_EMAIL_REQUEST
          , MailConstants.COPY_TO_DRIVE_REQUEST
          , QName.get("CreateSmartLinksRequest", MailConstants.NAMESPACE));

  @Override
  public void registerHandlers(DocumentDispatcher dispatcher) throws ServiceException {
    TrackCommandRequestHandler handler = new TrackCommandRequestHandler();
    handlerNames.forEach(qname -> dispatcher.registerHandler(qname, handler));
  }

  protected Try<MessageBrokerClient> getMessageBroker() {
    return Try.of(() -> Mockito.mock(MessageBrokerClient.class));
  }
}
