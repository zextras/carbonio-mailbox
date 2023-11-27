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

import com.zimbra.common.account.ZAttr;
import com.zimbra.common.account.ZAttrProvisioning;
import java.util.HashMap;
import java.util.Map;

/** AUTO-GENERATED. DO NOT EDIT. */
public abstract class ZAttrConfig extends Entry {

  public ZAttrConfig(Map<String, Object> attrs, Provisioning provisioning) {
    super(attrs, null, provisioning);
  }

  ///// BEGIN-AUTO-GEN-REPLACE

  /**
     * email address of sender used to send emails about important
     * infrastructure notifications
     *
     * @return carbonioNotificationFrom, or null if unset
     *
     * @since ZCS 23.4.0
     */
    @ZAttr(id=3127)
    public String getCarbonioNotificationFrom() {
        return getAttr(ZAttrProvisioning.A_carbonioNotificationFrom, null, true);
    }

  /**
     * email address of recipients who will receive emails about important
     * infrastructure notifications
     *
     * @return carbonioNotificationRecipients, or empty array if unset
     *
     * @since ZCS 23.4.0
     */
    @ZAttr(id=3128)
    public String[] getCarbonioNotificationRecipients() {
        return getMultiAttr(ZAttrProvisioning.A_carbonioNotificationRecipients, true, true);
    }

  /**
     * block all attachment downloading
     *
     * @return zimbraAttachmentsBlocked, or false if unset
     */
    @ZAttr(id=115)
    public boolean isAttachmentsBlocked() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAttachmentsBlocked, false, true);
    }

  /**
     * Information about the latest run of zmmigrateattrs. Includes the URL
     * of the destination ephemeral store and the state of the migration (in
     * progress, completed, failed)
     *
     * @return zimbraAttributeMigrationInfo, or null if unset
     *
     * @since ZCS 8.8.1
     */
    @ZAttr(id=3019)
    public String getAttributeMigrationInfo() {
        return getAttr(ZAttrProvisioning.A_zimbraAttributeMigrationInfo, null, true);
    }

    /**
     * Information about the latest run of zmmigrateattrs. Includes the URL
     * of the destination ephemeral store and the state of the migration (in
     * progress, completed, failed)
     *
     * @param zimbraAttributeMigrationInfo new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.1
     */
    @ZAttr(id=3019)
    public void setAttributeMigrationInfo(String zimbraAttributeMigrationInfo) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAttributeMigrationInfo, zimbraAttributeMigrationInfo);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Information about the latest run of zmmigrateattrs. Includes the URL
     * of the destination ephemeral store and the state of the migration (in
     * progress, completed, failed)
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.1
     */
    @ZAttr(id=3019)
    public void unsetAttributeMigrationInfo() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAttributeMigrationInfo, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether auth token validity value checking should be performed during
     * auth token validation. See description for
     * zimbraAuthTokenValidityValue.
     *
     * @return zimbraAuthTokenValidityValueEnabled, or true if unset
     *
     * @since ZCS 6.0.7
     */
    @ZAttr(id=1094)
    public boolean isAuthTokenValidityValueEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAuthTokenValidityValueEnabled, true, true);
    }

  /**
     * If TRUE, use a null return path for envelope MAIL FROM when sending
     * out of office and new mail notifications. If FALSE, the account
     * address is used for the return path. Note that setting the value to
     * TRUE may cause failed delivery of some out of office or new mail
     * notifications because some agents require a valid sender.
     *
     * @return zimbraAutoSubmittedNullReturnPath, or false if unset
     */
    @ZAttr(id=502)
    public boolean isAutoSubmittedNullReturnPath() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAutoSubmittedNullReturnPath, false, true);
    }

  /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: A list of hosts like www.abc.com, www.xyz.com. These are
     * used while doing CSRF referer check.
     *
     * @return zimbraCsrfAllowedRefererHosts, or empty array if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1630)
    public String[] getCsrfAllowedRefererHosts() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraCsrfAllowedRefererHosts, true, true);
    }

  /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: A flag to turn on or off CSRF referer related check. When
     * set to FALSE no CSRF referer check happens. When set to true CSRF
     * referer type check happens.
     *
     * @return zimbraCsrfRefererCheckEnabled, or true if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1631)
    public boolean isCsrfRefererCheckEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraCsrfRefererCheckEnabled, true, true);
    }

  /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: A flag to turn on or off CSRF token related check. When set
     * to FALSE no CSRF check happens. When set to true both CSRF referer and
     * CSRF token change is effective.
     *
     * @return zimbraCsrfTokenCheckEnabled, or false if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1628)
    public boolean isCsrfTokenCheckEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraCsrfTokenCheckEnabled, false, true);
    }

  /**
     * Custom RFC822 header names (case-sensitive) allowed to specify in
     * SendMsgRequest
     *
     * @return zimbraCustomMimeHeaderNameAllowed, or empty array if unset
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1265)
    public String[] getCustomMimeHeaderNameAllowed() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraCustomMimeHeaderNameAllowed, true, true);
    }

    /**
     * Custom RFC822 header names (case-sensitive) allowed to specify in
     * SendMsgRequest
     *
     * @param zimbraCustomMimeHeaderNameAllowed new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1265)
    public void setCustomMimeHeaderNameAllowed(String[] zimbraCustomMimeHeaderNameAllowed) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraCustomMimeHeaderNameAllowed, zimbraCustomMimeHeaderNameAllowed);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Which security layer to use for connection (cleartext, ssl, tls, or
     * tls if available). If not set on data source, fallback to the value on
     * global config.
     *
     * <p>Valid values: [cleartext, ssl, tls, tls_if_available]
     *
     * @return zimbraDataSourceConnectionType, or "cleartext" if unset
     */
    @ZAttr(id=425)
    public String getDataSourceConnectionTypeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraDataSourceConnectionType, "cleartext", true);
    }

  /**
     * stop words for lucene text analyzer. This setting takes effect only
     * for default analyzer. This setting affects only accounts that do not
     * have custom text analyzers. See zimbraTextAnalyzer for information on
     * custom text analyzers.
     *
     * @param zimbraDefaultAnalyzerStopWords new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1587)
    public void setDefaultAnalyzerStopWords(String[] zimbraDefaultAnalyzerStopWords) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraDefaultAnalyzerStopWords, zimbraDefaultAnalyzerStopWords);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * name of the default domain for accounts when authenticating without a
     * domain
     *
     * @return zimbraDefaultDomainName, or null if unset
     */
    @ZAttr(id=172)
    public String getDefaultDomainName() {
        return getAttr(ZAttrProvisioning.A_zimbraDefaultDomainName, null, true);
    }

    /**
     * name of the default domain for accounts when authenticating without a
     * domain
     *
     * @param zimbraDefaultDomainName new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=172)
    public void setDefaultDomainName(String zimbraDefaultDomainName) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraDefaultDomainName, zimbraDefaultDomainName);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * URL of ephemeral storage backend
     *
     * @return zimbraEphemeralBackendURL, or "ldap://default" if unset
     *
     * @since ZCS 8.7.6
     */
    @ZAttr(id=2995)
    public String getEphemeralBackendURL() {
        return getAttr(ZAttrProvisioning.A_zimbraEphemeralBackendURL, "ldap://default", true);
    }

  /**
     * URL to Exchange server for free/busy lookup and propagation
     *
     * @return zimbraFreebusyExchangeURL, or null if unset
     *
     * @since ZCS 5.0.3
     */
    @ZAttr(id=607)
    public String getFreebusyExchangeURL() {
        return getAttr(ZAttrProvisioning.A_zimbraFreebusyExchangeURL, null, true);
    }

  /**
     * URLs of external Zimbra servers for free/busy lookup in the form of
     * http[s]://[user:pass@]host:port
     *
     * @return zimbraFreebusyExternalZimbraURL, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1253)
    public String[] getFreebusyExternalZimbraURL() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraFreebusyExternalZimbraURL, true, true);
    }

  /**
     * how often the zimbraLastLogonTimestamp is updated. if set to 0,
     * updating zimbraLastLogonTimestamp is completely disabled . Must be in
     * valid duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getLastLogonTimestampFrequencyAsString to access value as a string.
     *
     * @see #getLastLogonTimestampFrequencyAsString()
     *
     * @return zimbraLastLogonTimestampFrequency in millseconds, or 86400000 (1d)  if unset
     */
    @ZAttr(id=114)
    public long getLastLogonTimestampFrequency() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraLastLogonTimestampFrequency, 86400000L, true);
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
     * System purge policy, encoded as metadata. Users can apply these policy
     * elements to their folders and tags. If the system policy changes, user
     * settings are automatically updated with the change.
     *
     * @return zimbraMailPurgeSystemPolicy, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1239)
    public String getMailPurgeSystemPolicy() {
        return getAttr(ZAttrProvisioning.A_zimbraMailPurgeSystemPolicy, null, true);
    }

    /**
     * System purge policy, encoded as metadata. Users can apply these policy
     * elements to their folders and tags. If the system policy changes, user
     * settings are automatically updated with the change.
     *
     * @param zimbraMailPurgeSystemPolicy new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1239)
    public void setMailPurgeSystemPolicy(String zimbraMailPurgeSystemPolicy) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailPurgeSystemPolicy, zimbraMailPurgeSystemPolicy);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether to enable LDAP-filter in zimbraMailSSLClientCertPrincipalMap
     *
     * @return zimbraMailSSLClientCertPrincipalMapLdapFilterEnabled, or false if unset
     *
     * @since ZCS 7.1.2
     */
    @ZAttr(id=1216)
    public boolean isMailSSLClientCertPrincipalMapLdapFilterEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailSSLClientCertPrincipalMapLdapFilterEnabled, false, true);
    }

  /**
     * Number of Message-Id header values to keep in the LMTP dedupe cache.
     * Subsequent attempts to deliver a message with a matching Message-Id to
     * the same mailbox will be ignored. A value of 0 disables deduping.
     *
     * @return zimbraMessageIdDedupeCacheSize, or 3000 if unset
     */
    @ZAttr(id=334)
    public int getMessageIdDedupeCacheSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMessageIdDedupeCacheSize, 3000, true);
    }

  /**
     * Timeout for a Message-Id entry in the LMTP dedupe cache. A value of 0
     * indicates no timeout. zimbraMessageIdDedupeCacheSize limit is ignored
     * when this is set to a non-zero value. . Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * <p>Use getMessageIdDedupeCacheTimeoutAsString to access value as a string.
     *
     * @see #getMessageIdDedupeCacheTimeoutAsString()
     *
     * @return zimbraMessageIdDedupeCacheTimeout in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 7.1.4
     */
    @ZAttr(id=1340)
    public long getMessageIdDedupeCacheTimeout() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraMessageIdDedupeCacheTimeout, 0L, true);
    }

  /**
     * Maximum total size of a mail message. Enforced in mailbox server and
     * also used as value for postconf message_size_limit. 0 means &quot;no
     * limit&quot;
     *
     * @return zimbraMtaMaxMessageSize, or 10240000 if unset
     */
    @ZAttr(id=198)
    public long getMtaMaxMessageSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraMtaMaxMessageSize, 10240000L, true);
    }

  /**
     * URL of the previous ephemeral storage backend
     *
     * @return zimbraPreviousEphemeralBackendURL, or null if unset
     *
     * @since ZCS 8.8.1
     */
    @ZAttr(id=3018)
    public String getPreviousEphemeralBackendURL() {
        return getAttr(ZAttrProvisioning.A_zimbraPreviousEphemeralBackendURL, null, true);
    }

    /**
     * URL of the previous ephemeral storage backend
     *
     * @param zimbraPreviousEphemeralBackendURL new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.1
     */
    @ZAttr(id=3018)
    public void setPreviousEphemeralBackendURL(String zimbraPreviousEphemeralBackendURL) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPreviousEphemeralBackendURL, zimbraPreviousEphemeralBackendURL);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * The total size (in bytes) of the in-memory queue of conversations to
     * be purged for each data source
     *
     * @return zimbraPurgedConversationsQueueSize, or 1000000 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2017)
    public long getPurgedConversationsQueueSize() {
        return getLongAttr(ZAttrProvisioning.A_zimbraPurgedConversationsQueueSize, 1000000L, true);
    }

  /**
     * This attribute is used to limit the amount of computation allowed when
     * matching regex expressions. For example as part of the IMAP LIST
     * command. Set to a higher value if legitimate IMAP list commands fail
     * throwing TooManyAccessesToMatchTargetException.
     *
     * @return zimbraRegexMaxAccessesWhenMatching, or 1000000 if unset
     *
     * @since ZCS 8.0.8,8.5.0
     */
    @ZAttr(id=1643)
    public int getRegexMaxAccessesWhenMatching() {
        return getIntAttr(ZAttrProvisioning.A_zimbraRegexMaxAccessesWhenMatching, 1000000, true);
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
     * The initial retry delay for the exponential backoff algorithm. Must be
     * in valid duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getScheduledTaskInitialRetryDelayAsString to access value as a string.
     *
     * @see #getScheduledTaskInitialRetryDelayAsString()
     *
     * @return zimbraScheduledTaskInitialRetryDelay in millseconds, or 5000 (5s)  if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2069)
    public long getScheduledTaskInitialRetryDelay() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraScheduledTaskInitialRetryDelay, 5000L, true);
    }

  /**
     * The maximum number of times a scheduled task can be retried upon
     * failure. A value of 0 means no maximum
     *
     * @return zimbraScheduledTaskMaxRetries, or 10 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2068)
    public int getScheduledTaskMaxRetries() {
        return getIntAttr(ZAttrProvisioning.A_zimbraScheduledTaskMaxRetries, 10, true);
    }

  /**
     * The maximum retry delay for the exponential backoff algorithm, or 0
     * for no maximum. Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * <p>Use getScheduledTaskMaxRetryDelayAsString to access value as a string.
     *
     * @see #getScheduledTaskMaxRetryDelayAsString()
     *
     * @return zimbraScheduledTaskMaxRetryDelay in millseconds, or 600000 (10m)  if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2070)
    public long getScheduledTaskMaxRetryDelay() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraScheduledTaskMaxRetryDelay, 600000L, true);
    }

  /**
     * Whether to retry, after a delay, scheduled tasks upon failure
     *
     * @return zimbraScheduledTaskRetry, or true if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2067)
    public boolean isScheduledTaskRetry() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraScheduledTaskRetry, true, true);
    }

  /**
     * The algorithm for determining how long the task scheduler should delay
     * a task attempt upon failure
     *
     * <p>Valid values: [constant, linear, exponential]
     *
     * @return zimbraScheduledTaskRetryPolicy, or "exponential" if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2071)
    public String getScheduledTaskRetryPolicyAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraScheduledTaskRetryPolicy, "exponential", true);
    }

  /**
     * If true, an X-Authenticated-User header will be added to messages sent
     * via SendMsgRequest.
     *
     * @return zimbraSmtpSendAddAuthenticatedUser, or false if unset
     *
     * @since ZCS 5.0.10
     */
    @ZAttr(id=747)
    public boolean isSmtpSendAddAuthenticatedUser() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSmtpSendAddAuthenticatedUser, false, true);
    }

  /**
     * Whether X-Mailer will be added to messages sent by Zimbra
     *
     * @return zimbraSmtpSendAddMailer, or true if unset
     *
     * @since ZCS 5.0.5
     */
    @ZAttr(id=636)
    public boolean isSmtpSendAddMailer() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSmtpSendAddMailer, true, true);
    }

  /**
     * Whether X-Originating-IP will be added to messages sent via
     * SendMsgRequest.
     *
     * @return zimbraSmtpSendAddOriginatingIP, or true if unset
     */
    @ZAttr(id=435)
    public boolean isSmtpSendAddOriginatingIP() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSmtpSendAddOriginatingIP, true, true);
    }

  /**
     * mail header name for flagging spam
     *
     * @return zimbraSpamHeader, or "X-Spam-Flag" if unset
     */
    @ZAttr(id=210)
    public String getSpamHeader() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamHeader, "X-Spam-Flag", true);
    }

  /**
     * regular expression for matching the spam header
     *
     * @return zimbraSpamHeaderValue, or "YES" if unset
     */
    @ZAttr(id=211)
    public String getSpamHeaderValue() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamHeaderValue, "YES", true);
    }

  /**
     * When user classifies a message as not spam forward message via SMTP to
     * this account
     *
     * @return zimbraSpamIsNotSpamAccount, or null if unset
     */
    @ZAttr(id=245)
    public String getSpamIsNotSpamAccount() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamIsNotSpamAccount, null, true);
    }

  /**
     * When user classifies a message as spam forward message via SMTP to
     * this account
     *
     * @return zimbraSpamIsSpamAccount, or null if unset
     */
    @ZAttr(id=244)
    public String getSpamIsSpamAccount() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamIsSpamAccount, null, true);
    }

  /**
     * value for envelope from (MAIL FROM) in spam report
     *
     * @return zimbraSpamReportEnvelopeFrom, or "<>" if unset
     *
     * @since ZCS 6.0.2
     */
    @ZAttr(id=1049)
    public String getSpamReportEnvelopeFrom() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamReportEnvelopeFrom, "<>", true);
    }

  /**
     * mail header name for sender in spam report
     *
     * @return zimbraSpamReportSenderHeader, or "X-Zimbra-Spam-Report-Sender" if unset
     */
    @ZAttr(id=465)
    public String getSpamReportSenderHeader() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamReportSenderHeader, "X-Zimbra-Spam-Report-Sender", true);
    }

  /**
     * spam report type value for ham
     *
     * @return zimbraSpamReportTypeHam, or "ham" if unset
     */
    @ZAttr(id=468)
    public String getSpamReportTypeHam() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamReportTypeHam, "ham", true);
    }

  /**
     * mail header name for report type in spam report
     *
     * @return zimbraSpamReportTypeHeader, or "X-Zimbra-Spam-Report-Type" if unset
     */
    @ZAttr(id=466)
    public String getSpamReportTypeHeader() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamReportTypeHeader, "X-Zimbra-Spam-Report-Type", true);
    }

  /**
     * spam report type value for spam
     *
     * @return zimbraSpamReportTypeSpam, or "spam" if unset
     */
    @ZAttr(id=467)
    public String getSpamReportTypeSpam() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamReportTypeSpam, "spam", true);
    }

  /**
     * Subject prefix for the spam training messages used to sent to the
     * zimbraSpamIsSpamAccount/zimbraSpamIsNotSpamAccount account.
     *
     * @return zimbraSpamTrainingSubjectPrefix, or "spam-report:" if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2020)
    public String getSpamTrainingSubjectPrefix() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamTrainingSubjectPrefix, "spam-report:", true);
    }

  /**
     * Aliases of Trash folder. In case some IMAP clients use different
     * folder names other than Trash, the spam filter still special-cases
     * those folders as if they are Trash.
     *
     * @return zimbraSpamTrashAlias, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1167)
    public String[] getSpamTrashAlias() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraSpamTrashAlias, true, true); return value.length > 0 ? value : new String[] {"/Deleted Messages","/Deleted Items"};
    }

  /**
     * Mail header name for flagging a message as not spam. If set, this
     * takes precedence over zimbraSpamHeader.
     *
     * @return zimbraSpamWhitelistHeader, or null if unset
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1257)
    public String getSpamWhitelistHeader() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamWhitelistHeader, null, true);
    }

    /**
     * Mail header name for flagging a message as not spam. If set, this
     * takes precedence over zimbraSpamHeader.
     *
     * @param zimbraSpamWhitelistHeader new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1257)
    public void setSpamWhitelistHeader(String zimbraSpamWhitelistHeader) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSpamWhitelistHeader, zimbraSpamWhitelistHeader);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * regular expression for matching the value of zimbraSpamWhitelistHeader
     * for flagging a message as not spam
     *
     * @return zimbraSpamWhitelistHeaderValue, or null if unset
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1258)
    public String getSpamWhitelistHeaderValue() {
        return getAttr(ZAttrProvisioning.A_zimbraSpamWhitelistHeaderValue, null, true);
    }

    /**
     * regular expression for matching the value of zimbraSpamWhitelistHeader
     * for flagging a message as not spam
     *
     * @param zimbraSpamWhitelistHeaderValue new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1258)
    public void setSpamWhitelistHeaderValue(String zimbraSpamWhitelistHeaderValue) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSpamWhitelistHeaderValue, zimbraSpamWhitelistHeaderValue);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * spnego auth error URL
     *
     * @return zimbraSpnegoAuthErrorURL, or null if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1124)
    public String getSpnegoAuthErrorURL() {
        return getAttr(ZAttrProvisioning.A_zimbraSpnegoAuthErrorURL, null, true);
    }

  /**
     * spnego auth realm
     *
     * @return zimbraSpnegoAuthRealm, or null if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1119)
    public String getSpnegoAuthRealm() {
        return getAttr(ZAttrProvisioning.A_zimbraSpnegoAuthRealm, null, true);
    }

  /**
     * Deprecated since: 23.8.0. UCService is not in use anymore. Orig desc:
     * Designated UC provider of the system
     *
     * @return zimbraUCProviderEnabled, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1410)
    public String getUCProviderEnabled() {
        return getAttr(ZAttrProvisioning.A_zimbraUCProviderEnabled, null, true);
    }

  ///// END-AUTO-GEN-REPLACE

}
