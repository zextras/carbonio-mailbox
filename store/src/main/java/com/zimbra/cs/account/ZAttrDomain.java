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
import com.zimbra.cs.ldap.LdapDateUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** AUTO-GENERATED. DO NOT EDIT. */
public abstract class ZAttrDomain extends NamedEntry {

  protected ZAttrDomain(
      String name,
      String id,
      Map<String, Object> attrs,
      Map<String, Object> defaults,
      Provisioning prov) {
    super(name, id, attrs, defaults, prov);
  }

  ///// BEGIN-AUTO-GEN-REPLACE

  /**
     * AD/LDAP timestamp format. For example:
     * yyyyMMddHHmmss.SSS&#039;Z&#039;, yyyyMMddHHmmss&#039;Z&#039;
     *
     * @return carbonioAutoProvTimestampFormat, or "yyyyMMddHHmmss.SSS'Z'" if unset
     *
     * @since ZCS 23.12.0
     */
    @ZAttr(id=3141)
    public String getCarbonioAutoProvTimestampFormat() {
        return getAttr(ZAttrProvisioning.A_carbonioAutoProvTimestampFormat, "yyyyMMddHHmmss.SSS'Z'", true);
    }

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
     * last calculated aggregate quota usage for the domain in bytes
     *
     * @return zimbraAggregateQuotaLastUsage, or -1 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1328)
    public long getAggregateQuotaLastUsage() {
        return getLongAttr(ZAttrProvisioning.A_zimbraAggregateQuotaLastUsage, -1L, true);
    }

    /**
     * last calculated aggregate quota usage for the domain in bytes
     *
     * @param zimbraAggregateQuotaLastUsage new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1328)
    public void setAggregateQuotaLastUsage(long zimbraAggregateQuotaLastUsage) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAggregateQuotaLastUsage, Long.toString(zimbraAggregateQuotaLastUsage));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * LDAP bind dn for ldap auth mech
     *
     * @return zimbraAuthLdapBindDn, or null if unset
     */
    @ZAttr(id=44)
    public String getAuthLdapBindDn() {
        return getAttr(ZAttrProvisioning.A_zimbraAuthLdapBindDn, null, true);
    }

  /**
     * LDAP search bind dn for ldap auth mech
     *
     * @return zimbraAuthLdapSearchBindDn, or null if unset
     */
    @ZAttr(id=253)
    public String getAuthLdapSearchBindDn() {
        return getAttr(ZAttrProvisioning.A_zimbraAuthLdapSearchBindDn, null, true);
    }

  /**
     * LDAP search bind password for ldap auth mech
     *
     * @return zimbraAuthLdapSearchBindPassword, or null if unset
     */
    @ZAttr(id=254)
    public String getAuthLdapSearchBindPassword() {
        return getAttr(ZAttrProvisioning.A_zimbraAuthLdapSearchBindPassword, null, true);
    }

  /**
     * whether to use startTLS for external LDAP auth
     *
     * @return zimbraAuthLdapStartTlsEnabled, or false if unset
     *
     * @since ZCS 5.0.6
     */
    @ZAttr(id=654)
    public boolean isAuthLdapStartTlsEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAuthLdapStartTlsEnabled, false, true);
    }

  /**
     * LDAP URL for ldap auth mech
     *
     * @return zimbraAuthLdapURL, or empty array if unset
     */
    @ZAttr(id=43)
    public String[] getAuthLdapURL() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraAuthLdapURL, true, true);
    }

  /**
     * mechanism to use for verifying password. Valid values are zimbra,
     * ldap, ad, kerberos5, custom:{handler-name} [arg1 arg2 ...]
     *
     * @return zimbraAuthMech, or null if unset
     */
    @ZAttr(id=42)
    public String getAuthMech() {
        return getAttr(ZAttrProvisioning.A_zimbraAuthMech, null, true);
    }

  /**
     * mechanism to use for verifying password for admin. See zimbraAuthMech
     *
     * @return zimbraAuthMechAdmin, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1252)
    public String getAuthMechAdmin() {
        return getAttr(ZAttrProvisioning.A_zimbraAuthMechAdmin, null, true);
    }

  /**
     * EAGER mode: optional LAZY mode: optional MANUAL mode: optional
     * Attribute name in the external directory that contains localpart of
     * the account name. If not specified, localpart of the account name is
     * the principal user used to authenticated to Zimbra.
     *
     * @return zimbraAutoProvAccountNameMap, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1230)
    public String getAutoProvAccountNameMap() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvAccountNameMap, null, true);
    }

  /**
     * EAGER mode: optional LAZY mode: optional MANUAL mode: optional
     * Attribute map for mapping attribute values from the external entry to
     * Zimbra account attributes. Values are in the format of {external
     * attribute}={zimbra attribute}. If not set, no attributes from the
     * external directory will be populated in Zimbra directory. Invalid
     * mapping configuration will cause the account creation to fail.
     * Examples of bad mapping: - invalid external attribute name. - invalid
     * Zimbra attribute name. - external attribute has multiple values but
     * the zimbra attribute is single-valued. - syntax violation. e.g. Value
     * on the external attribute is a String but the Zimbra attribute is
     * declared an integer.
     *
     * @return zimbraAutoProvAttrMap, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1231)
    public String[] getAutoProvAttrMap() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraAutoProvAttrMap, true, true);
    }

  /**
     * EAGER mode: required LAZY mode: N/A MANUAL mode: N/A Max number of
     * accounts to process in each interval for EAGER auto provision.
     *
     * @return zimbraAutoProvBatchSize, or 20 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1234)
    public int getAutoProvBatchSize() {
        return getIntAttr(ZAttrProvisioning.A_zimbraAutoProvBatchSize, 20, true);
    }

    /**
     * EAGER mode: required LAZY mode: N/A MANUAL mode: N/A Max number of
     * accounts to process in each interval for EAGER auto provision.
     *
     * @param zimbraAutoProvBatchSize new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1234)
    public void setAutoProvBatchSize(int zimbraAutoProvBatchSize) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAutoProvBatchSize, Integer.toString(zimbraAutoProvBatchSize));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * EAGER mode: for Zimbra internal use only - do not change it. LAZY
     * mode: N/A MANUAL mode: N/A Timestamp when the external domain is last
     * polled for EAGER auto provision. The poll (LDAP search) for the next
     * iteration will fetch external entries with create timestamp later than
     * the timestamp recorded from the previous iteration.
     *
     * @return zimbraAutoProvLastPolledTimestamp, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1235)
    public String getAutoProvLastPolledTimestampAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLastPolledTimestamp, null, true);
    }

  /**
     * EAGER mode: for Zimbra internal use only - do not change it. LAZY
     * mode: N/A MANUAL mode: N/A Timestamp when the external domain is last
     * polled for EAGER auto provision. The poll (LDAP search) for the next
     * iteration will fetch external entries with create timestamp later than
     * the timestamp recorded from the previous iteration.
     *
     * @param zimbraAutoProvLastPolledTimestamp new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1235)
    public void setAutoProvLastPolledTimestampAsString(String zimbraAutoProvLastPolledTimestamp) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraAutoProvLastPolledTimestamp, zimbraAutoProvLastPolledTimestamp);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * EAGER mode: required LAZY mode: required (if using
     * zimbraAutoProvLdapSearchFilter) MANUAL mode: required LDAP search bind
     * DN for auto provision.
     *
     * @return zimbraAutoProvLdapAdminBindDn, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1225)
    public String getAutoProvLdapAdminBindDn() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLdapAdminBindDn, null, true);
    }

  /**
     * EAGER mode: required LAZY mode: required MANUAL mode: required LDAP
     * search bind password for auto provision.
     *
     * @return zimbraAutoProvLdapAdminBindPassword, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1226)
    public String getAutoProvLdapAdminBindPassword() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLdapAdminBindPassword, null, true);
    }

  /**
     * EAGER mode: required LAZY mode: optional (if not using
     * zimbraAutoProvLdapSearchFilter) MANUAL mode: optional (if not using
     * zimbraAutoProvLdapSearchFilter) LDAP external DN template for account
     * auto provisioning. For LAZY and MANUAL modes, either
     * zimbraAutoProvLdapSearchFilter or zimbraAutoProvLdapBindDn has to be
     * set. If both are set, zimbraAutoProvLdapSearchFilter will take
     * precedence. Supported place holders: %n = username with @ (or without,
     * if no @ was specified) %u = username with @ removed %d = domain as
     * foo.com %D = domain as dc=foo,dc=com
     *
     * @return zimbraAutoProvLdapBindDn, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1229)
    public String getAutoProvLdapBindDn() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLdapBindDn, null, true);
    }

  /**
     * EAGER mode: required LAZY mode: required (if using
     * zimbraAutoProvLdapSearchFilter), MANUAL mode: required LDAP search
     * base for auto provision, used in conjunction with
     * zimbraAutoProvLdapSearchFilter. If not set, LDAP root DSE will be
     * used.
     *
     * @return zimbraAutoProvLdapSearchBase, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1227)
    public String getAutoProvLdapSearchBase() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLdapSearchBase, null, true);
    }

  /**
     * EAGER mode: required LAZY mode: optional (if not using
     * zimbraAutoProvLdapBindDn) MANUAL mode: optional (if not using
     * zimbraAutoProvLdapBindDn) LDAP search filter template for account auto
     * provisioning. For LAZY and MANUAL modes, either
     * zimbraAutoProvLdapSearchFilter or zimbraAutoProvLdapBindDn has to be
     * set. If both are set, zimbraAutoProvLdapSearchFilter will take
     * precedence. Supported place holders: %n = username with @ (or without,
     * if no @ was specified) %u = username with @ removed %d = domain as
     * foo.com %D = domain as dc=foo,dc=com
     *
     * @return zimbraAutoProvLdapSearchFilter, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1228)
    public String getAutoProvLdapSearchFilter() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLdapSearchFilter, null, true);
    }

  /**
     * EAGER mode: optional LAZY mode: optional MANUAL mode: optional Default
     * is FALSE. Whether to use startTLS when accessing the external LDAP
     * server for auto provision.
     *
     * @return zimbraAutoProvLdapStartTlsEnabled, or false if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1224)
    public boolean isAutoProvLdapStartTlsEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraAutoProvLdapStartTlsEnabled, false, true);
    }

  /**
     * EAGER mode: required LAZY mode: required MANUAL mode: required LDAP
     * URL of the external LDAP source for auto provision.
     *
     * @return zimbraAutoProvLdapURL, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1223)
    public String getAutoProvLdapURL() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvLdapURL, null, true);
    }

  /**
     * EAGER mode: optional LAZY mode: optional MANUAL mode: optional Class
     * name of auto provision listener. The class must implement the
     * com.zimbra.cs.account.Account.AutoProvisionListener interface. The
     * singleton listener instance is invoked after each account is auto
     * created in Zimbra. Listener can be plugged in as a server extension to
     * handle tasks like updating the account auto provision status in the
     * external LDAP directory. At each eager provision interval, ZCS does an
     * LDAP search based on the value configured in
     * zimbraAutoProvLdapSearchFilter. Returned entries from this search are
     * candidates to be auto provisioned in this batch. The
     * zimbraAutoProvLdapSearchFilter should include an assertion that will
     * only hit entries in the external directory that have not yet been
     * provisioned in ZCS, otherwise it&#039;s likely the same entries will
     * be repeated pulled in to ZCS. After an account is auto provisioned in
     * ZCS,
     * com.zimbra.cs.account.Account.AutoProvisionListener.postCreate(Domain
     * domain, Account acct, String externalDN) will be called by the auto
     * provisioning framework. Customer can implement the
     * AutoProvisionListener interface in a ZCS server extension and get
     * their AutoProvisionListener.postCreate() get called. The
     * implementation of customer&#039;s postCreate method can be, for
     * example, setting an attribute in the external directory on the account
     * just provisioned in ZCS. The attribute can be included as a condition
     * in the zimbraAutoProvLdapSearchFilter, so the entry won&#039;t be
     * returned again by the LDAP search in the next interval.
     *
     * @return zimbraAutoProvListenerClass, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1233)
    public String getAutoProvListenerClass() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvListenerClass, null, true);
    }

  /**
     * Template used to construct the subject of the notification message
     * sent to the user when the user&#039;s account is auto provisioned.
     * Supported variables: ${ACCOUNT_ADDRESS}, ${ACCOUNT_DISPLAY_NAME}
     *
     * @return zimbraAutoProvNotificationBody, or "Your account has been auto provisioned.  Your email address is ${ACCOUNT_ADDRESS}." if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1357)
    public String getAutoProvNotificationBody() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvNotificationBody, "Your account has been auto provisioned.  Your email address is ${ACCOUNT_ADDRESS}.", true);
    }

  /**
     * EAGER mode: optional LAZY mode: optional MANUAL mode: optional Email
     * address to put in the From header for the notification email to the
     * newly created account. If not set, no notification email will sent to
     * the newly created account.
     *
     * @return zimbraAutoProvNotificationFromAddress, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1232)
    public String getAutoProvNotificationFromAddress() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvNotificationFromAddress, null, true);
    }

  /**
     * Template used to construct the subject of the notification message
     * sent to the user when the user&#039;s account is auto provisioned.
     * Supported variables: ${ACCOUNT_ADDRESS}, ${ACCOUNT_DISPLAY_NAME}
     *
     * @return zimbraAutoProvNotificationSubject, or "New account auto provisioned" if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1356)
    public String getAutoProvNotificationSubject() {
        return getAttr(ZAttrProvisioning.A_zimbraAutoProvNotificationSubject, "New account auto provisioned", true);
    }

  /**
     * Realm for the basic auth challenge (WWW-Authenticate) header
     *
     * @return zimbraBasicAuthRealm, or "Carbonio" if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1098)
    public String getBasicAuthRealm() {
        return getAttr(ZAttrProvisioning.A_zimbraBasicAuthRealm, "Carbonio", true);
    }

  /**
     * maximum aggregate quota for the domain in bytes
     *
     * @return zimbraDomainAggregateQuota, or 0 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1327)
    public long getDomainAggregateQuota() {
        return getLongAttr(ZAttrProvisioning.A_zimbraDomainAggregateQuota, 0L, true);
    }

  /**
     * policy for a domain whose quota usage is above
     * zimbraDomainAggregateQuota
     *
     * <p>Valid values: [ALLOWSENDRECEIVE, BLOCKSEND, BLOCKSENDRECEIVE]
     *
     * @return zimbraDomainAggregateQuotaPolicy, or ZAttrProvisioning.DomainAggregateQuotaPolicy.ALLOWSENDRECEIVE if unset and/or has invalid value
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1329)
    public ZAttrProvisioning.DomainAggregateQuotaPolicy getDomainAggregateQuotaPolicy() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraDomainAggregateQuotaPolicy, true, true); return v == null ? ZAttrProvisioning.DomainAggregateQuotaPolicy.ALLOWSENDRECEIVE : ZAttrProvisioning.DomainAggregateQuotaPolicy.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return ZAttrProvisioning.DomainAggregateQuotaPolicy.ALLOWSENDRECEIVE; }
    }

  /**
     * email recipients to be notified when zimbraAggregateQuotaLastUsage
     * reaches zimbraDomainAggregateQuotaWarnPercent of the
     * zimbraDomainAggregateQuota
     *
     * @return zimbraDomainAggregateQuotaWarnEmailRecipient, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1331)
    public String[] getDomainAggregateQuotaWarnEmailRecipient() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraDomainAggregateQuotaWarnEmailRecipient, true, true);
    }

  /**
     * percentage threshold for domain aggregate quota warnings
     *
     * @return zimbraDomainAggregateQuotaWarnPercent, or 80 if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1330)
    public int getDomainAggregateQuotaWarnPercent() {
        return getIntAttr(ZAttrProvisioning.A_zimbraDomainAggregateQuotaWarnPercent, 80, true);
    }

  /**
     * COS zimbraID
     *
     * @return zimbraDomainDefaultCOSId, or null if unset
     */
    @ZAttr(id=299)
    public String getDomainDefaultCOSId() {
        return getAttr(ZAttrProvisioning.A_zimbraDomainDefaultCOSId, null, true);
    }

  /**
     * id of the default COS for external user accounts
     *
     * @return zimbraDomainDefaultExternalUserCOSId, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1247)
    public String getDomainDefaultExternalUserCOSId() {
        return getAttr(ZAttrProvisioning.A_zimbraDomainDefaultExternalUserCOSId, null, true);
    }

  /**
     * name of the domain
     *
     * @return zimbraDomainName, or null if unset
     */
    @ZAttr(id=19)
    public String getDomainName() {
        return getAttr(ZAttrProvisioning.A_zimbraDomainName, null, true);
    }

    /**
     * name of the domain
     *
     * @param zimbraDomainName new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=19)
    public void setDomainName(String zimbraDomainName) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraDomainName, zimbraDomainName);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * domain status. enum values are akin to those of zimbraAccountStatus
     * but the status affects all accounts on the domain. See table below for
     * how zimbraDomainStatus affects account status. active - see
     * zimbraAccountStatus maintenance - see zimbraAccountStatus locked - see
     * zimbraAccountStatus closed - see zimbraAccountStatus suspended -
     * maintenance + no creating/deleting/modifying accounts/DLs under the
     * domain. shutdown - suspended + cannot modify domain attrs + cannot
     * delete the domain Indicating server is doing major and lengthy
     * maintenance work on the domain, e.g. renaming the domain and moving
     * LDAP entries. Modification and deletion of the domain can only be done
     * internally by the server when it is safe to release the domain, they
     * cannot be done in admin console or zmprov. How zimbraDomainStatus
     * affects account behavior : -------------------------------------
     * zimbraDomainStatus account behavior
     * ------------------------------------- active zimbraAccountStatus
     * locked zimbraAccountStatus if it is maintenance or pending or closed,
     * else locked maintenance zimbraAccountStatus if it is pending or
     * closed, else maintenance suspended zimbraAccountStatus if it is
     * pending or closed, else maintenance shutdown zimbraAccountStatus if it
     * is pending or closed, else maintenance closed closed
     *
     * <p>Valid values: [active, maintenance, locked, closed, suspended, shutdown]
     *
     * @return zimbraDomainStatus, or null if unset and/or has invalid value
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=535)
    public ZAttrProvisioning.DomainStatus getDomainStatus() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraDomainStatus, true, true); return v == null ? null : ZAttrProvisioning.DomainStatus.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return null; }
    }

    /**
     * domain status. enum values are akin to those of zimbraAccountStatus
     * but the status affects all accounts on the domain. See table below for
     * how zimbraDomainStatus affects account status. active - see
     * zimbraAccountStatus maintenance - see zimbraAccountStatus locked - see
     * zimbraAccountStatus closed - see zimbraAccountStatus suspended -
     * maintenance + no creating/deleting/modifying accounts/DLs under the
     * domain. shutdown - suspended + cannot modify domain attrs + cannot
     * delete the domain Indicating server is doing major and lengthy
     * maintenance work on the domain, e.g. renaming the domain and moving
     * LDAP entries. Modification and deletion of the domain can only be done
     * internally by the server when it is safe to release the domain, they
     * cannot be done in admin console or zmprov. How zimbraDomainStatus
     * affects account behavior : -------------------------------------
     * zimbraDomainStatus account behavior
     * ------------------------------------- active zimbraAccountStatus
     * locked zimbraAccountStatus if it is maintenance or pending or closed,
     * else locked maintenance zimbraAccountStatus if it is pending or
     * closed, else maintenance suspended zimbraAccountStatus if it is
     * pending or closed, else maintenance shutdown zimbraAccountStatus if it
     * is pending or closed, else maintenance closed closed
     *
     * <p>Valid values: [active, maintenance, locked, closed, suspended, shutdown]
     *
     * @return zimbraDomainStatus, or null if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=535)
    public String getDomainStatusAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraDomainStatus, null, true);
    }

  /**
     * should be one of: local, alias
     *
     * <p>Valid values: [local, alias]
     *
     * @return zimbraDomainType, or null if unset and/or has invalid value
     */
    @ZAttr(id=212)
    public ZAttrProvisioning.DomainType getDomainType() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraDomainType, true, true); return v == null ? null : ZAttrProvisioning.DomainType.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return null; }
    }

  /**
     * the handler class for getting all groups an account belongs to in the
     * external directory
     *
     * @return zimbraExternalGroupHandlerClass, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1251)
    public String getExternalGroupHandlerClass() {
        return getAttr(ZAttrProvisioning.A_zimbraExternalGroupHandlerClass, null, true);
    }

  /**
     * LDAP search base for searching external LDAP groups
     *
     * @return zimbraExternalGroupLdapSearchBase, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1249)
    public String getExternalGroupLdapSearchBase() {
        return getAttr(ZAttrProvisioning.A_zimbraExternalGroupLdapSearchBase, null, true);
    }

  /**
     * LDAP search filter for searching external LDAP groups
     *
     * @return zimbraExternalGroupLdapSearchFilter, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1250)
    public String getExternalGroupLdapSearchFilter() {
        return getAttr(ZAttrProvisioning.A_zimbraExternalGroupLdapSearchFilter, null, true);
    }

  /**
     * Duration for which the URL sent in the share invitation email to an
     * external user is valid. A value of 0 indicates that the URL never
     * expires. . Must be in valid duration format: {digits}{time-unit}.
     * digits: 0-9, time-unit: [hmsd]|ms. h - hours, m - minutes, s -
     * seconds, d - days, ms - milliseconds. If time unit is not specified,
     * the default is s(seconds).
     *
     * <p>Use getExternalShareInvitationUrlExpirationAsString to access value as a string.
     *
     * @see #getExternalShareInvitationUrlExpirationAsString()
     *
     * @return zimbraExternalShareInvitationUrlExpiration in millseconds, or 0 (0)  if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1349)
    public long getExternalShareInvitationUrlExpiration() {
        return getTimeInterval(ZAttrProvisioning.A_zimbraExternalShareInvitationUrlExpiration, 0L, true);
    }

  /**
     * status of password reset feature
     *
     * <p>Valid values: [enabled, suspended, disabled]
     *
     * @return zimbraFeatureResetPasswordStatus, or null if unset
     *
     * @since ZCS 8.8.9
     */
    @ZAttr(id=2134)
    public String getFeatureResetPasswordStatusAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraFeatureResetPasswordStatus, null, true);
    }

  /**
     * foreign name for mapping an external name to a zimbra domain on domain
     * level, it is in the format of {application}:{foreign name}
     *
     * @param zimbraForeignName new to add to existing values
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1135)
    public void addForeignName(String zimbraForeignName) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        StringUtil.addToMultiMap(attrs, "+"  + ZAttrProvisioning.A_zimbraForeignName, zimbraForeignName);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * handler for foreign name mapping, it is in the format of
     * {application}:{class name}[:{params}]
     *
     * @return zimbraForeignNameHandler, or empty array if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1136)
    public String[] getForeignNameHandler() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraForeignNameHandler, true, true);
    }

  /**
     * handler for foreign name mapping, it is in the format of
     * {application}:{class name}[:{params}]
     *
     * @param zimbraForeignNameHandler new to add to existing values
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1136)
    public void addForeignNameHandler(String zimbraForeignNameHandler) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        StringUtil.addToMultiMap(attrs, "+"  + ZAttrProvisioning.A_zimbraForeignNameHandler, zimbraForeignNameHandler);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * zimbraId of GAL sync accounts
     *
     * @return zimbraGalAccountId, or empty array if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=831)
    public String[] getGalAccountId() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraGalAccountId, true, true);
    }

    /**
     * zimbraId of GAL sync accounts
     *
     * @param zimbraGalAccountId new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=831)
    public void setGalAccountId(String[] zimbraGalAccountId) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraGalAccountId, zimbraGalAccountId);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * zimbraId of GAL sync accounts
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=831)
    public void unsetGalAccountId() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraGalAccountId, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * When set to TRUE, GAL search will always include local calendar
     * resources regardless of zimbraGalMode.
     *
     * @return zimbraGalAlwaysIncludeLocalCalendarResources, or false if unset
     *
     * @since ZCS 6.0.7
     */
    @ZAttr(id=1093)
    public boolean isGalAlwaysIncludeLocalCalendarResources() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraGalAlwaysIncludeLocalCalendarResources, false, true);
    }

  /**
     * the time at which GAL definition is last modified.
     *
     * @return zimbraGalDefinitionLastModifiedTime, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1413)
    public String getGalDefinitionLastModifiedTimeAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraGalDefinitionLastModifiedTime, null, true);
    }

    /**
     * the time at which GAL definition is last modified.
     *
     * @param zimbraGalDefinitionLastModifiedTime new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1413)
    public void setGalDefinitionLastModifiedTime(Date zimbraGalDefinitionLastModifiedTime) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraGalDefinitionLastModifiedTime, zimbraGalDefinitionLastModifiedTime==null ? "" : LdapDateUtil.toGeneralizedTime(zimbraGalDefinitionLastModifiedTime));
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * whether to indicate if an email address on a message is a GAL group
     *
     * @return zimbraGalGroupIndicatorEnabled, or true if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1153)
    public boolean isGalGroupIndicatorEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraGalGroupIndicatorEnabled, true, true);
    }

  /**
     * maximum number of gal entries to return from a search
     *
     * @return zimbraGalMaxResults, or 100 if unset
     */
    @ZAttr(id=53)
    public int getGalMaxResults() {
        return getIntAttr(ZAttrProvisioning.A_zimbraGalMaxResults, 100, true);
    }

  /**
     * valid modes are &quot;zimbra&quot; (query internal directory only),
     * &quot;ldap&quot; (query external directory only), or &quot;both&quot;
     * (query internal and external directory)
     *
     * <p>Valid values: [zimbra, both, ldap]
     *
     * @return zimbraGalMode, or null if unset and/or has invalid value
     */
    @ZAttr(id=46)
    public ZAttrProvisioning.GalMode getGalMode() {
        try { String v = getAttr(ZAttrProvisioning.A_zimbraGalMode, true, true); return v == null ? null : ZAttrProvisioning.GalMode.fromString(v); } catch(com.zimbra.common.service.ServiceException e) { return null; }
    }

  /**
     * Maximum number of concurrent GAL sync requests allowed on the system /
     * domain.
     *
     * @return zimbraGalSyncMaxConcurrentClients, or 2 if unset
     *
     * @since ZCS 7.0.0
     */
    @ZAttr(id=1154)
    public int getGalSyncMaxConcurrentClients() {
        return getIntAttr(ZAttrProvisioning.A_zimbraGalSyncMaxConcurrentClients, 2, true);
    }

  /**
     * Page size control for SyncGalRequest. By default not more than 30000
     * entries will be returned for every SyncGalRequest
     *
     * @return zimbraGalSyncSizeLimit, or 30000 if unset
     *
     * @since ZCS 8.7.2
     */
    @ZAttr(id=2097)
    public int getGalSyncSizeLimit() {
        return getIntAttr(ZAttrProvisioning.A_zimbraGalSyncSizeLimit, 30000, true);
    }

  /**
     * whether to tokenize key and AND or OR the tokenized queries for GAL
     * search, if not set, key is not tokenized
     *
     * <p>Valid values: [and, or]
     *
     * @return zimbraGalTokenizeSearchKey, or "and" if unset
     *
     * @since ZCS 5.0.2
     */
    @ZAttr(id=600)
    public String getGalTokenizeSearchKeyAsString() {
        return getAttr(ZAttrProvisioning.A_zimbraGalTokenizeSearchKey, "and", true);
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
     * whether sharing with accounts and groups of all other domains hosted
     * on this deployment be considered internal sharing
     *
     * @return zimbraInternalSharingCrossDomainEnabled, or true if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1386)
    public boolean isInternalSharingCrossDomainEnabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraInternalSharingCrossDomainEnabled, true, true);
    }

  /**
     * Domains hosted on this deployment, accounts and groups of which are
     * considered internal during sharing. Applicable when
     * zimbraInternalSharingCrossDomainEnabled is set to FALSE.
     *
     * @return zimbraInternalSharingDomain, or empty array if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1387)
    public String[] getInternalSharingDomain() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraInternalSharingDomain, true, true);
    }

  /**
     * whether ldap based galsync disabled or not
     *
     * @return zimbraLdapGalSyncDisabled, or false if unset
     *
     * @since ZCS 7.2.2
     */
    @ZAttr(id=1420)
    public boolean isLdapGalSyncDisabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraLdapGalSyncDisabled, false, true);
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
        return getMultiAttr(ZAttrProvisioning.A_zimbraPrefSpellIgnoreWord, true, true);
    }

  /**
     * Name to be used in public API such as REST or SOAP proxy.
     *
     * @return zimbraPublicServiceHostname, or null if unset
     */
    @ZAttr(id=377)
    public String getPublicServiceHostname() {
        return getAttr(ZAttrProvisioning.A_zimbraPublicServiceHostname, null, true);
    }

    /**
     * Name to be used in public API such as REST or SOAP proxy.
     *
     * @param zimbraPublicServiceHostname new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     */
    @ZAttr(id=377)
    public void setPublicServiceHostname(String zimbraPublicServiceHostname) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraPublicServiceHostname, zimbraPublicServiceHostname);
        getProvisioning().modifyAttrs(this, attrs);
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
     * background color for chameleon skin for the domain
     *
     * @return zimbraSkinBackgroundColor, or null if unset
     *
     * @since ZCS 5.0.6
     */
    @ZAttr(id=648)
    public String getSkinBackgroundColor() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinBackgroundColor, null, true);
    }

  /**
     * favicon for chameleon skin for the domain
     *
     * @return zimbraSkinFavicon, or null if unset
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=800)
    public String getSkinFavicon() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinFavicon, null, true);
    }

  /**
     * foreground color for chameleon skin for the domain
     *
     * @return zimbraSkinForegroundColor, or null if unset
     *
     * @since ZCS 5.0.6
     */
    @ZAttr(id=647)
    public String getSkinForegroundColor() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinForegroundColor, null, true);
    }

  /**
     * logo app banner for chameleon skin for the domain
     *
     * @return zimbraSkinLogoAppBanner, or null if unset
     *
     * @since ZCS 5.0.7
     */
    @ZAttr(id=671)
    public String getSkinLogoAppBanner() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinLogoAppBanner, null, true);
    }

  /**
     * logo login banner for chameleon skin for the domain
     *
     * @return zimbraSkinLogoLoginBanner, or null if unset
     *
     * @since ZCS 5.0.7
     */
    @ZAttr(id=670)
    public String getSkinLogoLoginBanner() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinLogoLoginBanner, null, true);
    }

  /**
     * Logo URL for chameleon skin for the domain
     *
     * @return zimbraSkinLogoURL, or null if unset
     *
     * @since ZCS 5.0.6
     */
    @ZAttr(id=649)
    public String getSkinLogoURL() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinLogoURL, null, true);
    }

  /**
     * secondary color for chameleon skin for the domain
     *
     * @return zimbraSkinSecondaryColor, or null if unset
     *
     * @since ZCS 5.0.7
     */
    @ZAttr(id=668)
    public String getSkinSecondaryColor() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinSecondaryColor, null, true);
    }

  /**
     * selection color for chameleon skin for the domain
     *
     * @return zimbraSkinSelectionColor, or null if unset
     *
     * @since ZCS 5.0.7
     */
    @ZAttr(id=669)
    public String getSkinSelectionColor() {
        return getAttr(ZAttrProvisioning.A_zimbraSkinSelectionColor, null, true);
    }

  /**
     * the SMTP server to connect to when sending mail
     *
     * @return zimbraSmtpHostname, or empty array if unset
     */
    @ZAttr(id=97)
    public String[] getSmtpHostname() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraSmtpHostname, true, true);
    }

  /**
     * An alias for this domain, used to determine default login domain based
     * on URL client is visiting
     *
     * @return zimbraVirtualHostname, or empty array if unset
     */
    @ZAttr(id=352)
    public String[] getVirtualHostname() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraVirtualHostname, true, true);
    }

  /**
     * Virtual IP address for this domain, used to determine domain based on
     * an IP address and have IP-based virtual hosts for the proxy. Consider
     * using zimbraReverseProxySNIEnabled instead when using SNI capable
     * clients
     *
     * @return zimbraVirtualIPAddress, or empty array if unset
     *
     * @since ZCS 5.0.0
     */
    @ZAttr(id=562)
    public String[] getVirtualIPAddress() {
        return getMultiAttr(ZAttrProvisioning.A_zimbraVirtualIPAddress, true, true);
    }

  /**
     * link for admin users in web client
     *
     * @return zimbraWebClientAdminReference, or null if unset
     *
     * @since ZCS 5.0.9
     */
    @ZAttr(id=701)
    public String getWebClientAdminReference() {
        return getAttr(ZAttrProvisioning.A_zimbraWebClientAdminReference, null, true);
    }

  /**
     * login URL for web client to send the user to upon failed login, auth
     * expired, or no/invalid auth
     *
     * @return zimbraWebClientLoginURL, or null if unset
     */
    @ZAttr(id=506)
    public String getWebClientLoginURL() {
        return getAttr(ZAttrProvisioning.A_zimbraWebClientLoginURL, null, true);
    }

  /**
     * logout URL for web client to send the user to upon explicit logging
     * out
     *
     * @return zimbraWebClientLogoutURL, or null if unset
     */
    @ZAttr(id=507)
    public String getWebClientLogoutURL() {
        return getAttr(ZAttrProvisioning.A_zimbraWebClientLogoutURL, null, true);
    }

  /**
     * Whether the Stay Signed In checkbox should be disabled on the login
     * screen
     *
     * @return zimbraWebClientStaySignedInDisabled, or false if unset
     *
     * @since ZCS 8.7.0
     */
    @ZAttr(id=1687)
    public boolean isWebClientStaySignedInDisabled() {
        return getBooleanAttr(ZAttrProvisioning.A_zimbraWebClientStaySignedInDisabled, false, true);
    }

  ///// END-AUTO-GEN-REPLACE

}
