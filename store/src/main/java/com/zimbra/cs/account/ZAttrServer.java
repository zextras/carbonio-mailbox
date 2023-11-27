// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Sep 23, 2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.account;

import static com.zimbra.common.account.ProvisioningConstants.FALSE;
import static com.zimbra.common.account.ProvisioningConstants.TRUE;

import com.zimbra.common.account.ZAttr;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.util.StringUtil;
import java.util.HashMap;
import java.util.Map;

/** AUTO-GENERATED. DO NOT EDIT. */
public abstract class ZAttrServer extends NamedEntry {

  protected ZAttrServer(
      String name,
      String id,
      Map<String, Object> attrs,
      Map<String, Object> defaults,
      Provisioning prov) {
    super(name, id, attrs, defaults, prov);
  }

  ///// BEGIN-AUTO-GEN-REPLACE

  /**
     * RFC2256: common name(s) for which the entity is known by
     *
     * @return cn, or null if unset
     */
    @ZAttr(id=-1)
    public String getCn() {
        return getAttr(ZAttrProvisioning.A_cn, null, true);
    }

  /**
     * Ehcache: default expiration time for activesync cache values; default
     * is 5 minutes. Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * <p>Use getActiveSyncEhcacheExpirationAsString to access value as a string.
     *
     * @see #getActiveSyncEhcacheExpirationAsString()
     *
     * @return zimbraActiveSyncEhcacheExpiration in millseconds, or 300000 (5m)  if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=3003)
    public long getActiveSyncEhcacheExpiration() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraActiveSyncEhcacheExpiration, 300000L, true);
    }

  /**
     * Ehcache: the maximum heap size of the ActiveSync cache in Bytes before
     * eviction. By default this value is 10MB. This is a rough limit,Due to
     * internals of ehcache actual size in memory will often exceed this
     * limit by a modest margin.
     *
     * @return zimbraActiveSyncEhcacheHeapSize, or 10485760 if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=3001)
    public long getActiveSyncEhcacheHeapSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraActiveSyncEhcacheHeapSize, 10485760L, true);
    }

  /**
     * Ehcache: the maximum disk size of the ActiveSync cache in Bytes before
     * eviction. By default this value is 10GB. This is a rough limit,Due to
     * internals of ehcache actual size on disk will often exceed this limit
     * by a modest margin.
     *
     * @return zimbraActiveSyncEhcacheMaxDiskSize, or 10737418240 if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=3002)
    public long getActiveSyncEhcacheMaxDiskSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraActiveSyncEhcacheMaxDiskSize, 10737418240L, true);
    }

  /**
     * Specifies whether the admin server should bound to localhost or not.
     * This is an immutable property and is generated based on
     * zimbraAdminBindAddress.
     *
     * @param zimbraAdminLocalBind new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1377)
    public void setAdminLocalBind(boolean zimbraAdminLocalBind) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAdminLocalBind, zimbraAdminLocalBind ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * SSL port for admin UI
     *
     * <p>Use getAdminPortAsString to access value as a string.
     *
     * @see #getAdminPortAsString()
     *
     * @return zimbraAdminPort, or 7071 if unset
     */
    @ZAttr(id=155)
    public int getAdminPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraAdminPort, 7071, true);
    }

  /**
     * URL prefix for where the zimbraAdmin app resides on this server
     *
     * @return zimbraAdminURL, or "/carbonioAdmin" if unset
     */
    @ZAttr(id=497)
    public String getAdminURL() {
        return getAttr(ZAttrProvisioning.A_zimbraAdminURL, "/carbonioAdmin", true);
    }

  /**
     * delay between each batch for zmspamextract
     *
     * @return zimbraAntispamExtractionBatchDelay, or 100 if unset
     *
     * @since ZCS 8.0.5
     */
    @ZAttr(id=1457)
    public int getAntispamExtractionBatchDelay() {
        return getIntAttr(ZAttrProvisioning.A_zimbraAntispamExtractionBatchDelay, 100, true);
    }

  /**
     * batch size for zmspamextract
     *
     * @return zimbraAntispamExtractionBatchSize, or 25 if unset
     *
     * @since ZCS 8.0.5
     */
    @ZAttr(id=1456)
    public int getAntispamExtractionBatchSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraAntispamExtractionBatchSize, 25, true);
    }

  /**
     * EAGER mode: required LAZY mode: N/A MANUAL mode: N/A Domain scheduled
     * for eager auto provision on this server. Scheduled domains must have
     * EAGER mode enabled in zimbraAutoProvMode. Multiple domains can be
     * scheduled on a server for EAGER auto provision. Also, a domain can be
     * scheduled on multiple servers for EAGER auto provision.
     *
     * @return zimbraAutoProvScheduledDomains, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1237)
    public String[] getAutoProvScheduledDomains() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraAutoProvScheduledDomains, true, true);
    }

  /**
     * EAGER mode: required LAZY mode: N/A MANUAL mode: N/A Domain scheduled
     * for eager auto provision on this server. Scheduled domains must have
     * EAGER mode enabled in zimbraAutoProvMode. Multiple domains can be
     * scheduled on a server for EAGER auto provision. Also, a domain can be
     * scheduled on multiple servers for EAGER auto provision.
     *
     * @param zimbraAutoProvScheduledDomains existing value to remove
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1237)
    public void removeAutoProvScheduledDomains(String zimbraAutoProvScheduledDomains) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        StringUtil.addToMultiMap(attrs, "-"  + ZAttrProvisioning.A_zimbraAutoProvScheduledDomains, zimbraAutoProvScheduledDomains);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Enabled using the configured server ID for blob dir
     *
     * @return zimbraConfiguredServerIDForBlobDirEnabled, or false if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1551)
    public boolean isConfiguredServerIDForBlobDirEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraConfiguredServerIDForBlobDirEnabled, false, true);
    }

  /**
     * Comma separated list of Contact attributes that should be hidden from
     * clients and export of contacts.
     *
     * @return zimbraContactHiddenAttributes, or "dn,vcardUID,vcardURL,vcardXProps,member" if unset
     *
     * @since ZCS 6.0.6
     */
    @ZAttr(id=1086)
    public String getContactHiddenAttributes() {
        return getAttr(ZAttrProvisioning.A_zimbraContactHiddenAttributes, "dn,vcardUID,vcardURL,vcardXProps,member", true);
    }

  /**
     * Specify the decomposition mode used for looking up the Contact items
     * by the phonetic last/first name or last/first name. The accepted value
     * is as follows: 0 = No decomposition. The accented characters will be
     * compared &#039;as is&#039; (not be decomposed)
     * (Collator.NO_DECOMPOSITION). 1 = Canonical decomposition mapping rules
     * found in the Unicode Standard is used
     * (Collator.CANONICAL_DECOMPOSITION). 2 = Unicode canonical and Unicode
     * compatibility decomposition mapping rules found in the Unicode
     * Standard is used. When this mode is selected, compatible characters,
     * such as half-width and full-width katakana characters, are considered
     * equivalent (Collator.FULL_DECOMPOSITION).
     *
     * @return zimbraContactSearchDecomposition, or 2 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=1971)
    public int getContactSearchDecomposition() {
        return getIntAttr(ZAttrProvisioning.A_zimbraContactSearchDecomposition, 2, true);
    }

  /**
     * SQL statements that take longer than this duration to execute will be
     * logged to the sqltrace category in mailbox.log.. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getDatabaseSlowSqlThresholdAsString to access value as a string.
     *
     * @see #getDatabaseSlowSqlThresholdAsString()
     *
     * @return zimbraDatabaseSlowSqlThreshold in millseconds, or 2000 (2s)  if unset
     *
     * @since ZCS 6.0.0_RC1
     */
    @ZAttr(id=1038)
    public long getDatabaseSlowSqlThreshold() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDatabaseSlowSqlThreshold, 2000L, true);
    }

  /**
     * EmptyFolderOpTimeout is the time in seconds for which empty folder
     * operation will wait for the current empty folder operation to complete
     *
     * @return zimbraEmptyFolderOpTimeout, or 3 if unset
     *
     * @since ZCS 8.6.0
     */
    @ZAttr(id=1652)
    public int getEmptyFolderOpTimeout() {
        return getIntAttr(ZAttrProvisioning.A_zimbraEmptyFolderOpTimeout, 3, true);
    }

  /**
     * Interval between successive executions of the task that: - disables an
     * external virtual account when all its accessible shares have been
     * revoked or expired. - deletes an external virtual account after
     * zimbraExternalAccountLifetimeAfterDisabled of being disabled. . Must
     * be in valid duration format: {digits}{time-unit}. digits: 0-9,
     * time-unit: [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days,
     * ms - milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getExternalAccountStatusCheckIntervalAsString to access value as a string.
     *
     * @see #getExternalAccountStatusCheckIntervalAsString()
     *
     * @return zimbraExternalAccountStatusCheckInterval in millseconds, or 86400000 (1d)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1370)
    public long getExternalAccountStatusCheckInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraExternalAccountStatusCheckInterval, 86400000L, true);
    }

  /**
     * Maximum size in bytes for file uploads
     *
     * @return zimbraFileUploadMaxSize, or 10485760 if unset
     */
    @ZAttr(id=227)
    public long getFileUploadMaxSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraFileUploadMaxSize, 10485760L, true);
    }

  /**
     * The interval to wait when the server encounters problems while
     * propagating Zimbra users free/busy information to external provider
     * such as Exchange. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getFreebusyPropagationRetryIntervalAsString to access value as a string.
     *
     * @see #getFreebusyPropagationRetryIntervalAsString()
     *
     * @return zimbraFreebusyPropagationRetryInterval in millseconds, or 60000 (1m)  if unset
     *
     * @since ZCS 5.0.17
     */
    @ZAttr(id=1026)
    public long getFreebusyPropagationRetryInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFreebusyPropagationRetryInterval, 60000L, true);
    }

  /**
     * IP addresses to ignore when applying Jetty DosFilter.
     *
     * @return zimbraHttpThrottleSafeIPs, or empty array if unset
     *
     * @since ZCS 8.0.3
     */
    @ZAttr(id=1427)
    public String[] getHttpThrottleSafeIPs() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraHttpThrottleSafeIPs, true, true);
    }

  /**
     * Zimbra Systems Unique ID
     *
     * @return zimbraId, or null if unset
     */
    @ZAttr(id=1)
    public String getId() {
        return getAttr(ZAttrProvisioning.A_zimbraId, null, true);
    }

  /**
     * Ehcache: the maximum amount of disk space the imap active session
     * cache will consume in Bytes before eviction. By default this value is
     * 100 gigabytes. This is a rough limit,Due to internals of ehcache
     * actual size in memory will often exceed this limit by a modest margin.
     *
     * @return zimbraImapActiveSessionEhcacheMaxDiskSize, or 107374182400 if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=3005)
    public long getImapActiveSessionEhcacheMaxDiskSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraImapActiveSessionEhcacheMaxDiskSize, 107374182400L, true);
    }

  /**
     * whether to display IMAP Mail folders only
     *
     * @return zimbraImapDisplayMailFoldersOnly, or true if unset
     *
     * @since ZCS 8.7.0,9.0
     */
    @ZAttr(id=1909)
    public boolean isImapDisplayMailFoldersOnly() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraImapDisplayMailFoldersOnly, true, true);
    }

  /**
     * Ehcache: the maximum disk size of inactive IMAP cache in Bytes before
     * eviction.By default this value is 10GB.This is a rough limit,Due to
     * internals of ehcache actual size on disk will often exceed this limit
     * by a modest margin.
     *
     * @return zimbraImapInactiveSessionCacheMaxDiskSize, or 10737418240 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2013)
    public long getImapInactiveSessionCacheMaxDiskSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraImapInactiveSessionCacheMaxDiskSize, 10737418240L, true);
    }

  /**
     * Ehcache: the maximum heap size of the inactive session cache in Bytes
     * before eviction. By default this value is 1 megabyte. This is a rough
     * limit,Due to internals of ehcache actual size in memory will often
     * exceed this limit by a modest margin.
     *
     * @return zimbraImapInactiveSessionEhcacheSize, or 1048576 if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=3004)
    public long getImapInactiveSessionEhcacheSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraImapInactiveSessionEhcacheSize, 1048576L, true);
    }

  /**
     * port number on which IMAP proxy server should listen
     *
     * @return zimbraImapProxyBindPort, or "143" if unset
     */
    @ZAttr(id=348)
    public String getImapProxyBindPortAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraImapProxyBindPort, "143", true);
    }

  /**
     * port number on which IMAPS proxy server should listen
     *
     * @return zimbraImapSSLProxyBindPort, or "993" if unset
     */
    @ZAttr(id=349)
    public String getImapSSLProxyBindPortAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraImapSSLProxyBindPort, "993", true);
    }

  /**
     * whether IMAP SSL server is enabled for a given server
     *
     * @return zimbraImapSSLServerEnabled, or true if unset
     */
    @ZAttr(id=184)
    public boolean isImapSSLServerEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraImapSSLServerEnabled, true, true);
    }

  /**
     * whether IMAP is enabled for a server
     *
     * @return zimbraImapServerEnabled, or true if unset
     */
    @ZAttr(id=176)
    public boolean isImapServerEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraImapServerEnabled, true, true);
    }

  /**
     * This attribute is used for failed authentication requests. It
     * indicates the minimum time between current req and last req from the
     * same IP before this suspended IP will be reinstated
     *
     * @return zimbraInvalidLoginFilterDelayInMinBetwnReqBeforeReinstating, or 15 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1614)
    public int getInvalidLoginFilterDelayInMinBetwnReqBeforeReinstating() {
        return getIntAttr(ZAttrProvisioning.A_zimbraInvalidLoginFilterDelayInMinBetwnReqBeforeReinstating, 15, true);
    }

  /**
     * This attribute is used for failed authentication requests.This is a
     * DOSFilter style check for repeated failed logins from IP, if set to 0
     * no check happens, else failed login is recorded.
     *
     * @return zimbraInvalidLoginFilterMaxFailedLogin, or 10 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1613)
    public int getInvalidLoginFilterMaxFailedLogin() {
        return getIntAttr(ZAttrProvisioning.A_zimbraInvalidLoginFilterMaxFailedLogin, 10, true);
    }

  /**
     * This attribute is used for failed authentication requests. It
     * indicates the max size of data structures that holds the list of
     * failed logins
     *
     * @return zimbraInvalidLoginFilterMaxSizeOfFailedIpDb, or 7000 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1618)
    public int getInvalidLoginFilterMaxSizeOfFailedIpDb() {
        return getIntAttr(ZAttrProvisioning.A_zimbraInvalidLoginFilterMaxSizeOfFailedIpDb, 7000, true);
    }

  /**
     * This attribute is used for failed authentication requests. Interval at
     * which Task to reinstate IPs suspended as part of ZimbraInvalidLoging
     * filter are run.
     *
     * @return zimbraInvalidLoginFilterReinstateIpTaskIntervalInMin, or 5 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1615)
    public int getInvalidLoginFilterReinstateIpTaskIntervalInMin() {
        return getIntAttr(ZAttrProvisioning.A_zimbraInvalidLoginFilterReinstateIpTaskIntervalInMin, 5, true);
    }

  /**
     * Maximum number of item to perform an ItemAction on at a time.
     *
     * @return zimbraItemActionBatchSize, or 1000 if unset
     *
     * @since ZCS 8.0.5
     */
    @ZAttr(id=1451)
    public int getItemActionBatchSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraItemActionBatchSize, 1000, true);
    }

  /**
     * Maximum duration beyond which the mailbox must be scheduled for purge
     * irrespective of whether it is loaded into memory or not.. Must be in
     * valid duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getLastPurgeMaxDurationAsString to access value as a string.
     *
     * @see #getLastPurgeMaxDurationAsString()
     *
     * @return zimbraLastPurgeMaxDuration in millseconds, or 2592000000 (30d)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1382)
    public long getLastPurgeMaxDuration() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraLastPurgeMaxDuration, 2592000000L, true);
    }

  /**
     * Whether to include fractional seconds in LDAP gentime values (e.g.
     * zimbraPasswordLockoutFailureTime or
     * zimbraGalLastSuccessfulSyncTimestamp). Releases prior to 8.7 are
     * unable to parse gentime values which include fractional seconds;
     * therefore this value must remain set to FALSE in environments where
     * any release 8.6 or lower is present. It should be changed to TRUE once
     * all systems are upgraded to 8.7 or higher.
     *
     * @return zimbraLdapGentimeFractionalSecondsEnabled, or true if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2018)
    public boolean isLdapGentimeFractionalSecondsEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraLdapGentimeFractionalSecondsEnabled, true, true);
    }

  /**
     * port number on which LMTP server should listen
     *
     * <p>Use getLmtpBindPortAsString to access value as a string.
     *
     * @see #getLmtpBindPortAsString()
     *
     * @return zimbraLmtpBindPort, or 7025 if unset
     */
    @ZAttr(id=24)
    public int getLmtpBindPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraLmtpBindPort, 7025, true);
    }

  /**
     * version of lowest supported authentication protocol
     *
     * @return zimbraLowestSupportedAuthVersion, or 2 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1589)
    public int getLowestSupportedAuthVersion() {
        return getIntAttr(ZAttrProvisioning.A_zimbraLowestSupportedAuthVersion, 2, true);
    }

  /**
     * Maximum size in bytes for the &lt;content &gt; element in SOAP. Mail
     * content larger than this limit will be truncated.
     *
     * @return zimbraMailContentMaxSize, or 10240000 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=807)
    public long getMailContentMaxSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraMailContentMaxSize, 10240000L, true);
    }

  /**
     * Incoming messages larger than this number of bytes are streamed to
     * disk during LMTP delivery, instead of being read into memory. This
     * limits memory consumption at the expense of higher disk utilization.
     *
     * @return zimbraMailDiskStreamingThreshold, or 1048576 if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=565)
    public int getMailDiskStreamingThreshold() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailDiskStreamingThreshold, 1048576, true);
    }

  /**
     * Maximum number of messages to delete during a single transaction when
     * emptying a large folder.
     *
     * @return zimbraMailEmptyFolderBatchSize, or 1000 if unset
     *
     * @since ZCS 6.0.8
     */
    @ZAttr(id=1097)
    public int getMailEmptyFolderBatchSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailEmptyFolderBatchSize, 1000, true);
    }

  /**
     * Maximum number of file descriptors that are opened for accessing
     * message content.
     *
     * @return zimbraMailFileDescriptorCacheSize, or 1000 if unset
     *
     * @since ZCS 6.0.0_RC1
     */
    @ZAttr(id=1034)
    public int getMailFileDescriptorCacheSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailFileDescriptorCacheSize, 1000, true);
    }

  /**
     * When set to true, robots.txt on mailboxd will be set up to keep web
     * crawlers out
     *
     * @return zimbraMailKeepOutWebCrawlers, or true if unset
     *
     * @since ZCS 7.0.1
     */
    @ZAttr(id=1161)
    public boolean isMailKeepOutWebCrawlers() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailKeepOutWebCrawlers, true, true);
    }

  /**
     * Specifies whether the http server should bound to localhost or not.
     * This is an immutable property and is generated based on zimbraMailMode
     * and zimbraMailBindAddress.
     *
     * @param zimbraMailLocalBind new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1380)
    public void setMailLocalBind(boolean zimbraMailLocalBind) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailLocalBind, zimbraMailLocalBind ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether to run HTTP or HTTPS or both/mixed mode or redirect mode. See
     * also related attributes zimbraMailPort and zimbraMailSSLPort
     *
     * <p>Valid values: [http, https, both, mixed, redirect]
     *
     * @return zimbraMailMode, or ZAttrProvisioning.MailMode.both if unset and/or has invalid value
     */
    @ZAttr(id=308)
    public ZAttrProvisioning.MailMode getMailMode() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraMailMode, true, true); return v == null ? ZAttrProvisioning.MailMode.both : ZAttrProvisioning.MailMode.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.MailMode.both; }
    }

  /**
     * HTTP port for end-user UI
     *
     * @param zimbraMailPort new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=154)
    public void setMailPort(int zimbraMailPort) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailPort, Integer.toString(zimbraMailPort));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Maximum number of messages to delete from a folder during a single
     * purge operation. If the limit is exceeded, the mailbox is purged again
     * at the end of the purge cycle until all qualifying messages are
     * purged.
     *
     * @return zimbraMailPurgeBatchSize, or 1000 if unset
     *
     * @since ZCS 6.0.8
     */
    @ZAttr(id=1096)
    public int getMailPurgeBatchSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailPurgeBatchSize, 1000, true);
    }

  /**
     * If TRUE, the envelope sender of a message redirected by mail filters
     * will be set to the users address. If FALSE, the envelope sender will
     * be set to the From address of the redirected message.
     *
     * @return zimbraMailRedirectSetEnvelopeSender, or true if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=764)
    public boolean isMailRedirectSetEnvelopeSender() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailRedirectSetEnvelopeSender, true, true);
    }

  /**
     * enable authentication via X.509 Client Certificate. Disabled: client
     * authentication is disabled. NeedClientAuth: client authentication is
     * required during SSL handshake on the SSL mutual authentication
     * port(see zimbraMailSSLClientCertPort). The SSL handshake will fail if
     * the client does not present a certificate to authenticate.
     * WantClientAuth: client authentication is requested during SSL
     * handshake on the SSL mutual authentication port(see
     * zimbraMailSSLClientCertPort). The SSL handshake will still proceed if
     * the client does not present a certificate to authenticate. In the case
     * when client does not send a certificate, user will be redirected to
     * the usual entry page of the requested webapp, where username/password
     * is prompted.
     *
     * <p>Valid values: [Disabled, NeedClientAuth, WantClientAuth]
     *
     * @return zimbraMailSSLClientCertMode, or ZAttrProvisioning.MailSSLClientCertMode.Disabled if unset and/or has invalid value
     *
     * @since ZCS 7.1.0
     */
    @ZAttr(id=1190)
    public ZAttrProvisioning.MailSSLClientCertMode getMailSSLClientCertMode() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraMailSSLClientCertMode, true, true); return v == null ? ZAttrProvisioning.MailSSLClientCertMode.Disabled : ZAttrProvisioning.MailSSLClientCertMode.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.MailSSLClientCertMode.Disabled; }
    }

  /**
     * enable OCSP support for two way authentication.
     *
     * @return zimbraMailSSLClientCertOCSPEnabled, or true if unset
     *
     * @since ZCS 7.2.0
     */
    @ZAttr(id=1395)
    public boolean isMailSSLClientCertOCSPEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailSSLClientCertOCSPEnabled, true, true);
    }

  /**
     * SSL port HTTP proxy
     *
     * <p>Use getMailSSLProxyPortAsString to access value as a string.
     *
     * @see #getMailSSLProxyPortAsString()
     *
     * @return zimbraMailSSLProxyPort, or 0 if unset
     *
     * @since ZCS 5.0.3
     */
    @ZAttr(id=627)
    public int getMailSSLProxyPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailSSLProxyPort, 0, true);
    }

  /**
     * SSL port HTTP proxy
     *
     * @param zimbraMailSSLProxyPort new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.3
     */
    @ZAttr(id=627)
    public void setMailSSLProxyPort(int zimbraMailSSLProxyPort) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailSSLProxyPort, Integer.toString(zimbraMailSSLProxyPort));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * URL prefix for where the zimbra app resides on this server
     *
     * @return zimbraMailURL, or "/" if unset
     */
    @ZAttr(id=340)
    public String getMailURL() {
        return getAttr(ZAttrProvisioning.A_zimbraMailURL, "/", true);
    }

  /**
     * Used to control whether Java NIO direct buffers are used. Value is
     * propagated to Jetty configuration. In the future, other NIO pieces
     * (IMAP/POP/LMTP) will also honor this.
     *
     * @return zimbraMailUseDirectBuffers, or false if unset
     *
     * @since ZCS 5.0.22
     */
    @ZAttr(id=1002)
    public boolean isMailUseDirectBuffers() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailUseDirectBuffers, false, true);
    }

  /**
     * Time in milliseconds between IMAP/POP/LMTP rate limiter stale entry
     * cleanup cycle.. Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * <p>Use getMailboxThrottleReapIntervalAsString to access value as a string.
     *
     * @see #getMailboxThrottleReapIntervalAsString()
     *
     * @return zimbraMailboxThrottleReapInterval in millseconds, or 60000 (60s)  if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2053)
    public long getMailboxThrottleReapInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraMailboxThrottleReapInterval, 60000L, true);
    }

  /**
     * Maximum number of JavaMail MimeMessage objects in the message cache.
     *
     * @return zimbraMessageCacheSize, or 200 if unset
     */
    @ZAttr(id=297)
    public int getMessageCacheSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMessageCacheSize, 200, true);
    }

  /**
     * whether message channel service is enabled on this server
     *
     * @return zimbraMessageChannelEnabled, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1417)
    public boolean isMessageChannelEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMessageChannelEnabled, false, true);
    }

  /**
     * port number on which message channel should listen
     *
     * @return zimbraMessageChannelPort, or 7285 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1415)
    public int getMessageChannelPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMessageChannelPort, 7285, true);
    }

  /**
     * Internal port used by saslauthd to authenticate over SOAP
     *
     * <p>Use getMtaAuthPortAsString to access value as a string.
     *
     * @see #getMtaAuthPortAsString()
     *
     * @return zimbraMtaAuthPort, or 7073 if unset
     *
     * @since ZCS 8.7,9.0.0
     */
    @ZAttr(id=1906)
    public int getMtaAuthPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMtaAuthPort, 7073, true);
    }

  /**
     * Value for postconf smtpd_tls_security_level
     *
     * <p>Valid values: [may, none]
     *
     * @return zimbraMtaTlsSecurityLevel, or ZAttrProvisioning.MtaTlsSecurityLevel.may if unset and/or has invalid value
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=795)
    public ZAttrProvisioning.MtaTlsSecurityLevel getMtaTlsSecurityLevel() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraMtaTlsSecurityLevel, true, true); return v == null ? ZAttrProvisioning.MtaTlsSecurityLevel.may : ZAttrProvisioning.MtaTlsSecurityLevel.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.MtaTlsSecurityLevel.may; }
    }

  /**
     * Whether to enable zimbra network new generation admin module.
     *
     * @return zimbraNetworkAdminNGEnabled, or false if unset
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2130)
    public boolean isNetworkAdminNGEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraNetworkAdminNGEnabled, false, true);
    }

  /**
     * Whether to enable zimbra network new generation mobile sync module.
     *
     * @return zimbraNetworkMobileNGEnabled, or false if unset
     *
     * @since ZCS 8.8.0
     */
    @ZAttr(id=2118)
    public boolean isNetworkMobileNGEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraNetworkMobileNGEnabled, false, true);
    }

  /**
     * Whether to enable zimbra network new generation modules.
     *
     * @return zimbraNetworkModulesNGEnabled, or true if unset
     *
     * @since ZCS 8.8.0
     */
    @ZAttr(id=2117)
    public boolean isNetworkModulesNGEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraNetworkModulesNGEnabled, true, true);
    }

  /**
     * The max number of IMAP messages returned by OpenImapFolderRequest
     * before pagination begins
     *
     * @return zimbraOpenImapFolderRequestChunkSize, or 1000 if unset
     *
     * @since ZCS 8.8.1
     */
    @ZAttr(id=3012)
    public int getOpenImapFolderRequestChunkSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraOpenImapFolderRequestChunkSize, 1000, true);
    }

  /**
     * Max number of previous residing folders server tracks for a mail item
     *
     * @return zimbraPrevFoldersToTrackMax, or 10 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1592)
    public int getPrevFoldersToTrackMax() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPrevFoldersToTrackMax, 10, true);
    }

  /**
     * If set as TRUE, proxy will use SSL to connect to the upstream mail
     * servers for web and mail proxy. Note admin console proxy always use
     * https no matter how this attr is set.
     *
     * @return zimbraReverseProxySSLToUpstreamEnabled, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1360)
    public boolean isReverseProxySSLToUpstreamEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraReverseProxySSLToUpstreamEnabled, true, true);
    }

  /**
     * The pool of servers that are available to the proxy for handling IMAP
     * sessions. If empty, the NginxLookupExtension will select the mailbox
     * server that hosts the account.
     *
     * @return zimbraReverseProxyUpstreamImapServers, or empty array if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=3008)
    public String[] getReverseProxyUpstreamImapServers() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraReverseProxyUpstreamImapServers, true, true);
    }

  /**
     * Local HTTP-BIND URL prefix where ZWC sends XMPP over BOSH requests
     *
     * @return zimbraReverseProxyXmppBoshLocalHttpBindURL, or "/http-bind" if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=1957)
    public String getReverseProxyXmppBoshLocalHttpBindURL() {
        return getAttr(ZAttrProvisioning.A_zimbraReverseProxyXmppBoshLocalHttpBindURL, "/http-bind", true);
    }

  /**
     * SSL certificate
     *
     * @return zimbraSSLCertificate, or null if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=563)
    public String getSSLCertificate() {
        return getAttr(ZAttrProvisioning.A_zimbraSSLCertificate, null, true);
    }

  /**
     * SSL private key
     *
     * @return zimbraSSLPrivateKey, or null if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=564)
    public String getSSLPrivateKey() {
        return getAttr(ZAttrProvisioning.A_zimbraSSLPrivateKey, null, true);
    }

  /**
     * public hostname of the host
     *
     * @return zimbraServiceHostname, or null if unset
     */
    @ZAttr(id=65)
    public String getServiceHostname() {
        return getAttr(ZAttrProvisioning.A_zimbraServiceHostname, null, true);
    }

    /**
     * public hostname of the host
     *
     * @param zimbraServiceHostname new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=65)
    public void setServiceHostname(String zimbraServiceHostname) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraServiceHostname, zimbraServiceHostname);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Account name for authenticating to share notification MTA.
     *
     * @return zimbraShareNotificationMtaAuthAccount, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1343)
    public String getShareNotificationMtaAuthAccount() {
        return getAttr(ZAttrProvisioning.A_zimbraShareNotificationMtaAuthAccount, null, true);
    }

    /**
     * Account name for authenticating to share notification MTA.
     *
     * @param zimbraShareNotificationMtaAuthAccount new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1343)
    public void setShareNotificationMtaAuthAccount(String zimbraShareNotificationMtaAuthAccount) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraShareNotificationMtaAuthAccount, zimbraShareNotificationMtaAuthAccount);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Password for authenticating to share notification MTA.
     *
     * @return zimbraShareNotificationMtaAuthPassword, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1344)
    public String getShareNotificationMtaAuthPassword() {
        return getAttr(ZAttrProvisioning.A_zimbraShareNotificationMtaAuthPassword, null, true);
    }

    /**
     * Password for authenticating to share notification MTA.
     *
     * @param zimbraShareNotificationMtaAuthPassword new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1344)
    public void setShareNotificationMtaAuthPassword(String zimbraShareNotificationMtaAuthPassword) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraShareNotificationMtaAuthPassword, zimbraShareNotificationMtaAuthPassword);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether to use credential to authenticate to share notification MTA.
     *
     * @return zimbraShareNotificationMtaAuthRequired, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1346)
    public boolean isShareNotificationMtaAuthRequired() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraShareNotificationMtaAuthRequired, false, true);
    }

    /**
     * Whether to use credential to authenticate to share notification MTA.
     *
     * @param zimbraShareNotificationMtaAuthRequired new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1346)
    public void setShareNotificationMtaAuthRequired(boolean zimbraShareNotificationMtaAuthRequired) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraShareNotificationMtaAuthRequired, zimbraShareNotificationMtaAuthRequired ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Connection mode when connecting to share notification MTA.
     *
     * <p>Valid values: [CLEARTEXT, SSL, STARTTLS]
     *
     * @return zimbraShareNotificationMtaConnectionType, or ZAttrProvisioning.ShareNotificationMtaConnectionType.CLEARTEXT if unset and/or has invalid value
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1345)
    public ZAttrProvisioning.ShareNotificationMtaConnectionType getShareNotificationMtaConnectionType() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraShareNotificationMtaConnectionType, true, true); return v == null ? ZAttrProvisioning.ShareNotificationMtaConnectionType.CLEARTEXT : ZAttrProvisioning.ShareNotificationMtaConnectionType.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.ShareNotificationMtaConnectionType.CLEARTEXT; }
    }

  /**
     * Connection mode when connecting to share notification MTA.
     *
     * <p>Valid values: [CLEARTEXT, SSL, STARTTLS]
     *
     * @param zimbraShareNotificationMtaConnectionType new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1345)
    public void setShareNotificationMtaConnectionType(ZAttrProvisioning.ShareNotificationMtaConnectionType zimbraShareNotificationMtaConnectionType) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraShareNotificationMtaConnectionType, zimbraShareNotificationMtaConnectionType.toString());
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * SMTP hostname for share notification MTA used for sending email
     * notifications.
     *
     * @return zimbraShareNotificationMtaHostname, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1341)
    public String getShareNotificationMtaHostname() {
        return getAttr(ZAttrProvisioning.A_zimbraShareNotificationMtaHostname, null, true);
    }

    /**
     * SMTP hostname for share notification MTA used for sending email
     * notifications.
     *
     * @param zimbraShareNotificationMtaHostname new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1341)
    public void setShareNotificationMtaHostname(String zimbraShareNotificationMtaHostname) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraShareNotificationMtaHostname, zimbraShareNotificationMtaHostname);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * SMTP port for share notification MTA used for sending email
     * notifications.
     *
     * @return zimbraShareNotificationMtaPort, or -1 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1342)
    public int getShareNotificationMtaPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraShareNotificationMtaPort, -1, true);
    }

    /**
     * SMTP port for share notification MTA used for sending email
     * notifications.
     *
     * @param zimbraShareNotificationMtaPort new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1342)
    public void setShareNotificationMtaPort(int zimbraShareNotificationMtaPort) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraShareNotificationMtaPort, Integer.toString(zimbraShareNotificationMtaPort));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Interval between successive executions of the task that publishes
     * shared item updates to LDAP. Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getSharingUpdatePublishIntervalAsString to access value as a string.
     *
     * @see #getSharingUpdatePublishIntervalAsString()
     *
     * @return zimbraSharingUpdatePublishInterval in millseconds, or 900000 (15m)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1220)
    public long getSharingUpdatePublishInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraSharingUpdatePublishInterval, 900000L, true);
    }

  /**
     * Maximum time an entry in the short term All Effective Rights cache
     * will be regarded as valid. If value is 0, the cache is disabled. The
     * cache is particularly useful when significant use is made of delegated
     * administration. This cache can improve performance by avoiding
     * recomputing All Effective Rights of named entries like accounts
     * frequently in a short period of time. All Effective Rights are
     * computations of the rights that named entries like accounts have -
     * although when used, they are checked separately. The longer the value
     * of this setting is, the more stale the view of the details is likely
     * to be. For this reason, the maximum accepted value is 30m. Larger
     * values will be treated as being 30m . Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * <p>Use getShortTermAllEffectiveRightsCacheExpirationAsString to access value as a string.
     *
     * @see #getShortTermAllEffectiveRightsCacheExpirationAsString()
     *
     * @return zimbraShortTermAllEffectiveRightsCacheExpiration in millseconds, or 50000 (50s)  if unset
     *
     * @since ZCS 8.7.0
     */
    @ZAttr(id=1903)
    public long getShortTermAllEffectiveRightsCacheExpiration() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraShortTermAllEffectiveRightsCacheExpiration, 50000L, true);
    }

  /**
     * Maximum number of entries in the short term All Effective Rights
     * cache. This cache can improve performance by avoiding recomputing All
     * Effective Rights of named entries like accounts frequently in a short
     * period of time. Can disable the cache be specifying a value of 0
     *
     * @return zimbraShortTermAllEffectiveRightsCacheSize, or 128 if unset
     *
     * @since ZCS 8.7.0
     */
    @ZAttr(id=1902)
    public int getShortTermAllEffectiveRightsCacheSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraShortTermAllEffectiveRightsCacheSize, 128, true);
    }

  /**
     * Maximum time an entry in the Grantee cache will be regarded as valid.
     * If value is 0, the cache is disabled. This cache can improve
     * performance by avoiding recomputing details frequently in a short
     * period of time, for instance for each entry in search results. The
     * cache is particularly useful when significant use is made of delegated
     * administration. Grantees objects provide a view of what rights a
     * grantee has - although those are checked separately. The longer the
     * value of this setting is, the more stale the view of the details is
     * likely to be. For this reason, the maximum accepted value is 30m.
     * Larger values will be treated as being 30m . Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * <p>Use getShortTermGranteeCacheExpirationAsString to access value as a string.
     *
     * @see #getShortTermGranteeCacheExpirationAsString()
     *
     * @return zimbraShortTermGranteeCacheExpiration in millseconds, or 50000 (50s)  if unset
     *
     * @since ZCS 8.7.0
     */
    @ZAttr(id=1901)
    public long getShortTermGranteeCacheExpiration() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraShortTermGranteeCacheExpiration, 50000L, true);
    }

  /**
     * Maximum number of entries in the short term Grantee cache. This cache
     * can improve performance by avoiding recomputing details frequently in
     * a short period of time, for instance for each entry in search results.
     * Can disable the cache be specifying a value of 0
     *
     * @return zimbraShortTermGranteeCacheSize, or 128 if unset
     *
     * @since ZCS 8.7.0
     */
    @ZAttr(id=1900)
    public int getShortTermGranteeCacheSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraShortTermGranteeCacheSize, 128, true);
    }

  /**
     * the SMTP server to connect to when sending mail
     *
     * @return zimbraSmtpHostname, or empty array if unset
     */
    @ZAttr(id=97)
    public String[] getSmtpHostname() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraSmtpHostname, true, true); return value.length > 0 ? value : new String[] {"127.78.0.7"};
    }

    /**
     * the SMTP server to connect to when sending mail
     *
     * @param zimbraSmtpHostname new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=97)
    public void setSmtpHostname(String[] zimbraSmtpHostname) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSmtpHostname, zimbraSmtpHostname);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * the SMTP server port to connect to when sending mail
     *
     * <p>Use getSmtpPortAsString to access value as a string.
     *
     * @see #getSmtpPortAsString()
     *
     * @return zimbraSmtpPort, or 20025 if unset
     */
    @ZAttr(id=98)
    public int getSmtpPort() {
        return getIntAttr(ZAttrProvisioning.A_zimbraSmtpPort, 20025, true);
    }

  /**
     * the SMTP server port to connect to when sending mail
     *
     * @param zimbraSmtpPort new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=98)
    public void setSmtpPort(int zimbraSmtpPort) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSmtpPort, Integer.toString(zimbraSmtpPort));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Value of the mail.smtp.sendpartial property
     *
     * @param zimbraSmtpSendPartial new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=249)
    public void setSmtpSendPartial(boolean zimbraSmtpSendPartial) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSmtpSendPartial, zimbraSmtpSendPartial ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * timeout value in seconds
     *
     * @param zimbraSmtpTimeout new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=99)
    public void setSmtpTimeout(int zimbraSmtpTimeout) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSmtpTimeout, Integer.toString(zimbraSmtpTimeout));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * The list of available dictionaries that can be used for spell
     * checking.
     *
     * @return zimbraSpellAvailableDictionary, or empty array if unset
     *
     * @since ZCS 6.0.0_GA
     */
    @ZAttr(id=1042)
    public String[] getSpellAvailableDictionary() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraSpellAvailableDictionary, true, true); return value.length > 0 ? value : new String[] {"en_US"};
    }

  /**
     * Prefixes of thread names. Each value is a column in threads.csv that
     * tracks the number of threads whose name starts with the given prefix.
     *
     * @return zimbraStatThreadNamePrefix, or empty array if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=792)
    public String[] getStatThreadNamePrefix() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraStatThreadNamePrefix, true, true); return value.length > 0 ? value : new String[] {"btpool","pool","LmtpServer","ImapServer","ImapSSLServer","Pop3Server","Pop3SSLServer","ScheduledTask","Timer","AnonymousIoService","CloudRoutingReaderThread","GC","SocketAcceptor","Thread","qtp"};
    }

  /**
     * weclient URL to directly connect when making service to JS calls from
     * mail server in split mode
     *
     * @return zimbraWebClientURL, or null if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1445)
    public String getWebClientURL() {
        return getAttr(ZAttrProvisioning.A_zimbraWebClientURL, null, true);
    }

  ///// END-AUTO-GEN-REPLACE

}
