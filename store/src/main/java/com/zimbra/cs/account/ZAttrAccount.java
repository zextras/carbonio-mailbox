// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import static com.zimbra.common.account.ProvisioningConstants.FALSE;
import static com.zimbra.common.account.ProvisioningConstants.TRUE;

import com.zimbra.common.account.ZAttr;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.ldap.LdapDateUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** AUTO-GENERATED. DO NOT EDIT. */
public abstract class ZAttrAccount extends MailTarget {

  protected ZAttrAccount(
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
     * RFC2798: preferred name to be used when displaying entries
     *
     * @return displayName, or null if unset
     */
    @ZAttr(id=-1)
    public String getDisplayName() {
        return getAttr(ZAttrProvisioning.A_displayName, null, true);
    }

  /**
     * RFC1274: RFC822 Mailbox
     *
     * @return mail, or null if unset
     */
    @ZAttr(id=-1)
    public String getMail() {
        return getAttr(ZAttrProvisioning.A_mail, null, true);
    }

    /**
     * RFC1274: RFC822 Mailbox
     *
     * @param mail new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=-1)
    public void setMail(String mail) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_mail, mail);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * RFC1274: user identifier
     *
     * @return uid, or null if unset
     */
    @ZAttr(id=-1)
    public String getUid() {
        return getAttr(ZAttrProvisioning.A_uid, null, true);
    }

  /**
     * account status. active - active lockout - no login until lockout
     * duration is over, mail delivery OK. locked - no login, mail delivery
     * OK. maintenance - no login, no delivery(lmtp server returns 4.x.x
     * Persistent Transient Failure). pending - no login, no delivery(lmtp
     * server returns 5.x.x Permanent Failure), Account behavior is like
     * closed, except that when the status is being set to pending, account
     * addresses are not removed from distribution lists. The use case is for
     * hosted. New account creation based on invites that are not completed
     * until user accepts TOS on account creation confirmation page. closed -
     * no login, no delivery(lmtp server returns 5.x.x Permanent Failure),
     * all addresses (account main email and all aliases) of the account are
     * removed from all distribution lists.
     *
     * <p>Valid values: [active, maintenance, locked, closed, lockout, pending]
     *
     * @return zimbraAccountStatus, or null if unset and/or has invalid value
     */
    @ZAttr(id=2)
    public ZAttrProvisioning.AccountStatus getAccountStatus() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraAccountStatus, true, true); return v == null ? null : ZAttrProvisioning.AccountStatus.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return null; }
    }

  /**
     * account status. active - active lockout - no login until lockout
     * duration is over, mail delivery OK. locked - no login, mail delivery
     * OK. maintenance - no login, no delivery(lmtp server returns 4.x.x
     * Persistent Transient Failure). pending - no login, no delivery(lmtp
     * server returns 5.x.x Permanent Failure), Account behavior is like
     * closed, except that when the status is being set to pending, account
     * addresses are not removed from distribution lists. The use case is for
     * hosted. New account creation based on invites that are not completed
     * until user accepts TOS on account creation confirmation page. closed -
     * no login, no delivery(lmtp server returns 5.x.x Permanent Failure),
     * all addresses (account main email and all aliases) of the account are
     * removed from all distribution lists.
     *
     * <p>Valid values: [active, maintenance, locked, closed, lockout, pending]
     *
     * @param zimbraAccountStatus new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=2)
    public void setAccountStatus(ZAttrProvisioning.AccountStatus zimbraAccountStatus) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAccountStatus, zimbraAccountStatus.toString());
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script defined by admin (not able to edit and view from the end
     * user) applied after the end user filter rule
     *
     * @param zimbraAdminSieveScriptAfter new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2114)
    public void setAdminSieveScriptAfter(String zimbraAdminSieveScriptAfter) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAdminSieveScriptAfter, zimbraAdminSieveScriptAfter);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script defined by admin (not able to edit and view from the end
     * user) applied after the end user filter rule
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2114)
    public void unsetAdminSieveScriptAfter() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAdminSieveScriptAfter, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script defined by admin (not able to edit and view from the end
     * user) applied before the end user filter rule
     *
     * @return zimbraAdminSieveScriptBefore, or null if unset
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2113)
    public String getAdminSieveScriptBefore() {
        return getAttr(ZAttrProvisioning.A_zimbraAdminSieveScriptBefore, null, true);
    }

    /**
     * sieve script defined by admin (not able to edit and view from the end
     * user) applied before the end user filter rule
     *
     * @param zimbraAdminSieveScriptBefore new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2113)
    public void setAdminSieveScriptBefore(String zimbraAdminSieveScriptBefore) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAdminSieveScriptBefore, zimbraAdminSieveScriptBefore);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script defined by admin (not able to edit and view from the end
     * user) applied before the end user filter rule
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2113)
    public void unsetAdminSieveScriptBefore() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAdminSieveScriptBefore, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether this account can use any from address. Not changeable by
     * domain admin to allow arbitrary addresses
     *
     * @return zimbraAllowAnyFromAddress, or false if unset
     */
    @ZAttr(id=427)
    public boolean isAllowAnyFromAddress() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAllowAnyFromAddress, false, true);
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
     * whether or not to index attachments
     *
     * @return zimbraAttachmentsIndexingEnabled, or true if unset
     */
    @ZAttr(id=173)
    public boolean isAttachmentsIndexingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAttachmentsIndexingEnabled, true, true);
    }

  /**
     * explicit mapping to an external LDAP dn for a given account
     *
     * @return zimbraAuthLdapExternalDn, or null if unset
     */
    @ZAttr(id=256)
    public String getAuthLdapExternalDn() {
        return getAttr(ZAttrProvisioning.A_zimbraAuthLdapExternalDn, null, true);
    }

  /**
     * if set, this value gets stored in the auth token and compared on every
     * request. Changing it will invalidate all outstanding auth tokens. It
     * also gets changed when an account password is changed.
     *
     * @return zimbraAuthTokenValidityValue, or -1 if unset
     *
     * @since ZCS 6.0.0_GA
     */
    @ZAttr(id=1044)
    public int getAuthTokenValidityValue() {
        return getIntAttr(ZAttrProvisioning.A_zimbraAuthTokenValidityValue, -1, true);
    }

  /**
     * if set, this value gets stored in the auth token and compared on every
     * request. Changing it will invalidate all outstanding auth tokens. It
     * also gets changed when an account password is changed.
     *
     * @param zimbraAuthTokenValidityValue new value
     * @param attrs existing map to populate, or null to create a new map
     * @return populated map to pass into Provisioning.modifyAttrs
     *
     * @since ZCS 6.0.0_GA
     */
    @ZAttr(id=1044)
    public Map<String,Object> setAuthTokenValidityValue(int zimbraAuthTokenValidityValue, Map<String,Object> attrs) {
        if (attrs == null) attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAuthTokenValidityValue, Integer.toString(zimbraAuthTokenValidityValue));
        return attrs;
    }

  /**
     * list of currently active auth tokens
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error on accessing ephemeral data
     *
     * @return zimbraAuthTokens, or empty array if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1585)
    public String getAuthTokens(String dynamicComponent) throws com.zimbra.common.service.ServiceException {
        return getEphemeralAttr(ZAttrProvisioning.A_zimbraAuthTokens, dynamicComponent).getValue(null);
    }

    /**
     * list of currently active auth tokens
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @param zimbraAuthTokens new to add to existing values
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1585)
    public void addAuthTokens(String dynamicComponent, String zimbraAuthTokens, com.zimbra.cs.ephemeral.EphemeralInput.Expiration expiration) throws com.zimbra.common.service.ServiceException {
        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraAuthTokens, dynamicComponent, zimbraAuthTokens, true, expiration);
    }

    /**
     * list of currently active auth tokens
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @param zimbraAuthTokens existing value to remove
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1585)
    public void removeAuthTokens(String dynamicComponent, String zimbraAuthTokens) throws com.zimbra.common.service.ServiceException {
        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraAuthTokens, dynamicComponent, zimbraAuthTokens);
    }

    /**
     * list of currently active auth tokens
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1585)
    public boolean hasAuthTokens(String dynamicComponent) throws com.zimbra.common.service.ServiceException {
        return hasEphemeralAttr(ZAttrProvisioning.A_zimbraAuthTokens, dynamicComponent);
    }

    /**
     * list of currently active auth tokens
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1585)
    public void purgeAuthTokens() throws com.zimbra.common.service.ServiceException {
        purgeEphemeralAttr(ZAttrProvisioning.A_zimbraAuthTokens);
    }

    /**
     * Whether account should act as a service account to provide free busy
     * information to exchange servers for users in the domain.
     *
     * @return zimbraAvailabilityServiceProvider, or false if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2072)
    public boolean isAvailabilityServiceProvider() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAvailabilityServiceProvider, false, true);
    }

  /**
     * Batch size to use when indexing data
     *
     * @return zimbraBatchedIndexingSize, or 20 if unset
     *
     * @since ZCS 5.0.3
     */
    @ZAttr(id=619)
    public int getBatchedIndexingSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraBatchedIndexingSize, 20, true);
    }

  /**
     * COS zimbraID
     *
     * @return zimbraCOSId, or null if unset
     */
    @ZAttr(id=14)
    public String getCOSId() {
        return getAttr(ZAttrProvisioning.A_zimbraCOSId, null, true);
    }

    /**
     * COS zimbraID
     *
     * @param zimbraCOSId new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=14)
    public void setCOSId(String zimbraCOSId) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraCOSId, zimbraCOSId);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether to retain exception instances when the recurrence series is
     * changed to new time; set to FALSE for Exchange compatibility
     *
     * @return zimbraCalendarKeepExceptionsOnSeriesTimeChange, or false if unset
     *
     * @since ZCS 7.1.2
     */
    @ZAttr(id=1240)
    public boolean isCalendarKeepExceptionsOnSeriesTimeChange() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraCalendarKeepExceptionsOnSeriesTimeChange, false, true);
    }

  /**
     * email address identifying the default device for receiving reminders
     * for appointments and tasks
     *
     * @return zimbraCalendarReminderDeviceEmail, or null if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1140)
    public String getCalendarReminderDeviceEmail() {
        return getAttr(ZAttrProvisioning.A_zimbraCalendarReminderDeviceEmail, null, true);
    }

  /**
     * Deprecated since: 8.5.0. family mailbox feature is deprecated. Orig
     * desc: zimbraId of child accounts
     *
     * @return zimbraChildAccount, or empty array if unset
     */
    @ZAttr(id=449)
    public String[] getChildAccount() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraChildAccount, true, true);
    }

  /**
     * maximum number of contact entries to return from an auto complete
     *
     * @return zimbraContactAutoCompleteMaxResults, or 20 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=827)
    public int getContactAutoCompleteMaxResults() {
        return getIntAttr(ZAttrProvisioning.A_zimbraContactAutoCompleteMaxResults, 20, true);
    }

  /**
     * Comma separates list of attributes in contact object to search for
     * email addresses when generating auto-complete contact list. The same
     * set of fields are used for GAL contacts as well because LDAP
     * attributes for GAL objects are mapped to Contact compatible attributes
     * via zimbraGalLdapAttrMap.
     *
     * @return zimbraContactEmailFields, or "email,email2,email3,email4,email5,email6,email7,email8,email9,email10,workEmail1,workEmail2,workEmail3" if unset
     *
     * @since ZCS 6.0.7
     */
    @ZAttr(id=1088)
    public String getContactEmailFields() {
        return getAttr(ZAttrProvisioning.A_zimbraContactEmailFields, "email,email2,email3,email4,email5,email6,email7,email8,email9,email10,workEmail1,workEmail2,workEmail3", true);
    }

  /**
     * Maximum number of contacts allowed in mailbox. 0 means no limit.
     *
     * @return zimbraContactMaxNumEntries, or 10000 if unset
     */
    @ZAttr(id=107)
    public int getContactMaxNumEntries() {
        return getIntAttr(ZAttrProvisioning.A_zimbraContactMaxNumEntries, 10000, true);
    }

  /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: List of data associated with CSRF token for an account. The
     * data format is CSRF token data:Auth token Key crumb:Auth Token Key
     * expiration
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error on accessing ephemeral data
     *
     * @return zimbraCsrfTokenData, or empty array if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1629)
    public String getCsrfTokenData(String dynamicComponent) throws com.zimbra.common.service.ServiceException {
        return getEphemeralAttr(ZAttrProvisioning.A_zimbraCsrfTokenData, dynamicComponent).getValue(null);
    }

    /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: List of data associated with CSRF token for an account. The
     * data format is CSRF token data:Auth token Key crumb:Auth Token Key
     * expiration
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @param zimbraCsrfTokenData new to add to existing values
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1629)
    public void addCsrfTokenData(String dynamicComponent, String zimbraCsrfTokenData, com.zimbra.cs.ephemeral.EphemeralInput.Expiration expiration) throws com.zimbra.common.service.ServiceException {
        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraCsrfTokenData, dynamicComponent, zimbraCsrfTokenData, true, expiration);
    }

    /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: List of data associated with CSRF token for an account. The
     * data format is CSRF token data:Auth token Key crumb:Auth Token Key
     * expiration
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @param zimbraCsrfTokenData existing value to remove
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1629)
    public void removeCsrfTokenData(String dynamicComponent, String zimbraCsrfTokenData) throws com.zimbra.common.service.ServiceException {
        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraCsrfTokenData, dynamicComponent, zimbraCsrfTokenData);
    }

    /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: List of data associated with CSRF token for an account. The
     * data format is CSRF token data:Auth token Key crumb:Auth Token Key
     * expiration
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1629)
    public boolean hasCsrfTokenData(String dynamicComponent) throws com.zimbra.common.service.ServiceException {
        return hasEphemeralAttr(ZAttrProvisioning.A_zimbraCsrfTokenData, dynamicComponent);
    }

    /**
     * Deprecated since: 23.11.0. deprecated as not being used in Carbonio.
     * Orig desc: List of data associated with CSRF token for an account. The
     * data format is CSRF token data:Auth token Key crumb:Auth Token Key
     * expiration
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1629)
    public void purgeCsrfTokenData() throws com.zimbra.common.service.ServiceException {
        purgeEphemeralAttr(ZAttrProvisioning.A_zimbraCsrfTokenData);
    }

  /**
     * The time interval between automated data imports for a Caldav data
     * source. If unset or 0, the data source will not be scheduled for
     * automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourceCaldavPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceCaldavPollingIntervalAsString()
     *
     * @return zimbraDataSourceCaldavPollingInterval in millseconds, or -1 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=788)
    public long getDataSourceCaldavPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceCaldavPollingInterval, -1L, true);
    }

  /**
     * The time interval between automated data imports for a remote calendar
     * data source. If explicitly set to 0, the data source will not be
     * scheduled for automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourceCalendarPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceCalendarPollingIntervalAsString()
     *
     * @return zimbraDataSourceCalendarPollingInterval in millseconds, or 43200000 (12h)  if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=819)
    public long getDataSourceCalendarPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceCalendarPollingInterval, 43200000L, true);
    }

  /**
     * The time interval between automated data imports for a GAL data
     * source. If unset or 0, the data source will not be scheduled for
     * automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourceGalPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceGalPollingIntervalAsString()
     *
     * @return zimbraDataSourceGalPollingInterval in millseconds, or -1 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=826)
    public long getDataSourceGalPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceGalPollingInterval, -1L, true);
    }

  /**
     * The time interval between automated data imports for an Imap data
     * source. If unset or 0, the data source will not be scheduled for
     * automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourceImapPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceImapPollingIntervalAsString()
     *
     * @return zimbraDataSourceImapPollingInterval in millseconds, or -1 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=768)
    public long getDataSourceImapPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceImapPollingInterval, -1L, true);
    }

  /**
     * Shortest allowed duration for zimbraDataSourcePollingInterval.. Must
     * be in valid duration format: {digits}{time-unit}. digits: 0-9,
     * time-unit: [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days,
     * ms - milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getDataSourceMinPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceMinPollingIntervalAsString()
     *
     * @return zimbraDataSourceMinPollingInterval in millseconds, or 60000 (1m)  if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=525)
    public long getDataSourceMinPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceMinPollingInterval, 60000L, true);
    }

  /**
     * The time interval between automated data imports for a Pop3 data
     * source. If unset or 0, the data source will not be scheduled for
     * automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourcePop3PollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourcePop3PollingIntervalAsString()
     *
     * @return zimbraDataSourcePop3PollingInterval in millseconds, or -1 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=767)
    public long getDataSourcePop3PollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourcePop3PollingInterval, -1L, true);
    }

  /**
     * Quota allotted to each data source
     *
     * @return zimbraDataSourceQuota, or 0 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2015)
    public long getDataSourceQuota() {
        return getLongAttr(ZAttrProvisioning.A_zimbraDataSourceQuota, 0L, true);
    }

  /**
     * The time interval between automated data imports for a Rss data
     * source. If explicitly set to 0, the data source will not be scheduled
     * for automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourceRssPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceRssPollingIntervalAsString()
     *
     * @return zimbraDataSourceRssPollingInterval in millseconds, or 43200000 (12h)  if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=770)
    public long getDataSourceRssPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceRssPollingInterval, 43200000L, true);
    }

  /**
     * Quota allotted to all data sources
     *
     * @return zimbraDataSourceTotalQuota, or 0 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2016)
    public long getDataSourceTotalQuota() {
        return getLongAttr(ZAttrProvisioning.A_zimbraDataSourceTotalQuota, 0L, true);
    }

  /**
     * The time interval between automated data imports for a Yahoo address
     * book data source. If unset or 0, the data source will not be scheduled
     * for automated polling. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getDataSourceYabPollingIntervalAsString to access value as a string.
     *
     * @see #getDataSourceYabPollingIntervalAsString()
     *
     * @return zimbraDataSourceYabPollingInterval in millseconds, or -1 if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=789)
    public long getDataSourceYabPollingInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDataSourceYabPollingInterval, -1L, true);
    }

  /**
     * Default flags on folder. These are set when a new folder is created,
     * has no effect on existing folders. Possible values are: * -
     * \Subscribed b - \ExcludeFB # - \Checked i - \NoInherit y - \SyncFolder
     * ~ - \Sync o - \Noinferiors g - \Global
     *
     * @return zimbraDefaultFolderFlags, or null if unset
     *
     * @since ZCS 7.1.1
     */
    @ZAttr(id=1210)
    public String getDefaultFolderFlags() {
        return getAttr(ZAttrProvisioning.A_zimbraDefaultFolderFlags, null, true);
    }

    /**
     * Default flags on folder. These are set when a new folder is created,
     * has no effect on existing folders. Possible values are: * -
     * \Subscribed b - \ExcludeFB # - \Checked i - \NoInherit y - \SyncFolder
     * ~ - \Sync o - \Noinferiors g - \Global
     *
     * @param zimbraDefaultFolderFlags new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.1.1
     */
    @ZAttr(id=1210)
    public void setDefaultFolderFlags(String zimbraDefaultFolderFlags) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraDefaultFolderFlags, zimbraDefaultFolderFlags);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether conversations are allowed to span multiple accounts. Every
     * time this attribute is changed, new messages that continue existing
     * conversation threads in imported accounts will no longer thread with
     * those conversations, and will instead start new ones.
     *
     * @return zimbraDisableCrossAccountConversationThreading, or true if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2048)
    public boolean isDisableCrossAccountConversationThreading() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraDisableCrossAccountConversationThreading, true, true);
    }

  /**
     * enable/disable dumpster
     *
     * @return zimbraDumpsterEnabled, or false if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1128)
    public boolean isDumpsterEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraDumpsterEnabled, false, true);
    }

    /**
     * enable/disable dumpster
     *
     * @param zimbraDumpsterEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1128)
    public void setDumpsterEnabled(boolean zimbraDumpsterEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraDumpsterEnabled, zimbraDumpsterEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * disables purging from dumpster when set to FALSE
     *
     * @return zimbraDumpsterPurgeEnabled, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1315)
    public boolean isDumpsterPurgeEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraDumpsterPurgeEnabled, true, true);
    }

  /**
     * limits how much of a dumpster data is viewable by the end user, based
     * on the age since being put in dumpster. Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * <p>Use getDumpsterUserVisibleAgeAsString to access value as a string.
     *
     * @see #getDumpsterUserVisibleAgeAsString()
     *
     * @return zimbraDumpsterUserVisibleAge in millseconds, or 2592000000 (30d)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1314)
    public long getDumpsterUserVisibleAge() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraDumpsterUserVisibleAge, 2592000000L, true);
    }

  /**
     * Time when external virtual account was last automatically disabled by
     * the system. Applicable only when zimbraIsExternalVirtualAccount on the
     * account is set to TRUE.
     *
     * <p>Use getExternalAccountDisabledTimeAsString to access value as a string.
     *
     * @see #getExternalAccountDisabledTimeAsString()
     *
     * @return zimbraExternalAccountDisabledTime as Date, null if unset or unable to parse
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1371)
    public Date getExternalAccountDisabledTime() {
        return getGeneralizedTimeAttr(ZAttrProvisioning.A_zimbraExternalAccountDisabledTime, null, true);
    }

  /**
     * Time when external virtual account was last automatically disabled by
     * the system. Applicable only when zimbraIsExternalVirtualAccount on the
     * account is set to TRUE.
     *
     * @param zimbraExternalAccountDisabledTime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1371)
    public void setExternalAccountDisabledTime(Date zimbraExternalAccountDisabledTime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraExternalAccountDisabledTime, zimbraExternalAccountDisabledTime==null ? "" : LdapDateUtil.toGeneralizedTime(zimbraExternalAccountDisabledTime));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Duration after the last time the external virtual account was
     * automatically disabled by the system after which the external virtual
     * account would be automatically deleted. Value of 0 indicates that the
     * external virtual account should never be automatically deleted.
     * Applicable only when zimbraIsExternalVirtualAccount on the account is
     * set to TRUE. . Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * <p>Use getExternalAccountLifetimeAfterDisabledAsString to access value as a string.
     *
     * @see #getExternalAccountLifetimeAfterDisabledAsString()
     *
     * @return zimbraExternalAccountLifetimeAfterDisabled in millseconds, or 2592000000 (30d)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1372)
    public long getExternalAccountLifetimeAfterDisabled() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraExternalAccountLifetimeAfterDisabled, 2592000000L, true);
    }

  /**
     * whether checking against zimbraExternalShareWhitelistDomain for
     * external user sharing is enabled
     *
     * @return zimbraExternalShareDomainWhitelistEnabled, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1264)
    public boolean isExternalShareDomainWhitelistEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraExternalShareDomainWhitelistEnabled, false, true);
    }

  /**
     * Maximum allowed lifetime of shares to external users. A value of 0
     * indicates that there&#039;s no limit on an external share&#039;s
     * lifetime. . Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * <p>Use getExternalShareLifetimeAsString to access value as a string.
     *
     * @see #getExternalShareLifetimeAsString()
     *
     * @return zimbraExternalShareLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1260)
    public long getExternalShareLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraExternalShareLifetime, 0L, true);
    }

  /**
     * list of external domains that users can share files and folders with
     *
     * @return zimbraExternalShareWhitelistDomain, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1263)
    public String[] getExternalShareWhitelistDomain() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraExternalShareWhitelistDomain, true, true);
    }

  /**
     * switch for turning external user sharing on/off
     *
     * @return zimbraExternalSharingEnabled, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1261)
    public boolean isExternalSharingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraExternalSharingEnabled, true, true);
    }

    /**
     * switch for turning external user sharing on/off
     *
     * @param zimbraExternalSharingEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1261)
    public void setExternalSharingEnabled(boolean zimbraExternalSharingEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraExternalSharingEnabled, zimbraExternalSharingEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * External email address of an external user. Applicable only when
     * zimbraIsExternalVirtualAccount is set to TRUE.
     *
     * @return zimbraExternalUserMailAddress, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1244)
    public String getExternalUserMailAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraExternalUserMailAddress, null, true);
    }

  /**
     * RFC822 email address under verification for an account
     *
     * @return zimbraFeatureAddressUnderVerification, or null if unset
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2128)
    public String getFeatureAddressUnderVerification() {
        return getAttr(ZAttrProvisioning.A_zimbraFeatureAddressUnderVerification, null, true);
    }

    /**
     * RFC822 email address under verification for an account
     *
     * @param zimbraFeatureAddressUnderVerification new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2128)
    public void setFeatureAddressUnderVerification(String zimbraFeatureAddressUnderVerification) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureAddressUnderVerification, zimbraFeatureAddressUnderVerification);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * RFC822 email address under verification for an account
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2128)
    public void unsetFeatureAddressUnderVerification() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureAddressUnderVerification, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Enable end-user email address verification
     *
     * @param zimbraFeatureAddressVerificationEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2126)
    public void setFeatureAddressVerificationEnabled(boolean zimbraFeatureAddressVerificationEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureAddressVerificationEnabled, zimbraFeatureAddressVerificationEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Expiry time for end-user email address verification. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getFeatureAddressVerificationExpiryAsString to access value as a string.
     *
     * @see #getFeatureAddressVerificationExpiryAsString()
     *
     * @return zimbraFeatureAddressVerificationExpiry in millseconds, or 86400000 (1d)  if unset
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2127)
    public long getFeatureAddressVerificationExpiry() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFeatureAddressVerificationExpiry, 86400000L, true);
    }

  /**
     * End-user email address verification status
     *
     * <p>Valid values: [verified, pending, failed, expired]
     *
     * @return zimbraFeatureAddressVerificationStatus, or null if unset and/or has invalid value
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2129)
    public ZAttrProvisioning.FeatureAddressVerificationStatus getFeatureAddressVerificationStatus() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraFeatureAddressVerificationStatus, true, true); return v == null ? null : ZAttrProvisioning.FeatureAddressVerificationStatus.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return null; }
    }

  /**
     * End-user email address verification status
     *
     * <p>Valid values: [verified, pending, failed, expired]
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2129)
    public void unsetFeatureAddressVerificationStatus() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureAddressVerificationStatus, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether or not to enable rerouting spam messages to Junk folder in
     * ZCS, exposing Junk folder and actions in the web UI, and exposing Junk
     * folder to IMAP clients.
     *
     * @return zimbraFeatureAntispamEnabled, or true if unset
     *
     * @since ZCS 7.1.0
     */
    @ZAttr(id=1168)
    public boolean isFeatureAntispamEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureAntispamEnabled, true, true);
    }

    /**
     * whether or not to enable rerouting spam messages to Junk folder in
     * ZCS, exposing Junk folder and actions in the web UI, and exposing Junk
     * folder to IMAP clients.
     *
     * @param zimbraFeatureAntispamEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.1.0
     */
    @ZAttr(id=1168)
    public void setFeatureAntispamEnabled(boolean zimbraFeatureAntispamEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureAntispamEnabled, zimbraFeatureAntispamEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * calendar features
     *
     * @return zimbraFeatureCalendarEnabled, or true if unset
     */
    @ZAttr(id=136)
    public boolean isFeatureCalendarEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureCalendarEnabled, true, true);
    }

  /**
     * Enable contact backup feature
     *
     * @return zimbraFeatureContactBackupEnabled, or false if unset
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2131)
    public boolean isFeatureContactBackupEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureContactBackupEnabled, false, true);
    }

  /**
     * Whether data source purging is enabled
     *
     * @return zimbraFeatureDataSourcePurgingEnabled, or false if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2014)
    public boolean isFeatureDataSourcePurgingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureDataSourcePurgingEnabled, false, true);
    }

  /**
     * enable auto-completion from the GAL, zimbraFeatureGalEnabled also has
     * to be enabled for the auto-completion feature
     *
     * @return zimbraFeatureGalAutoCompleteEnabled, or true if unset
     */
    @ZAttr(id=359)
    public boolean isFeatureGalAutoCompleteEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureGalAutoCompleteEnabled, true, true);
    }

  /**
     * whether GAL features are enabled
     *
     * @return zimbraFeatureGalEnabled, or true if unset
     */
    @ZAttr(id=149)
    public boolean isFeatureGalEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureGalEnabled, true, true);
    }

  /**
     * enable end-user mail forwarding features
     *
     * @return zimbraFeatureMailForwardingEnabled, or true if unset
     */
    @ZAttr(id=342)
    public boolean isFeatureMailForwardingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureMailForwardingEnabled, true, true);
    }

    /**
     * enable end-user mail forwarding features
     *
     * @param zimbraFeatureMailForwardingEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=342)
    public void setFeatureMailForwardingEnabled(boolean zimbraFeatureMailForwardingEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureMailForwardingEnabled, zimbraFeatureMailForwardingEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Mark messages sent to a forwarding address as read
     *
     * @return zimbraFeatureMarkMailForwardedAsRead, or false if unset
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2123)
    public boolean isFeatureMarkMailForwardedAsRead() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureMarkMailForwardedAsRead, false, true);
    }

  /**
     * status of password reset feature
     *
     * <p>Valid values: [enabled, suspended, disabled]
     *
     * @return zimbraFeatureResetPasswordStatus, or ZAttrProvisioning.FeatureResetPasswordStatus.disabled if unset and/or has invalid value
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2134)
    public ZAttrProvisioning.FeatureResetPasswordStatus getFeatureResetPasswordStatus() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraFeatureResetPasswordStatus, true, true); return v == null ? ZAttrProvisioning.FeatureResetPasswordStatus.disabled : ZAttrProvisioning.FeatureResetPasswordStatus.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.FeatureResetPasswordStatus.disabled; }
    }

  /**
     * status of password reset feature
     *
     * <p>Valid values: [enabled, suspended, disabled]
     *
     * @param zimbraFeatureResetPasswordStatus new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2134)
    public void setFeatureResetPasswordStatus(ZAttrProvisioning.FeatureResetPasswordStatus zimbraFeatureResetPasswordStatus) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureResetPasswordStatus, zimbraFeatureResetPasswordStatus.toString());
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * time for which reset password feature is suspended. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getFeatureResetPasswordSuspensionTimeAsString to access value as a string.
     *
     * @see #getFeatureResetPasswordSuspensionTimeAsString()
     *
     * @return zimbraFeatureResetPasswordSuspensionTime in millseconds, or 86400000 (1d)  if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2142)
    public long getFeatureResetPasswordSuspensionTime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFeatureResetPasswordSuspensionTime, 86400000L, true);
    }

  /**
     * time for which reset password feature is suspended. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * @param zimbraFeatureResetPasswordSuspensionTime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2142)
    public void setFeatureResetPasswordSuspensionTime(String zimbraFeatureResetPasswordSuspensionTime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraFeatureResetPasswordSuspensionTime, zimbraFeatureResetPasswordSuspensionTime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether S/MIME feature is enabled. Note: SMIME is a Network feature,
     * this attribute is effective only if SMIME is permitted by license.
     *
     * @return zimbraFeatureSMIMEEnabled, or false if unset
     *
     * @since ZCS 7.1.0
     */
    @ZAttr(id=1186)
    public boolean isFeatureSMIMEEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFeatureSMIMEEnabled, false, true);
    }

  /**
     * Maximum allowed lifetime of file shares to external users. A value of
     * 0 indicates that there&#039;s no limit on an external file
     * share&#039;s lifetime. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getFileExternalShareLifetimeAsString to access value as a string.
     *
     * @see #getFileExternalShareLifetimeAsString()
     *
     * @return zimbraFileExternalShareLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1363)
    public long getFileExternalShareLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFileExternalShareLifetime, 0L, true);
    }

  /**
     * Maximum allowed lifetime of public file shares. A value of 0 indicates
     * that there&#039;s no limit on a public file share&#039;s lifetime. .
     * Must be in valid duration format: {digits}{time-unit}. digits: 0-9,
     * time-unit: [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days,
     * ms - milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getFilePublicShareLifetimeAsString to access value as a string.
     *
     * @see #getFilePublicShareLifetimeAsString()
     *
     * @return zimbraFilePublicShareLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1364)
    public long getFilePublicShareLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFilePublicShareLifetime, 0L, true);
    }

  /**
     * Maximum allowed lifetime of file shares to internal users or groups. A
     * value of 0 indicates that there&#039;s no limit on an internal file
     * share&#039;s lifetime. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getFileShareLifetimeAsString to access value as a string.
     *
     * @see #getFileShareLifetimeAsString()
     *
     * @return zimbraFileShareLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1362)
    public long getFileShareLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFileShareLifetime, 0L, true);
    }

  /**
     * how long a file version is kept around. Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * <p>Use getFileVersionLifetimeAsString to access value as a string.
     *
     * @see #getFileVersionLifetimeAsString()
     *
     * @return zimbraFileVersionLifetime in millseconds, or -1 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1325)
    public long getFileVersionLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFileVersionLifetime, -1L, true);
    }

    /**
     * how long a file version is kept around. Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * @return zimbraFileVersionLifetime, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1325)
    public String getFileVersionLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraFileVersionLifetime, null, true);
    }

  /**
     * whether file versioning is enabled
     *
     * @return zimbraFileVersioningEnabled, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1324)
    public boolean isFileVersioningEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraFileVersioningEnabled, false, true);
    }

  /**
     * Maximum number of messages that can be processed in a single
     * ApplyFilterRules operation.
     *
     * @return zimbraFilterBatchSize, or 10000 if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1158)
    public int getFilterBatchSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraFilterBatchSize, 10000, true);
    }

  /**
     * The amount of time to sleep between every two messages during
     * ApplyFilterRules. Increasing this value will even out server load at
     * the expense of slowing down the operation. . Must be in valid duration
     * format: {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h -
     * hours, m - minutes, s - seconds, d - days, ms - milliseconds. If time
     * unit is not specified, the default is s(seconds).
     *
     * <p>Use getFilterSleepIntervalAsString to access value as a string.
     *
     * @see #getFilterSleepIntervalAsString()
     *
     * @return zimbraFilterSleepInterval in millseconds, or 1 (1ms)  if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1159)
    public long getFilterSleepInterval() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraFilterSleepInterval, 1L, true);
    }

  /**
     * Whether to force clear zimbra auth cookies when SOAP session ends
     * (i.e. force logout on browser tab close)
     *
     * @return zimbraForceClearCookies, or false if unset
     *
     * @since ZCS 8.0.4
     */
    @ZAttr(id=1437)
    public boolean isForceClearCookies() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraForceClearCookies, false, true);
    }

  /**
     * mapping to foreign principal identifier
     *
     * @return zimbraForeignPrincipal, or empty array if unset
     */
    @ZAttr(id=295)
    public String[] getForeignPrincipal() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraForeignPrincipal, true, true);
    }

  /**
     * whether to use gal sync account for autocomplete
     *
     * @return zimbraGalSyncAccountBasedAutoCompleteEnabled, or true if unset
     *
     * @since ZCS 6.0.0_BETA2
     */
    @ZAttr(id=1027)
    public boolean isGalSyncAccountBasedAutoCompleteEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraGalSyncAccountBasedAutoCompleteEnabled, true, true);
    }

  /**
     * seniority index of the group or group member which will determine the
     * sorting order in the hierarchical address book
     *
     * @return zimbraHABSeniorityIndex, or -1 if unset
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=3071)
    public int getHABSeniorityIndex() {
        return getIntAttr(ZAttrProvisioning.A_zimbraHABSeniorityIndex, -1, true);
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
     * whether IMAP is enabled for an account
     *
     * @return zimbraImapEnabled, or true if unset
     */
    @ZAttr(id=174)
    public boolean isImapEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraImapEnabled, true, true);
    }

    /**
     * whether IMAP is enabled for an account
     *
     * @param zimbraImapEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=174)
    public void setImapEnabled(boolean zimbraImapEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraImapEnabled, zimbraImapEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * additional domains considered as internal w.r.t. recipient
     *
     * @return zimbraInternalSendersDomain, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1319)
    public String[] getInternalSendersDomain() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraInternalSendersDomain, true, true);
    }

    /**
     * additional domains considered as internal w.r.t. recipient
     *
     * @param zimbraInternalSendersDomain new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1319)
    public void setInternalSendersDomain(String[] zimbraInternalSendersDomain) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraInternalSendersDomain, zimbraInternalSendersDomain);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * additional domains considered as internal w.r.t. recipient
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1319)
    public void unsetInternalSendersDomain() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraInternalSendersDomain, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * list of invalid jwt
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @param zimbraInvalidJWTokens new to add to existing values
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.6
     */
    @ZAttr(id=2133)
    public void addInvalidJWTokens(String dynamicComponent, String zimbraInvalidJWTokens, com.zimbra.cs.ephemeral.EphemeralInput.Expiration expiration) throws com.zimbra.common.service.ServiceException {
        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraInvalidJWTokens, dynamicComponent, zimbraInvalidJWTokens, true, expiration);
    }

  /**
     * list of invalid jwt
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.6
     */
    @ZAttr(id=2133)
    public boolean hasInvalidJWTokens(String dynamicComponent) throws com.zimbra.common.service.ServiceException {
        return hasEphemeralAttr(ZAttrProvisioning.A_zimbraInvalidJWTokens, dynamicComponent);
    }

    /**
     * list of invalid jwt
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.6
     */
    @ZAttr(id=2133)
    public void purgeInvalidJWTokens() throws com.zimbra.common.service.ServiceException {
        purgeEphemeralAttr(ZAttrProvisioning.A_zimbraInvalidJWTokens);
    }

    /**
     * set to true for admin accounts
     *
     * @return zimbraIsAdminAccount, or false if unset
     */
    @ZAttr(id=31)
    public boolean isIsAdminAccount() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsAdminAccount, false, true);
    }

    /**
     * set to true for admin accounts
     *
     * @param zimbraIsAdminAccount new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=31)
    public void setIsAdminAccount(boolean zimbraIsAdminAccount) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraIsAdminAccount, zimbraIsAdminAccount ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * set to true for delegated admin accounts
     *
     * @return zimbraIsDelegatedAdminAccount, or false if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=852)
    public boolean isIsDelegatedAdminAccount() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, false, true);
    }

  /**
     * whether it is an external user account
     *
     * @return zimbraIsExternalVirtualAccount, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1243)
    public boolean isIsExternalVirtualAccount() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsExternalVirtualAccount, false, true);
    }

  /**
     * Indicates the account is an account used by the system such as spam
     * accounts or Notebook accounts. System accounts cannot be deleted in
     * admin console.
     *
     * @return zimbraIsSystemAccount, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1214)
    public boolean isIsSystemAccount() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsSystemAccount, false, true);
    }

  /**
     * Indicates the account is a resource used by the system. System
     * resource accounts are not counted against license quota.
     *
     * @return zimbraIsSystemResource, or false if unset
     */
    @ZAttr(id=376)
    public boolean isIsSystemResource() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraIsSystemResource, false, true);
    }

  /**
     * Whether to index junk messages
     *
     * @return zimbraJunkMessagesIndexingEnabled, or true if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=579)
    public boolean isJunkMessagesIndexingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraJunkMessagesIndexingEnabled, true, true);
    }

    /**
     * Whether to index junk messages
     *
     * @param zimbraJunkMessagesIndexingEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=579)
    public void setJunkMessagesIndexingEnabled(boolean zimbraJunkMessagesIndexingEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraJunkMessagesIndexingEnabled, zimbraJunkMessagesIndexingEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * rough estimate of when the user last logged in
     *
     * <p>Use getLastLogonTimestampAsString to access value as a string.
     *
     * @see #getLastLogonTimestampAsString()
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error on accessing ephemeral data
     *
     * @return zimbraLastLogonTimestamp as Date, null if unset or unable to parse
     */
    @ZAttr(id=113)
    public Date getLastLogonTimestamp() throws com.zimbra.common.service.ServiceException {
        String v = getEphemeralAttr(ZAttrProvisioning.A_zimbraLastLogonTimestamp, null).getValue(null); return v == null ? null : LdapDateUtil.parseGeneralizedTime(v);
    }

    /**
     * rough estimate of when the user last logged in
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error on accessing ephemeral data
     *
     * @return zimbraLastLogonTimestamp, or null if unset
     */
    @ZAttr(id=113)
    public String getLastLogonTimestampAsString() throws com.zimbra.common.service.ServiceException {
        return getEphemeralAttr(ZAttrProvisioning.A_zimbraLastLogonTimestamp, null).getValue(null);
    }

    /**
     * rough estimate of when the user last logged in
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @param zimbraLastLogonTimestamp new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=113)
    public void setLastLogonTimestamp(Date zimbraLastLogonTimestamp) throws com.zimbra.common.service.ServiceException {
        modifyEphemeralAttr(ZAttrProvisioning.A_zimbraLastLogonTimestamp, null, zimbraLastLogonTimestamp==null ? "" : LdapDateUtil.toGeneralizedTime(zimbraLastLogonTimestamp), false, null);
    }

  /**
     * rough estimate of when the user last logged in
     *
     * Ephemeral attribute - requests routed to EphemeralStore
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=113)
    public void unsetLastLogonTimestamp() throws com.zimbra.common.service.ServiceException {
        deleteEphemeralAttr(ZAttrProvisioning.A_zimbraLastLogonTimestamp);
    }

  /**
     * RFC822 email address of this recipient for accepting mail
     *
     * @return zimbraMailAlias, or empty array if unset
     */
    @ZAttr(id=20)
    public String[] getMailAlias() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraMailAlias, true, true);
    }

    /**
     * RFC822 email address of this recipient for accepting mail
     *
     * @param zimbraMailAlias new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=20)
    public void setMailAlias(String[] zimbraMailAlias) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailAlias, zimbraMailAlias);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * If TRUE, a mailbox that exceeds its quota is still allowed to receive
     * mail, but is not allowed to send.
     *
     * @return zimbraMailAllowReceiveButNotSendWhenOverQuota, or false if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1099)
    public boolean isMailAllowReceiveButNotSendWhenOverQuota() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailAllowReceiveButNotSendWhenOverQuota, false, true);
    }

  /**
     * RFC822 email address of this recipient for local delivery
     *
     * @return zimbraMailDeliveryAddress, or empty array if unset
     */
    @ZAttr(id=13)
    public String[] getMailDeliveryAddress() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraMailDeliveryAddress, true, true);
    }

  /**
     * Retention period of messages in the dumpster. 0 means that all
     * messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getMailDumpsterLifetimeAsString to access value as a string.
     *
     * @see #getMailDumpsterLifetimeAsString()
     *
     * @return zimbraMailDumpsterLifetime in millseconds, or 2592000000 (30d)  if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1133)
    public long getMailDumpsterLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraMailDumpsterLifetime, 2592000000L, true);
    }

    /**
     * Retention period of messages in the dumpster. 0 means that all
     * messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraMailDumpsterLifetime, or "30d" if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1133)
    public String getMailDumpsterLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraMailDumpsterLifetime, "30d", true);
    }

  /**
     * max number of chars in zimbraPrefMailForwardingAddress
     *
     * @return zimbraMailForwardingAddressMaxLength, or 4096 if unset
     *
     * @since ZCS 6.0.0_RC1
     */
    @ZAttr(id=1039)
    public int getMailForwardingAddressMaxLength() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailForwardingAddressMaxLength, 4096, true);
    }

  /**
     * max number of email addresses in zimbraPrefMailForwardingAddress
     *
     * @return zimbraMailForwardingAddressMaxNumAddrs, or 100 if unset
     *
     * @since ZCS 6.0.0_RC1
     */
    @ZAttr(id=1040)
    public int getMailForwardingAddressMaxNumAddrs() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailForwardingAddressMaxNumAddrs, 100, true);
    }

  /**
     * the server hosting the account&#039;s mailbox
     *
     * @return zimbraMailHost, or null if unset
     */
    @ZAttr(id=4)
    public String getMailHost() {
        return getAttr(ZAttrProvisioning.A_zimbraMailHost, null, true);
    }

  /**
     * lifetime of a mail message regardless of location. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getMailMessageLifetimeAsString to access value as a string.
     *
     * @see #getMailMessageLifetimeAsString()
     *
     * @return zimbraMailMessageLifetime in millseconds, or 0 (0)  if unset
     */
    @ZAttr(id=106)
    public long getMailMessageLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraMailMessageLifetime, 0L, true);
    }

    /**
     * lifetime of a mail message regardless of location. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * @return zimbraMailMessageLifetime, or "0" if unset
     */
    @ZAttr(id=106)
    public String getMailMessageLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraMailMessageLifetime, "0", true);
    }

    /**
     * lifetime of a mail message regardless of location. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * @param zimbraMailMessageLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=106)
    public void setMailMessageLifetime(String zimbraMailMessageLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailMessageLifetime, zimbraMailMessageLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script generated from user outgoing filter rules
     *
     * @param zimbraMailOutgoingSieveScript new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1130)
    public void setMailOutgoingSieveScript(String zimbraMailOutgoingSieveScript) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailOutgoingSieveScript, zimbraMailOutgoingSieveScript);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * If TRUE, a message is purged from Spam based on the date that it was
     * moved to the Spam folder. If FALSE, a message is purged from Spam
     * based on the date that it was added to the mailbox.
     *
     * @return zimbraMailPurgeUseChangeDateForSpam, or true if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1117)
    public boolean isMailPurgeUseChangeDateForSpam() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraMailPurgeUseChangeDateForSpam, true, true);
    }

    /**
     * If TRUE, a message is purged from Spam based on the date that it was
     * moved to the Spam folder. If FALSE, a message is purged from Spam
     * based on the date that it was added to the mailbox.
     *
     * @param zimbraMailPurgeUseChangeDateForSpam new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1117)
    public void setMailPurgeUseChangeDateForSpam(boolean zimbraMailPurgeUseChangeDateForSpam) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailPurgeUseChangeDateForSpam, zimbraMailPurgeUseChangeDateForSpam ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * If TRUE, a message is purged from trash based on the date that it was
     * moved to the Trash folder. If FALSE, a message is purged from Trash
     * based on the date that it was added to the mailbox.
     *
     * @param zimbraMailPurgeUseChangeDateForTrash new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.17
     */
    @ZAttr(id=748)
    public void setMailPurgeUseChangeDateForTrash(boolean zimbraMailPurgeUseChangeDateForTrash) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailPurgeUseChangeDateForTrash, zimbraMailPurgeUseChangeDateForTrash ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script generated from user filter rules
     *
     * @return zimbraMailSieveScript, or null if unset
     */
    @ZAttr(id=32)
    public String getMailSieveScript() {
        return getAttr(ZAttrProvisioning.A_zimbraMailSieveScript, null, true);
    }

    /**
     * sieve script generated from user filter rules
     *
     * @param zimbraMailSieveScript new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=32)
    public void setMailSieveScript(String zimbraMailSieveScript) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailSieveScript, zimbraMailSieveScript);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * sieve script generated from user filter rules
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=32)
    public void unsetMailSieveScript() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailSieveScript, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * maximum length of mail signature, 0 means unlimited.
     *
     * @return zimbraMailSignatureMaxLength, or 10240 if unset
     */
    @ZAttr(id=454)
    public long getMailSignatureMaxLength() {
        return getLongAttr(ZAttrProvisioning.A_zimbraMailSignatureMaxLength, 10240L, true);
    }

  /**
     * Retention period of messages in the Junk folder. 0 means that all
     * messages will be retained. This admin-modifiable attribute works in
     * conjunction with zimbraPrefJunkLifetime, which is user-modifiable. The
     * shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getMailSpamLifetimeAsString to access value as a string.
     *
     * @see #getMailSpamLifetimeAsString()
     *
     * @return zimbraMailSpamLifetime in millseconds, or 2592000000 (30d)  if unset
     */
    @ZAttr(id=105)
    public long getMailSpamLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraMailSpamLifetime, 2592000000L, true);
    }

    /**
     * Retention period of messages in the Junk folder. 0 means that all
     * messages will be retained. This admin-modifiable attribute works in
     * conjunction with zimbraPrefJunkLifetime, which is user-modifiable. The
     * shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraMailSpamLifetime, or "30d" if unset
     */
    @ZAttr(id=105)
    public String getMailSpamLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraMailSpamLifetime, "30d", true);
    }

    /**
     * Retention period of messages in the Junk folder. 0 means that all
     * messages will be retained. This admin-modifiable attribute works in
     * conjunction with zimbraPrefJunkLifetime, which is user-modifiable. The
     * shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraMailSpamLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=105)
    public void setMailSpamLifetime(String zimbraMailSpamLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailSpamLifetime, zimbraMailSpamLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * The algorithm to use when aggregating new messages into conversations.
     * Possible values are: - &quot;none&quot;: no conversation threading is
     * performed. - &quot;subject&quot;: the message will be threaded based
     * solely on its normalized subject. - &quot;strict&quot;: only the
     * threading message headers (References, In-Reply-To, Message-ID, and
     * Resent-Message-ID) are used to correlate messages. No checking of
     * normalized subjects is performed. - &quot;references&quot;: the same
     * logic as &quot;strict&quot; with the constraints slightly altered so
     * that the non-standard Thread-Index header is considered when threading
     * messages and that a reply message lacking References and In-Reply-To
     * headers will fall back to using subject-based threading. -
     * &quot;subjrefs&quot;: the same logic as &quot;references&quot; with
     * the further caveat that changes in the normalized subject will break a
     * thread in two.
     *
     * <p>Valid values: [subject, subjrefs, references, strict, none]
     *
     * @return zimbraMailThreadingAlgorithm, or ZAttrProvisioning.MailThreadingAlgorithm.references if unset and/or has invalid value
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1160)
    public ZAttrProvisioning.MailThreadingAlgorithm getMailThreadingAlgorithm() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraMailThreadingAlgorithm, true, true); return v == null ? ZAttrProvisioning.MailThreadingAlgorithm.references : ZAttrProvisioning.MailThreadingAlgorithm.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.MailThreadingAlgorithm.references; }
    }

  /**
     * The algorithm to use when aggregating new messages into conversations.
     * Possible values are: - &quot;none&quot;: no conversation threading is
     * performed. - &quot;subject&quot;: the message will be threaded based
     * solely on its normalized subject. - &quot;strict&quot;: only the
     * threading message headers (References, In-Reply-To, Message-ID, and
     * Resent-Message-ID) are used to correlate messages. No checking of
     * normalized subjects is performed. - &quot;references&quot;: the same
     * logic as &quot;strict&quot; with the constraints slightly altered so
     * that the non-standard Thread-Index header is considered when threading
     * messages and that a reply message lacking References and In-Reply-To
     * headers will fall back to using subject-based threading. -
     * &quot;subjrefs&quot;: the same logic as &quot;references&quot; with
     * the further caveat that changes in the normalized subject will break a
     * thread in two.
     *
     * <p>Valid values: [subject, subjrefs, references, strict, none]
     *
     * @param zimbraMailThreadingAlgorithm new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1160)
    public void setMailThreadingAlgorithm(ZAttrProvisioning.MailThreadingAlgorithm zimbraMailThreadingAlgorithm) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailThreadingAlgorithm, zimbraMailThreadingAlgorithm.toString());
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Retention period of messages in the Trash folder. 0 means that all
     * messages will be retained. This admin-modifiable attribute works in
     * conjunction with zimbraPrefTrashLifetime, which is user-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getMailTrashLifetimeAsString to access value as a string.
     *
     * @see #getMailTrashLifetimeAsString()
     *
     * @return zimbraMailTrashLifetime in millseconds, or 2592000000 (30d)  if unset
     */
    @ZAttr(id=104)
    public long getMailTrashLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraMailTrashLifetime, 2592000000L, true);
    }

    /**
     * Retention period of messages in the Trash folder. 0 means that all
     * messages will be retained. This admin-modifiable attribute works in
     * conjunction with zimbraPrefTrashLifetime, which is user-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraMailTrashLifetime, or "30d" if unset
     */
    @ZAttr(id=104)
    public String getMailTrashLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraMailTrashLifetime, "30d", true);
    }

    /**
     * Retention period of messages in the Trash folder. 0 means that all
     * messages will be retained. This admin-modifiable attribute works in
     * conjunction with zimbraPrefTrashLifetime, which is user-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraMailTrashLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=104)
    public void setMailTrashLifetime(String zimbraMailTrashLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailTrashLifetime, zimbraMailTrashLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Maximum number of entries for zimbraPrefMailTrustedSenderList.
     *
     * @return zimbraMailTrustedSenderListMaxNumEntries, or 500 if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1139)
    public int getMailTrustedSenderListMaxNumEntries() {
        return getIntAttr(ZAttrProvisioning.A_zimbraMailTrustedSenderListMaxNumEntries, 500, true);
    }

  /**
     * temporary RFC822 email address of this recipient for accepting mail
     * during account rename
     *
     * @return zimbraOldMailAddress, or null if unset
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2143)
    public String getOldMailAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraOldMailAddress, null, true);
    }

    /**
     * temporary RFC822 email address of this recipient for accepting mail
     * during account rename
     *
     * @param zimbraOldMailAddress new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2143)
    public void setOldMailAddress(String zimbraOldMailAddress) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraOldMailAddress, zimbraOldMailAddress);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * temporary RFC822 email address of this recipient for accepting mail
     * during account rename
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2143)
    public void unsetOldMailAddress() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraOldMailAddress, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * block common keywords in password string
     *
     * @return zimbraPasswordBlockCommonEnabled, or false if unset
     *
     * @since ZCS 9.0.0
     */
    @ZAttr(id=3090)
    public boolean isPasswordBlockCommonEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPasswordBlockCommonEnabled, false, true);
    }

  /**
     * whether or not to enforce password history. Number of unique passwords
     * a user must have before being allowed to re-use an old one. A value of
     * 0 means no password history.
     *
     * @return zimbraPasswordEnforceHistory, or 0 if unset
     */
    @ZAttr(id=37)
    public int getPasswordEnforceHistory() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordEnforceHistory, 0, true);
    }

  /**
     * user is unable to change password
     *
     * @return zimbraPasswordLocked, or false if unset
     */
    @ZAttr(id=45)
    public boolean isPasswordLocked() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPasswordLocked, false, true);
    }

  /**
     * whether or not account lockout is enabled.
     *
     * @param zimbraPasswordLockoutEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=378)
    public void setPasswordLockoutEnabled(boolean zimbraPasswordLockoutEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPasswordLockoutEnabled, zimbraPasswordLockoutEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * the duration after which old consecutive failed login attempts are
     * purged from the list, even though no successful authentication has
     * occurred. Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * @param zimbraPasswordLockoutFailureLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=381)
    public void setPasswordLockoutFailureLifetime(String zimbraPasswordLockoutFailureLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPasswordLockoutFailureLifetime, zimbraPasswordLockoutFailureLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * this attribute contains the timestamps of each of the consecutive
     * authentication failures made on an account
     *
     * @return zimbraPasswordLockoutFailureTime, or empty array if unset
     */
    @ZAttr(id=383)
    public String[] getPasswordLockoutFailureTimeAsString() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraPasswordLockoutFailureTime, true, true);
    }

  /**
     * the time at which an account was locked
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=382)
    public void unsetPasswordLockoutLockedTime() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPasswordLockoutLockedTime, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * number of consecutive failed login attempts until an account is locked
     * out
     *
     * @param zimbraPasswordLockoutMaxFailures new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=380)
    public void setPasswordLockoutMaxFailures(int zimbraPasswordLockoutMaxFailures) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPasswordLockoutMaxFailures, Integer.toString(zimbraPasswordLockoutMaxFailures));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Number of invalid passwords kept in a cache per account. Any number of
     * login attempts using password present in cache will be considered as
     * single failed attempt. If Twofactor authentication enabled the cache
     * size will be sum of zimbraPasswordLockoutSuppressionCacheSize and
     * number of application specific password generated.
     *
     * @return zimbraPasswordLockoutSuppressionCacheSize, or 1 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2086)
    public int getPasswordLockoutSuppressionCacheSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordLockoutSuppressionCacheSize, 1, true);
    }

  /**
     * If TRUE it will not increment the repeated failed login attempt using
     * old or invalid password from
     * zimbraPasswordLockoutSuppressionProtocols.
     *
     * @return zimbraPasswordLockoutSuppressionEnabled, or true if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2087)
    public boolean isPasswordLockoutSuppressionEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPasswordLockoutSuppressionEnabled, true, true);
    }

  /**
     * Protocols for which repeated failed login attempts with same password
     * considered as single failure. Supported protocols
     * zsync,imap,pop3,http_basic,http_dav,soap.
     *
     * <p>Valid values: [zsync, imap, pop3, http_basic, http_dav, soap]
     *
     * @return zimbraPasswordLockoutSuppressionProtocols, or empty array if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2088)
    public String[] getPasswordLockoutSuppressionProtocolsAsString() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraPasswordLockoutSuppressionProtocols, true, true); return value.length > 0 ? value : new String[] {"zsync"};
    }

  /**
     * maximum days between password changes
     *
     * @return zimbraPasswordMaxAge, or 0 if unset
     */
    @ZAttr(id=36)
    public int getPasswordMaxAge() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMaxAge, 0, true);
    }

  /**
     * max length of a password
     *
     * @return zimbraPasswordMaxLength, or 64 if unset
     */
    @ZAttr(id=34)
    public int getPasswordMaxLength() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMaxLength, 64, true);
    }

  /**
     * minimum days between password changes
     *
     * @return zimbraPasswordMinAge, or 0 if unset
     */
    @ZAttr(id=35)
    public int getPasswordMinAge() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinAge, 0, true);
    }

  /**
     * minimum number of numeric or ascii punctuation characters required in
     * a password
     *
     * @return zimbraPasswordMinDigitsOrPuncs, or 0 if unset
     *
     * @since ZCS 7.1.3
     */
    @ZAttr(id=1255)
    public int getPasswordMinDigitsOrPuncs() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinDigitsOrPuncs, 0, true);
    }

  /**
     * minimum length of a password
     *
     * @return zimbraPasswordMinLength, or 6 if unset
     */
    @ZAttr(id=33)
    public int getPasswordMinLength() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinLength, 6, true);
    }

  /**
     * minimum number of lower case characters required in a password
     *
     * @return zimbraPasswordMinLowerCaseChars, or 0 if unset
     */
    @ZAttr(id=390)
    public int getPasswordMinLowerCaseChars() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinLowerCaseChars, 0, true);
    }

  /**
     * minimum number of numeric characters required in a password
     *
     * @return zimbraPasswordMinNumericChars, or 0 if unset
     */
    @ZAttr(id=392)
    public int getPasswordMinNumericChars() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinNumericChars, 0, true);
    }

  /**
     * minimum number of ascii punctuation characters required in a password
     *
     * @return zimbraPasswordMinPunctuationChars, or 0 if unset
     */
    @ZAttr(id=391)
    public int getPasswordMinPunctuationChars() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinPunctuationChars, 0, true);
    }

  /**
     * minimum number of upper case characters required in a password
     *
     * @return zimbraPasswordMinUpperCaseChars, or 0 if unset
     */
    @ZAttr(id=389)
    public int getPasswordMinUpperCaseChars() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordMinUpperCaseChars, 0, true);
    }

  /**
     * Maximum attempts for password recovery resend
     *
     * @return zimbraPasswordRecoveryMaxAttempts, or 10 if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2141)
    public int getPasswordRecoveryMaxAttempts() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPasswordRecoveryMaxAttempts, 10, true);
    }

    /**
     * Maximum attempts for password recovery resend
     *
     * @param zimbraPasswordRecoveryMaxAttempts new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2141)
    public void setPasswordRecoveryMaxAttempts(int zimbraPasswordRecoveryMaxAttempts) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPasswordRecoveryMaxAttempts, Integer.toString(zimbraPasswordRecoveryMaxAttempts));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether POP3 is enabled for an account
     *
     * @return zimbraPop3Enabled, or true if unset
     */
    @ZAttr(id=175)
    public boolean isPop3Enabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPop3Enabled, true, true);
    }

    /**
     * whether POP3 is enabled for an account
     *
     * @param zimbraPop3Enabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=175)
    public void setPop3Enabled(boolean zimbraPop3Enabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPop3Enabled, zimbraPop3Enabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Use the iCal style delegation model for shared calendars for CalDAV
     * interface when set to TRUE.
     *
     * @return zimbraPrefAppleIcalDelegationEnabled, or false if unset
     *
     * @since ZCS 5.0.17
     */
    @ZAttr(id=1028)
    public boolean isPrefAppleIcalDelegationEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefAppleIcalDelegationEnabled, false, true);
    }

  /**
     * whether or not new address in outgoing email are auto added to address
     * book
     *
     * @return zimbraPrefAutoAddAddressEnabled, or true if unset
     */
    @ZAttr(id=131)
    public boolean isPrefAutoAddAddressEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefAutoAddAddressEnabled, true, true);
    }

  /**
     * Allowed recipients if
     * &quot;zimbraPrefCalendarSendInviteDeniedAutoReply&quot; is TRUE:
     * internal - Only send &quot;invite denied&quot; auto-response if the
     * sender of the original invite is an internal user. sameDomain - Only
     * send &quot;invite denied&quot; auto-response if the sender of the
     * original invite is in the same domain as the invitee. all - No
     * restrictions on who to send &quot;invite denied&quot; auto-responses
     * to.
     *
     * <p>Valid values: [internal, sameDomain, all]
     *
     * @return zimbraPrefCalendarAllowedTargetsForInviteDeniedAutoReply, or ZAttrProvisioning.PrefCalendarAllowedTargetsForInviteDeniedAutoReply.internal if unset and/or has invalid value
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1632)
    public ZAttrProvisioning.PrefCalendarAllowedTargetsForInviteDeniedAutoReply getPrefCalendarAllowedTargetsForInviteDeniedAutoReply() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefCalendarAllowedTargetsForInviteDeniedAutoReply, true, true); return v == null ? ZAttrProvisioning.PrefCalendarAllowedTargetsForInviteDeniedAutoReply.internal : ZAttrProvisioning.PrefCalendarAllowedTargetsForInviteDeniedAutoReply.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefCalendarAllowedTargetsForInviteDeniedAutoReply.internal; }
    }

  /**
     * default visibility of the appointment when starting a new appointment
     * in the UI
     *
     * <p>Valid values: [public, private]
     *
     * @return zimbraPrefCalendarApptVisibility, or ZAttrProvisioning.PrefCalendarApptVisibility.public_ if unset and/or has invalid value
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=832)
    public ZAttrProvisioning.PrefCalendarApptVisibility getPrefCalendarApptVisibility() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefCalendarApptVisibility, true, true); return v == null ? ZAttrProvisioning.PrefCalendarApptVisibility.public_ : ZAttrProvisioning.PrefCalendarApptVisibility.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefCalendarApptVisibility.public_; }
    }

  /**
     * automatically add appointments when invited
     *
     * @return zimbraPrefCalendarAutoAddInvites, or true if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=848)
    public boolean isPrefCalendarAutoAddInvites() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefCalendarAutoAddInvites, true, true);
    }

  /**
     * Forward a copy of calendar invites received to these users.
     *
     * @return zimbraPrefCalendarForwardInvitesTo, or empty array if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=851)
    public String[] getPrefCalendarForwardInvitesTo() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraPrefCalendarForwardInvitesTo, true, true);
    }

  /**
     * initial calendar view to use
     *
     * <p>Valid values: [day, week, workWeek, month, list, year]
     *
     * @return zimbraPrefCalendarInitialView, or ZAttrProvisioning.PrefCalendarInitialView.workWeek if unset and/or has invalid value
     */
    @ZAttr(id=240)
    public ZAttrProvisioning.PrefCalendarInitialView getPrefCalendarInitialView() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefCalendarInitialView, true, true); return v == null ? ZAttrProvisioning.PrefCalendarInitialView.workWeek : ZAttrProvisioning.PrefCalendarInitialView.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefCalendarInitialView.workWeek; }
    }

  /**
     * RFC822 email address for receiving reminders for appointments and
     * tasks
     *
     * @return zimbraPrefCalendarReminderEmail, or null if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=575)
    public String getPrefCalendarReminderEmail() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefCalendarReminderEmail, null, true);
    }

  /**
     * If an invite is received from an organizer who does not have
     * permission to invite this user to a meeting, send an auto-decline
     * reply. Note that
     * zimbraPrefCalendarAllowedTargetsForInviteDeniedAutoReply may further
     * restrict who can receive this reply.
     *
     * @return zimbraPrefCalendarSendInviteDeniedAutoReply, or false if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=849)
    public boolean isPrefCalendarSendInviteDeniedAutoReply() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefCalendarSendInviteDeniedAutoReply, false, true);
    }

  /**
     * working hours for each day of the week
     *
     * @return zimbraPrefCalendarWorkingHours, or "1:N:0800:1700,2:Y:0800:1700,3:Y:0800:1700,4:Y:0800:1700,5:Y:0800:1700,6:Y:0800:1700,7:N:0800:1700" if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1103)
    public String getPrefCalendarWorkingHours() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefCalendarWorkingHours, "1:N:0800:1700,2:Y:0800:1700,3:Y:0800:1700,4:Y:0800:1700,5:Y:0800:1700,6:Y:0800:1700,7:N:0800:1700", true);
    }

  /**
     * dedupeNone|secondCopyIfOnToOrCC|moveSentMessageToInbox|dedupeAll
     *
     * <p>Valid values: [dedupeNone, secondCopyifOnToOrCC, dedupeAll]
     *
     * @return zimbraPrefDedupeMessagesSentToSelf, or ZAttrProvisioning.PrefDedupeMessagesSentToSelf.dedupeNone if unset and/or has invalid value
     */
    @ZAttr(id=144)
    public ZAttrProvisioning.PrefDedupeMessagesSentToSelf getPrefDedupeMessagesSentToSelf() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefDedupeMessagesSentToSelf, true, true); return v == null ? ZAttrProvisioning.PrefDedupeMessagesSentToSelf.dedupeNone : ZAttrProvisioning.PrefDedupeMessagesSentToSelf.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefDedupeMessagesSentToSelf.dedupeNone; }
    }

  /**
     * Default calendar folder id. Current default calendar id is 10, as
     * calendar folder with id 10, is created for all users. Cos level change
     * is blocked. So admin can not change value of this attribute on cos
     * level.
     *
     * @return zimbraPrefDefaultCalendarId, or 10 if unset
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2994)
    public int getPrefDefaultCalendarId() {
        return getIntAttr(ZAttrProvisioning.A_zimbraPrefDefaultCalendarId, 10, true);
    }

    /**
     * Default calendar folder id. Current default calendar id is 10, as
     * calendar folder with id 10, is created for all users. Cos level change
     * is blocked. So admin can not change value of this attribute on cos
     * level.
     *
     * @param zimbraPrefDefaultCalendarId new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2994)
    public void setPrefDefaultCalendarId(int zimbraPrefDefaultCalendarId) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefDefaultCalendarId, Integer.toString(zimbraPrefDefaultCalendarId));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * default mail signature for account/identity/dataSource
     *
     * @return zimbraPrefDefaultSignatureId, or null if unset
     */
    @ZAttr(id=492)
    public String getPrefDefaultSignatureId() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefDefaultSignatureId, null, true);
    }

  /**
     * default mail signature for account/identity/dataSource
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=492)
    public void unsetPrefDefaultSignatureId() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefDefaultSignatureId, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Which mailbox to save messages sent via sendAs/sendOnBehalfOf
     * delegation to.
     *
     * <p>Valid values: [owner, sender, both, none]
     *
     * @return zimbraPrefDelegatedSendSaveTarget, or ZAttrProvisioning.PrefDelegatedSendSaveTarget.owner if unset and/or has invalid value
     *
     * @since ZCS 8.6.0,9.0.0
     */
    @ZAttr(id=1651)
    public ZAttrProvisioning.PrefDelegatedSendSaveTarget getPrefDelegatedSendSaveTarget() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefDelegatedSendSaveTarget, true, true); return v == null ? ZAttrProvisioning.PrefDelegatedSendSaveTarget.owner : ZAttrProvisioning.PrefDelegatedSendSaveTarget.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefDelegatedSendSaveTarget.owner; }
    }

  /**
     * whether meeting invite emails are moved to Trash folder upon
     * accept/decline
     *
     * @return zimbraPrefDeleteInviteOnReply, or true if unset
     */
    @ZAttr(id=470)
    public boolean isPrefDeleteInviteOnReply() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefDeleteInviteOnReply, true, true);
    }

  /**
     * Specifies the meaning of an external sender. &quot;ALL&quot; means
     * users whose domain doesn&#039;t match the recipient&#039;s or
     * zimbraInternalSendersDomain. &quot;ALLNOTINAB&quot; means
     * &quot;ALL&quot; minus users who are in the recipient&#039;s address
     * book. &quot;INAB&quot; Users/Addresses whose domain doesn&#039;t match
     * the recipient&#039;s domain or zimbraInternalSendersDomain and which
     * are present in recipient&#039;s address book. &quot;INSD&quot; means
     * users whose domain matches the specific domain
     *
     * <p>Valid values: [ALL, ALLNOTINAB, INAB, INSD]
     *
     * @return zimbraPrefExternalSendersType, or ZAttrProvisioning.PrefExternalSendersType.ALL if unset and/or has invalid value
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1320)
    public ZAttrProvisioning.PrefExternalSendersType getPrefExternalSendersType() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefExternalSendersType, true, true); return v == null ? ZAttrProvisioning.PrefExternalSendersType.ALL : ZAttrProvisioning.PrefExternalSendersType.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefExternalSendersType.ALL; }
    }

  /**
     * Specifies the meaning of an external sender. &quot;ALL&quot; means
     * users whose domain doesn&#039;t match the recipient&#039;s or
     * zimbraInternalSendersDomain. &quot;ALLNOTINAB&quot; means
     * &quot;ALL&quot; minus users who are in the recipient&#039;s address
     * book. &quot;INAB&quot; Users/Addresses whose domain doesn&#039;t match
     * the recipient&#039;s domain or zimbraInternalSendersDomain and which
     * are present in recipient&#039;s address book. &quot;INSD&quot; means
     * users whose domain matches the specific domain
     *
     * <p>Valid values: [ALL, ALLNOTINAB, INAB, INSD]
     *
     * @param zimbraPrefExternalSendersType new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1320)
    public void setPrefExternalSendersType(ZAttrProvisioning.PrefExternalSendersType zimbraPrefExternalSendersType) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefExternalSendersType, zimbraPrefExternalSendersType.toString());
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Specifies the meaning of an external sender. &quot;ALL&quot; means
     * users whose domain doesn&#039;t match the recipient&#039;s or
     * zimbraInternalSendersDomain. &quot;ALLNOTINAB&quot; means
     * &quot;ALL&quot; minus users who are in the recipient&#039;s address
     * book. &quot;INAB&quot; Users/Addresses whose domain doesn&#039;t match
     * the recipient&#039;s domain or zimbraInternalSendersDomain and which
     * are present in recipient&#039;s address book. &quot;INSD&quot; means
     * users whose domain matches the specific domain
     *
     * <p>Valid values: [ALL, ALLNOTINAB, INAB, INSD]
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1320)
    public void unsetPrefExternalSendersType() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefExternalSendersType, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * email address to put in from header. Deprecated on data source as of
     * bug 67068.
     *
     * @return zimbraPrefFromAddress, or null if unset
     */
    @ZAttr(id=403)
    public String getPrefFromAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefFromAddress, null, true);
    }

  /**
     * personal part of email address put in from header
     *
     * @return zimbraPrefFromDisplay, or null if unset
     */
    @ZAttr(id=402)
    public String getPrefFromDisplay() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefFromDisplay, null, true);
    }

  /**
     * whether end-user wants auto-complete from GAL. Feature must also be
     * enabled.
     *
     * @return zimbraPrefGalAutoCompleteEnabled, or true if unset
     */
    @ZAttr(id=372)
    public boolean isPrefGalAutoCompleteEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefGalAutoCompleteEnabled, true, true);
    }

  /**
     * whether end user choose to use IMAP feature. If it is set to TRUE,
     * IMAP feature is available only when zimbraImapEnabled is TRUE.
     *
     * @return zimbraPrefImapEnabled, or true if unset
     *
     * @since ZCS 9.0.0
     */
    @ZAttr(id=3086)
    public boolean isPrefImapEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefImapEnabled, true, true);
    }

    /**
     * whether end user choose to use IMAP feature. If it is set to TRUE,
     * IMAP feature is available only when zimbraImapEnabled is TRUE.
     *
     * @param zimbraPrefImapEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 9.0.0
     */
    @ZAttr(id=3086)
    public void setPrefImapEnabled(boolean zimbraPrefImapEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefImapEnabled, zimbraPrefImapEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Retention period of read messages in the Inbox folder. 0 means that
     * all messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getPrefInboxReadLifetimeAsString to access value as a string.
     *
     * @see #getPrefInboxReadLifetimeAsString()
     *
     * @return zimbraPrefInboxReadLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=538)
    public long getPrefInboxReadLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraPrefInboxReadLifetime, 0L, true);
    }

    /**
     * Retention period of read messages in the Inbox folder. 0 means that
     * all messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraPrefInboxReadLifetime, or "0" if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=538)
    public String getPrefInboxReadLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefInboxReadLifetime, "0", true);
    }

    /**
     * Retention period of read messages in the Inbox folder. 0 means that
     * all messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraPrefInboxReadLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=538)
    public void setPrefInboxReadLifetime(String zimbraPrefInboxReadLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefInboxReadLifetime, zimbraPrefInboxReadLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Retention period of unread messages in the Inbox folder. 0 means that
     * all messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getPrefInboxUnreadLifetimeAsString to access value as a string.
     *
     * @see #getPrefInboxUnreadLifetimeAsString()
     *
     * @return zimbraPrefInboxUnreadLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=537)
    public long getPrefInboxUnreadLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraPrefInboxUnreadLifetime, 0L, true);
    }

    /**
     * Retention period of unread messages in the Inbox folder. 0 means that
     * all messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraPrefInboxUnreadLifetime, or "0" if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=537)
    public String getPrefInboxUnreadLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefInboxUnreadLifetime, "0", true);
    }

    /**
     * Retention period of unread messages in the Inbox folder. 0 means that
     * all messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraPrefInboxUnreadLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=537)
    public void setPrefInboxUnreadLifetime(String zimbraPrefInboxUnreadLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefInboxUnreadLifetime, zimbraPrefInboxUnreadLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether or not to include spam in search by default
     *
     * @return zimbraPrefIncludeSpamInSearch, or true if unset
     */
    @ZAttr(id=55)
    public boolean isPrefIncludeSpamInSearch() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefIncludeSpamInSearch, true, true);
    }

  /**
     * whether or not to include trash in search by default
     *
     * @return zimbraPrefIncludeTrashInSearch, or true if unset
     */
    @ZAttr(id=56)
    public boolean isPrefIncludeTrashInSearch() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefIncludeTrashInSearch, true, true);
    }

  /**
     * Retention period of messages in the Junk folder. 0 means that all
     * messages will be retained. This user-modifiable attribute works in
     * conjunction with zimbraMailSpamLifetime, which is admin-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getPrefJunkLifetimeAsString to access value as a string.
     *
     * @see #getPrefJunkLifetimeAsString()
     *
     * @return zimbraPrefJunkLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=540)
    public long getPrefJunkLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraPrefJunkLifetime, 0L, true);
    }

    /**
     * Retention period of messages in the Junk folder. 0 means that all
     * messages will be retained. This user-modifiable attribute works in
     * conjunction with zimbraMailSpamLifetime, which is admin-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraPrefJunkLifetime, or "0" if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=540)
    public String getPrefJunkLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefJunkLifetime, "0", true);
    }

    /**
     * Retention period of messages in the Junk folder. 0 means that all
     * messages will be retained. This user-modifiable attribute works in
     * conjunction with zimbraMailSpamLifetime, which is admin-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraPrefJunkLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=540)
    public void setPrefJunkLifetime(String zimbraPrefJunkLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefJunkLifetime, zimbraPrefJunkLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * user locale preference, e.g. en_US Whenever the server looks for the
     * user locale, it will first look for zimbraPrefLocale, if it is not set
     * then it will fallback to the current mechanism of looking for
     * zimbraLocale in the various places for a user. zimbraLocale is the non
     * end-user attribute that specifies which locale an object defaults to,
     * it is not an end-user setting.
     *
     * @return zimbraPrefLocale, or null if unset
     */
    @ZAttr(id=442)
    public String getPrefLocale() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefLocale, null, true);
    }

    /**
     * user locale preference, e.g. en_US Whenever the server looks for the
     * user locale, it will first look for zimbraPrefLocale, if it is not set
     * then it will fallback to the current mechanism of looking for
     * zimbraLocale in the various places for a user. zimbraLocale is the non
     * end-user attribute that specifies which locale an object defaults to,
     * it is not an end-user setting.
     *
     * @param zimbraPrefLocale new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=442)
    public void setPrefLocale(String zimbraPrefLocale) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefLocale, zimbraPrefLocale);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Default Charset for mail composing and parsing text
     *
     * @return zimbraPrefMailDefaultCharset, or null if unset
     */
    @ZAttr(id=469)
    public String getPrefMailDefaultCharset() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefMailDefaultCharset, null, true);
    }

    /**
     * Default Charset for mail composing and parsing text
     *
     * @param zimbraPrefMailDefaultCharset new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=469)
    public void setPrefMailDefaultCharset(String zimbraPrefMailDefaultCharset) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefMailDefaultCharset, zimbraPrefMailDefaultCharset);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * a list of comma separated folder ids of all folders used to count for
     * showing a new message indicator icon for the account, useful in UIs
     * managing multiple accounts: desktop and family mailboxes.
     *
     * @return zimbraPrefMailFoldersCheckedForNewMsgIndicator, or null if unset
     *
     * @since ZCS 6.0.5
     */
    @ZAttr(id=1072)
    public String getPrefMailFoldersCheckedForNewMsgIndicator() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefMailFoldersCheckedForNewMsgIndicator, null, true);
    }

  /**
     * RFC822 forwarding address for an account
     *
     * @return zimbraPrefMailForwardingAddress, or null if unset
     */
    @ZAttr(id=343)
    public String getPrefMailForwardingAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefMailForwardingAddress, null, true);
    }

    /**
     * RFC822 forwarding address for an account
     *
     * @param zimbraPrefMailForwardingAddress new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=343)
    public void setPrefMailForwardingAddress(String zimbraPrefMailForwardingAddress) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefMailForwardingAddress, zimbraPrefMailForwardingAddress);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * initial search done by dhtml client
     *
     * @return zimbraPrefMailInitialSearch, or "in:inbox" if unset
     */
    @ZAttr(id=102)
    public String getPrefMailInitialSearch() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefMailInitialSearch, "in:inbox", true);
    }

  /**
     * whether or not to deliver mail locally
     *
     * @return zimbraPrefMailLocalDeliveryDisabled, or false if unset
     */
    @ZAttr(id=344)
    public boolean isPrefMailLocalDeliveryDisabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefMailLocalDeliveryDisabled, false, true);
    }

  /**
     * whether to send read receipt
     *
     * <p>Valid values: [always, never, prompt]
     *
     * @return zimbraPrefMailSendReadReceipts, or ZAttrProvisioning.PrefMailSendReadReceipts.prompt if unset and/or has invalid value
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=822)
    public ZAttrProvisioning.PrefMailSendReadReceipts getPrefMailSendReadReceipts() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefMailSendReadReceipts, true, true); return v == null ? ZAttrProvisioning.PrefMailSendReadReceipts.prompt : ZAttrProvisioning.PrefMailSendReadReceipts.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefMailSendReadReceipts.prompt; }
    }

  /**
     * Account-level switch that enables message deduping. See
     * zimbraMessageIdDedupeCacheSize for more details.
     *
     * @return zimbraPrefMessageIdDedupingEnabled, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1198)
    public boolean isPrefMessageIdDedupingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefMessageIdDedupingEnabled, true, true);
    }

  /**
     * If TRUE, send zimbraPrefOutOfOfficeExternalReply to external senders.
     * External senders are specified by zimbraInternalSendersDomain and
     * zimbraPrefExternalSendersType.
     *
     * @return zimbraPrefOutOfOfficeExternalReplyEnabled, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1318)
    public boolean isPrefOutOfOfficeExternalReplyEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefOutOfOfficeExternalReplyEnabled, false, true);
    }

    /**
     * If TRUE, send zimbraPrefOutOfOfficeExternalReply to external senders.
     * External senders are specified by zimbraInternalSendersDomain and
     * zimbraPrefExternalSendersType.
     *
     * @param zimbraPrefOutOfOfficeExternalReplyEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1318)
    public void setPrefOutOfOfficeExternalReplyEnabled(boolean zimbraPrefOutOfOfficeExternalReplyEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefOutOfOfficeExternalReplyEnabled, zimbraPrefOutOfOfficeExternalReplyEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether or not out of office reply is enabled
     *
     * @return zimbraPrefOutOfOfficeReplyEnabled, or false if unset
     */
    @ZAttr(id=59)
    public boolean isPrefOutOfOfficeReplyEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefOutOfOfficeReplyEnabled, false, true);
    }

  /**
     * Specific domains to which custom out of office message is to be sent
     *
     * @return zimbraPrefOutOfOfficeSpecificDomains, or empty array if unset
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2132)
    public String[] getPrefOutOfOfficeSpecificDomains() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraPrefOutOfOfficeSpecificDomains, true, true);
    }

    /**
     * Specific domains to which custom out of office message is to be sent
     *
     * @param zimbraPrefOutOfOfficeSpecificDomains new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.5
     */
    @ZAttr(id=2132)
    public void setPrefOutOfOfficeSpecificDomains(String[] zimbraPrefOutOfOfficeSpecificDomains) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefOutOfOfficeSpecificDomains, zimbraPrefOutOfOfficeSpecificDomains);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * If TRUE, OOO reply is not sent to external senders, when the user
     * enables OOO for the account
     *
     * @return zimbraPrefOutOfOfficeSuppressExternalReply, or false if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1576)
    public boolean isPrefOutOfOfficeSuppressExternalReply() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefOutOfOfficeSuppressExternalReply, false, true);
    }

    /**
     * If TRUE, OOO reply is not sent to external senders, when the user
     * enables OOO for the account
     *
     * @param zimbraPrefOutOfOfficeSuppressExternalReply new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1576)
    public void setPrefOutOfOfficeSuppressExternalReply(boolean zimbraPrefOutOfOfficeSuppressExternalReply) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefOutOfOfficeSuppressExternalReply, zimbraPrefOutOfOfficeSuppressExternalReply ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * RFC822 recovery email address for an account
     *
     * @return zimbraPrefPasswordRecoveryAddress, or null if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2135)
    public String getPrefPasswordRecoveryAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefPasswordRecoveryAddress, null, true);
    }

  /**
     * End-user recovery email address verification status
     *
     * <p>Valid values: [verified, pending]
     *
     * @return zimbraPrefPasswordRecoveryAddressStatus, or null if unset and/or has invalid value
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2136)
    public ZAttrProvisioning.PrefPasswordRecoveryAddressStatus getPrefPasswordRecoveryAddressStatus() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefPasswordRecoveryAddressStatus, true, true); return v == null ? null : ZAttrProvisioning.PrefPasswordRecoveryAddressStatus.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return null; }
    }

  /**
     * End-user recovery email address verification status
     *
     * <p>Valid values: [verified, pending]
     *
     * @param zimbraPrefPasswordRecoveryAddressStatus new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2136)
    public void setPrefPasswordRecoveryAddressStatus(ZAttrProvisioning.PrefPasswordRecoveryAddressStatus zimbraPrefPasswordRecoveryAddressStatus) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefPasswordRecoveryAddressStatus, zimbraPrefPasswordRecoveryAddressStatus.toString());
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * When messages are accessed via POP3: - keep: Leave DELE&#039;ed
     * messages in Inbox. - read: Mark RETR&#039;ed messages as read, and
     * leave DELE&#039;ed messages in Inbox. - trash: Move DELE&#039;ed
     * messages to Trash, and mark them as read. - delete: Hard-delete
     * DELE&#039;ed messages. This is the straightforward POP3
     * implementation.
     *
     * <p>Valid values: [keep, read, trash, delete]
     *
     * @return zimbraPrefPop3DeleteOption, or ZAttrProvisioning.PrefPop3DeleteOption.delete if unset and/or has invalid value
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1165)
    public ZAttrProvisioning.PrefPop3DeleteOption getPrefPop3DeleteOption() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraPrefPop3DeleteOption, true, true); return v == null ? ZAttrProvisioning.PrefPop3DeleteOption.delete : ZAttrProvisioning.PrefPop3DeleteOption.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.PrefPop3DeleteOption.delete; }
    }

  /**
     * whether end user choose to use POP3 feature. If it is set to TRUE,
     * POP3 feature is available only when zimbraPop3Enabled is TRUE.
     *
     * @return zimbraPrefPop3Enabled, or true if unset
     *
     * @since ZCS 9.0.0
     */
    @ZAttr(id=3087)
    public boolean isPrefPop3Enabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefPop3Enabled, true, true);
    }

    /**
     * whether end user choose to use POP3 feature. If it is set to TRUE,
     * POP3 feature is available only when zimbraPop3Enabled is TRUE.
     *
     * @param zimbraPrefPop3Enabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 9.0.0
     */
    @ZAttr(id=3087)
    public void setPrefPop3Enabled(boolean zimbraPrefPop3Enabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefPop3Enabled, zimbraPrefPop3Enabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether or not to include spam messages in POP3 access
     *
     * @return zimbraPrefPop3IncludeSpam, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1166)
    public boolean isPrefPop3IncludeSpam() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefPop3IncludeSpam, false, true);
    }

  /**
     * address to put in reply-to header
     *
     * @return zimbraPrefReplyToAddress, or null if unset
     */
    @ZAttr(id=60)
    public String getPrefReplyToAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefReplyToAddress, null, true);
    }

  /**
     * personal part of email address put in reply-to header
     *
     * @return zimbraPrefReplyToDisplay, or null if unset
     */
    @ZAttr(id=404)
    public String getPrefReplyToDisplay() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefReplyToDisplay, null, true);
    }

  /**
     * TRUE if we should set a reply-to header
     *
     * @return zimbraPrefReplyToEnabled, or false if unset
     */
    @ZAttr(id=405)
    public boolean isPrefReplyToEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefReplyToEnabled, false, true);
    }

  /**
     * whether or not to save outgoing mail (deprecatedSince 5.0 in identity)
     *
     * @return zimbraPrefSaveToSent, or true if unset
     */
    @ZAttr(id=22)
    public boolean isPrefSaveToSent() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefSaveToSent, true, true);
    }

  /**
     * Retention period of messages in the Sent folder. 0 means that all
     * messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getPrefSentLifetimeAsString to access value as a string.
     *
     * @see #getPrefSentLifetimeAsString()
     *
     * @return zimbraPrefSentLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=539)
    public long getPrefSentLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraPrefSentLifetime, 0L, true);
    }

    /**
     * Retention period of messages in the Sent folder. 0 means that all
     * messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraPrefSentLifetime, or "0" if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=539)
    public String getPrefSentLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefSentLifetime, "0", true);
    }

    /**
     * Retention period of messages in the Sent folder. 0 means that all
     * messages will be retained. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraPrefSentLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=539)
    public void setPrefSentLifetime(String zimbraPrefSentLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefSentLifetime, zimbraPrefSentLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether end-user wants auto-complete from shared address books.
     *
     * @return zimbraPrefSharedAddrBookAutoCompleteEnabled, or false if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=759)
    public boolean isPrefSharedAddrBookAutoCompleteEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefSharedAddrBookAutoCompleteEnabled, false, true);
    }

  /**
     * The name of the dictionary used for spell checking. If not set, the
     * locale is used.
     *
     * @return zimbraPrefSpellDictionary, or null if unset
     *
     * @since ZCS 6.0.0_GA
     */
    @ZAttr(id=1041)
    public String getPrefSpellDictionary() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefSpellDictionary, null, true);
    }

  /**
     * If TRUE, the spell checker ignores words that contain only upper-case
     * letters.
     *
     * @return zimbraPrefSpellIgnoreAllCaps, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1207)
    public boolean isPrefSpellIgnoreAllCaps() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefSpellIgnoreAllCaps, true, true);
    }

  /**
     * Regular Expression for words to ignore during spell check.
     *
     * @return zimbraPrefSpellIgnorePattern, or null if unset
     *
     * @since ZCS 8.0.4
     */
    @ZAttr(id=1432)
    public String getPrefSpellIgnorePattern() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefSpellIgnorePattern, null, true);
    }

  /**
     * List of words to ignore when checking spelling. The word list of an
     * account includes the words specified for its cos and domain.
     *
     * @return zimbraPrefSpellIgnoreWord, or empty array if unset
     *
     * @since ZCS 6.0.5
     */
    @ZAttr(id=1073)
    public String[] getPrefSpellIgnoreWord() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraPrefSpellIgnoreWord, true, true); return value.length > 0 ? value : new String[] {"blog"};
    }

  /**
     * Retention period of messages in the Trash folder. 0 means that all
     * messages will be retained. This user-modifiable attribute works in
     * conjunction with zimbraMailTrashLifetime, which is admin-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getPrefTrashLifetimeAsString to access value as a string.
     *
     * @see #getPrefTrashLifetimeAsString()
     *
     * @return zimbraPrefTrashLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=541)
    public long getPrefTrashLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraPrefTrashLifetime, 0L, true);
    }

    /**
     * Retention period of messages in the Trash folder. 0 means that all
     * messages will be retained. This user-modifiable attribute works in
     * conjunction with zimbraMailTrashLifetime, which is admin-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @return zimbraPrefTrashLifetime, or "0" if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=541)
    public String getPrefTrashLifetimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraPrefTrashLifetime, "0", true);
    }

    /**
     * Retention period of messages in the Trash folder. 0 means that all
     * messages will be retained. This user-modifiable attribute works in
     * conjunction with zimbraMailTrashLifetime, which is admin-modifiable.
     * The shorter duration is used. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * @param zimbraPrefTrashLifetime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=541)
    public void setPrefTrashLifetime(String zimbraPrefTrashLifetime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPrefTrashLifetime, zimbraPrefTrashLifetime);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * When composing and sending mail, whether to use RFC 2231 MIME
     * parameter value encoding. If set to FALSE, then RFC 2047 style
     * encoding is used.
     *
     * @return zimbraPrefUseRfc2231, or false if unset
     */
    @ZAttr(id=395)
    public boolean isPrefUseRfc2231() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPrefUseRfc2231, false, true);
    }

  /**
     * timestamp of account rename and previous name of the account
     *
     * @return zimbraPrimaryEmailChangeHistory, or empty array if unset
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2144)
    public String[] getPrimaryEmailChangeHistory() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraPrimaryEmailChangeHistory, true, true);
    }

  /**
     * timestamp of account rename and previous name of the account
     *
     * @param zimbraPrimaryEmailChangeHistory new to add to existing values
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.10
     */
    @ZAttr(id=2144)
    public void addPrimaryEmailChangeHistory(String zimbraPrimaryEmailChangeHistory) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        StringUtil.addToMultiMap(attrs, "+"  + ZAttrProvisioning.A_zimbraPrimaryEmailChangeHistory, zimbraPrimaryEmailChangeHistory);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Maximum allowed lifetime of public shares. A value of 0 indicates that
     * there&#039;s no limit on a public share&#039;s lifetime. . Must be in
     * valid duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getPublicShareLifetimeAsString to access value as a string.
     *
     * @see #getPublicShareLifetimeAsString()
     *
     * @return zimbraPublicShareLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1355)
    public long getPublicShareLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraPublicShareLifetime, 0L, true);
    }

  /**
     * switch for turning public sharing on/off
     *
     * @return zimbraPublicSharingEnabled, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1351)
    public boolean isPublicSharingEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraPublicSharingEnabled, true, true);
    }

  /**
     * Expiry time for recovery email code verification. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getRecoveryAccountCodeValidityAsString to access value as a string.
     *
     * @see #getRecoveryAccountCodeValidityAsString()
     *
     * @return zimbraRecoveryAccountCodeValidity in millseconds, or 86400000 (1d)  if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2140)
    public long getRecoveryAccountCodeValidity() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraRecoveryAccountCodeValidity, 86400000L, true);
    }

  /**
     * Recovery email verification data
     *
     * @return zimbraRecoveryAccountVerificationData, or null if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2139)
    public String getRecoveryAccountVerificationData() {
        return getAttr(ZAttrProvisioning.A_zimbraRecoveryAccountVerificationData, null, true);
    }

  /**
     * Recovery code sent to recovery email address
     *
     * @return zimbraResetPasswordRecoveryCode, or null if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2137)
    public String getResetPasswordRecoveryCode() {
        return getAttr(ZAttrProvisioning.A_zimbraResetPasswordRecoveryCode, null, true);
    }

  /**
     * Recovery code sent to recovery email address
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2137)
    public void unsetResetPasswordRecoveryCode() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraResetPasswordRecoveryCode, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Expiry time for password reset recovery code. Must be in valid
     * duration format: {digits}{time-unit}. digits: 0-9, time-unit:
     * [hmsd]|ms. h - hours, m - minutes, s - seconds, d - days, ms -
     * milliseconds. If time unit is not specified, the default is
     * s(seconds).
     *
     * <p>Use getResetPasswordRecoveryCodeExpiryAsString to access value as a string.
     *
     * @see #getResetPasswordRecoveryCodeExpiryAsString()
     *
     * @return zimbraResetPasswordRecoveryCodeExpiry in millseconds, or 600000 (10m)  if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2138)
    public long getResetPasswordRecoveryCodeExpiry() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraResetPasswordRecoveryCodeExpiry, 600000L, true);
    }

  /**
     * Maximum allowed lifetime of shares to internal users or groups. A
     * value of 0 indicates that there&#039;s no limit on an internal
     * share&#039;s lifetime. . Must be in valid duration format:
     * {digits}{time-unit}. digits: 0-9, time-unit: [hmsd]|ms. h - hours, m -
     * minutes, s - seconds, d - days, ms - milliseconds. If time unit is not
     * specified, the default is s(seconds).
     *
     * <p>Use getShareLifetimeAsString to access value as a string.
     *
     * @see #getShareLifetimeAsString()
     *
     * @return zimbraShareLifetime in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1348)
    public long getShareLifetime() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraShareLifetime, 0L, true);
    }

  /**
     * All items an account has shared
     *
     * @return zimbraSharedItem, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1219)
    public String[] getSharedItem() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraSharedItem, true, true);
    }

    /**
     * All items an account has shared
     *
     * @param zimbraSharedItem new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1219)
    public void setSharedItem(String[] zimbraSharedItem) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSharedItem, zimbraSharedItem);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether edit header commands in admin sieve scripts are enabled or
     * disabled. If TRUE, the addheader, deleteheader and replaceheader
     * commands will be executed during admin sieve script execution.
     *
     * @return zimbraSieveEditHeaderEnabled, or false if unset
     *
     * @since ZCS 8.8.4
     */
    @ZAttr(id=2121)
    public boolean isSieveEditHeaderEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSieveEditHeaderEnabled, false, true);
    }

    /**
     * Whether edit header commands in admin sieve scripts are enabled or
     * disabled. If TRUE, the addheader, deleteheader and replaceheader
     * commands will be executed during admin sieve script execution.
     *
     * @param zimbraSieveEditHeaderEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.4
     */
    @ZAttr(id=2121)
    public void setSieveEditHeaderEnabled(boolean zimbraSieveEditHeaderEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSieveEditHeaderEnabled, zimbraSieveEditHeaderEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Comma separated list of sieve immutable headers
     *
     * @return zimbraSieveImmutableHeaders, or "Received,DKIM-Signature,Authentication-Results,Received-SPF,Message-ID,Content-Type,Content-Disposition,Content-Transfer-Encoding,MIME-Version,Auto-Submitted" if unset
     *
     * @since ZCS 8.8.4
     */
    @ZAttr(id=2122)
    public String getSieveImmutableHeaders() {
        return getAttr(ZAttrProvisioning.A_zimbraSieveImmutableHeaders, "Received,DKIM-Signature,Authentication-Results,Received-SPF,Message-ID,Content-Type,Content-Disposition,Content-Transfer-Encoding,MIME-Version,Auto-Submitted", true);
    }

    /**
     * Comma separated list of sieve immutable headers
     *
     * @param zimbraSieveImmutableHeaders new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.4
     */
    @ZAttr(id=2122)
    public void setSieveImmutableHeaders(String zimbraSieveImmutableHeaders) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSieveImmutableHeaders, zimbraSieveImmutableHeaders);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether the RFC compliant &#039;notify&#039; is used. If TRUE, ZCS
     * parses the &#039;notify&#039; action parameters based on the syntax
     * defined by the RFC 5435 and 5436. If FALSE, ZCS treats the
     * &#039;notify&#039; action parameters with Zimbra specific format
     *
     * @return zimbraSieveNotifyActionRFCCompliant, or false if unset
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2112)
    public boolean isSieveNotifyActionRFCCompliant() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSieveNotifyActionRFCCompliant, false, true);
    }

  /**
     * Whether to enable the Sieve &quot;reject&quot; action defined in RFC
     * 5429.
     *
     * @return zimbraSieveRejectMailEnabled, or true if unset
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2111)
    public boolean isSieveRejectMailEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSieveRejectMailEnabled, true, true);
    }

    /**
     * Whether to enable the Sieve &quot;reject&quot; action defined in RFC
     * 5429.
     *
     * @param zimbraSieveRejectMailEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.7.8
     */
    @ZAttr(id=2111)
    public void setSieveRejectMailEnabled(boolean zimbraSieveRejectMailEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSieveRejectMailEnabled, zimbraSieveRejectMailEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether the declaration of the Sieve extension feature is mandatory by
     * the &#039;require&#039; control. If TRUE, before ZCS evaluates a Sieve
     * extension test or action, it checks the corresponding capability
     * string at &#039;require&#039; control; and if the capability string is
     * not declared in the &#039;require&#039;, the entire Sieve filter
     * execution will be failed. If FALSE, any Sieve extensions can be used
     * without declaring the capability string in the &#039;require&#039;
     * control.
     *
     * @return zimbraSieveRequireControlEnabled, or true if unset
     *
     * @since ZCS 8.8.4
     */
    @ZAttr(id=2120)
    public boolean isSieveRequireControlEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSieveRequireControlEnabled, true, true);
    }

    /**
     * Whether the declaration of the Sieve extension feature is mandatory by
     * the &#039;require&#039; control. If TRUE, before ZCS evaluates a Sieve
     * extension test or action, it checks the corresponding capability
     * string at &#039;require&#039; control; and if the capability string is
     * not declared in the &#039;require&#039;, the entire Sieve filter
     * execution will be failed. If FALSE, any Sieve extensions can be used
     * without declaring the capability string in the &#039;require&#039;
     * control.
     *
     * @param zimbraSieveRequireControlEnabled new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.8.4
     */
    @ZAttr(id=2120)
    public void setSieveRequireControlEnabled(boolean zimbraSieveRequireControlEnabled) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraSieveRequireControlEnabled, zimbraSieveRequireControlEnabled ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Whether to enable smtp debug trace
     *
     * @return zimbraSmtpEnableTrace, or false if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=793)
    public boolean isSmtpEnableTrace() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSmtpEnableTrace, false, true);
    }

  /**
     * If TRUE, the address for MAIL FROM in the SMTP session will always be
     * set to the email address of the account. If FALSE, the address will be
     * the value of the Sender or From header in the outgoing message, in
     * that order.
     *
     * @return zimbraSmtpRestrictEnvelopeFrom, or false if unset
     *
     * @since ZCS 6.0.5
     */
    @ZAttr(id=1077)
    public boolean isSmtpRestrictEnvelopeFrom() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraSmtpRestrictEnvelopeFrom, false, true);
    }

  /**
     * The registered name of the Zimbra Analyzer Extension for this account
     * to use
     *
     * @return zimbraTextAnalyzer, or null if unset
     */
    @ZAttr(id=393)
    public String getTextAnalyzer() {
        return getAttr(ZAttrProvisioning.A_zimbraTextAnalyzer, null, true);
    }

  /**
     * Deprecated since: 23.8.0. UCService is not in use anymore. Orig desc:
     * password for the user&#039;s UC service
     *
     * @return zimbraUCPassword, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1409)
    public String getUCPassword() {
        return getAttr(ZAttrProvisioning.A_zimbraUCPassword, null, true);
    }

    /**
     * Deprecated since: 23.8.0. UCService is not in use anymore. Orig desc:
     * password for the user&#039;s UC service
     *
     * @param zimbraUCPassword new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1409)
    public void setUCPassword(String zimbraUCPassword) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraUCPassword, zimbraUCPassword);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Deprecated since: 23.8.0. UCService is not in use anymore. Orig desc:
     * username for the user&#039;s UC service
     *
     * @return zimbraUCUsername, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1408)
    public String getUCUsername() {
        return getAttr(ZAttrProvisioning.A_zimbraUCUsername, null, true);
    }

  /**
     * Whether virtual user set/changed his password after an external
     * virtual account for him is provisioned. This attribute is applicable
     * for accounts having zimbraIsExternalVirtualAccount set to TRUE.
     *
     * @return zimbraVirtualAccountInitialPasswordSet, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1414)
    public boolean isVirtualAccountInitialPasswordSet() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraVirtualAccountInitialPasswordSet, false, true);
    }

    /**
     * Whether virtual user set/changed his password after an external
     * virtual account for him is provisioned. This attribute is applicable
     * for accounts having zimbraIsExternalVirtualAccount set to TRUE.
     *
     * @param zimbraVirtualAccountInitialPasswordSet new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1414)
    public void setVirtualAccountInitialPasswordSet(boolean zimbraVirtualAccountInitialPasswordSet) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraVirtualAccountInitialPasswordSet, zimbraVirtualAccountInitialPasswordSet ? TRUE : FALSE);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * limit for the number of days that the web client would use to sync any
     * mail folder&#039;s data for offline use
     *
     * @return zimbraWebClientOfflineSyncMaxDays, or 30 if unset
     *
     * @since ZCS 8.5.0
     */
    @ZAttr(id=1452)
    public int getWebClientOfflineSyncMaxDays() {
        return getIntAttr(ZAttrProvisioning.A_zimbraWebClientOfflineSyncMaxDays, 30, true);
    }

  /**
     * User properties for Zimlets
     *
     * @return zimbraZimletUserProperties, or empty array if unset
     */
    @ZAttr(id=296)
    public String[] getZimletUserProperties() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraZimletUserProperties, true, true);
    }

  ///// END-AUTO-GEN-REPLACE

}
