// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.type.NamedValue;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-response-description Provides a limited amount of information about the requested
 *     account. <br>
 *     Note: there are some minor differences between the Admin and Account versions of
 *     GetAccountInfoResponse.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_ACCOUNT_INFO_RESPONSE)
@XmlType(
    propOrder = {
      "name",
      "attrs",
      "soapURL",
      "publicURL",
      "changePasswordURL",
      "adminURL",
      "boshURL"
    })
public class GetAccountInfoResponse {

  /**
   * @zm-api-field-tag account-email-address
   * @zm-api-field-description Account name - an email address (user@domain)
   */
  @ZimbraJsonAttribute
  @XmlElement(name = AccountConstants.A_NAME /* name */, required = true)
  private final String name;

  /**
   * @zm-api-field-description Account attributes. Currently only these attributes are returned:
   *     <table>
   * <tr><td> <b>zimbraId</b>       </td><td> the unique UUID of the zimbra account </td></tr>
   * <tr><td> <b>zimbraMailHost</b> </td><td> the server on which this user's mail resides </td></tr>
   * <tr><td> <b>displayName</b>    </td><td> display name for the account </td></tr>
   * </table>
   */
  @ZimbraKeyValuePairs
  @XmlElement(name = AccountConstants.E_ATTR /* attr */, required = true)
  private List<NamedValue> attrs = Lists.newArrayList();

  /**
   * @zm-api-field-description URL to talk to for soap service for this account. e.g.:
   *     <pre>
   *     http://server:7070/service/soap/
   * </pre>
   *     <p>If both http and https (SSL) are enabled, the https URL will be returned.
   */
  @XmlElement(name = AccountConstants.E_SOAP_URL /* soapURL */, required = false)
  @ZimbraJsonAttribute
  private String soapURL;

  /**
   * @zm-api-field-tag account-base-public-url
   * @zm-api-field-description Base public URL for the requested account
   */
  @XmlElement(name = AccountConstants.E_PUBLIC_URL /* publicURL */, required = false)
  @ZimbraJsonAttribute
  private String publicURL;

  /**
   * @zm-api-field-tag change-password-url
   * @zm-api-field-description URL to talk to in order to change a password. Not returned if not
   *     configured via domain attribute <b>zimbraChangePasswordURL</b>
   */
  @XmlElement(
      name = AccountConstants.E_CHANGE_PASSWORD_URL /* changePasswordURL */,
      required = false)
  @ZimbraJsonAttribute
  private String changePasswordURL;

  /**
   * @zm-api-field-tag admin-base-public-url
   * @zm-api-field-description base URL for accessing the admin console
   */
  @XmlElement(name = AccountConstants.E_ADMIN_URL /* adminURL */, required = false)
  @ZimbraJsonAttribute
  private String adminURL;

  /**
   * @zm-api-field-tag bosh-url
   * @zm-api-field-description Proxy URL for accessing XMPP over BOSH. Should be returned only when
   *     zimbraFeatureChatEnabled is set to TRUE for Account/COS
   */
  @XmlElement(name = AccountConstants.E_BOSH_URL /* boshURL */, required = false)
  @ZimbraJsonAttribute
  private String boshURL;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetAccountInfoResponse() {
    this(null);
  }

  public GetAccountInfoResponse(String name) {
    this.name = name;
  }

  public void setAttrs(Iterable<NamedValue> attrs) {
    this.attrs.clear();
    if (attrs != null) {
      Iterables.addAll(this.attrs, attrs);
    }
  }

  public GetAccountInfoResponse addAttr(NamedValue attr) {
    this.attrs.add(attr);
    return this;
  }

  public void setSoapURL(String soapURL) {
    this.soapURL = soapURL;
  }

  public void setPublicURL(String publicURL) {
    this.publicURL = publicURL;
  }

  public void setChangePasswordURL(String changePasswordURL) {
    this.changePasswordURL = changePasswordURL;
  }

  public void setAdminURL(String adminURL) {
    this.adminURL = adminURL;
  }

  public void setBoshURL(String boshURL) {
    this.boshURL = boshURL;
  }

  public String getName() {
    return name;
  }

  public List<NamedValue> getAttrs() {
    return attrs;
  }

  public String getSoapURL() {
    return soapURL;
  }

  public String getPublicURL() {
    return publicURL;
  }

  public String getChangePasswordURL() {
    return changePasswordURL;
  }

  public String getAdminURL() {
    return adminURL;
  }

  public String getBoshURL() {
    return boshURL;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("name", name)
        .add("attrs", attrs)
        .add("soapURL", soapURL)
        .add("publicURL", publicURL)
        .add("changePasswordURL", changePasswordURL);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
