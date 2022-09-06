// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ImapDataSource;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAttribute;

public class MailImapDataSource extends MailDataSource implements ImapDataSource {

  /**
   * @zm-api-field-tag data-source-oauthToken
   * @zm-api-field-description oauthToken for data source
   */
  @XmlAttribute(name = MailConstants.A_DS_OAUTH_TOKEN /* oauthToken */, required = false)
  private String oauthToken;

  /**
   * @zm-api-field-tag data-source-clientId
   * @zm-api-field-description client Id for refreshing data source oauth token
   */
  @XmlAttribute(name = MailConstants.A_DS_CLIENT_ID /* clientId */, required = false)
  private String clientId;

  /**
   * @zm-api-field-tag data-source-clientSecret
   * @zm-api-field-description client secret for refreshing data source oauth token
   */
  @XmlAttribute(name = MailConstants.A_DS_CLIENT_SECRET /* clientSecret */, required = false)
  private String clientSecret;

  /**
   * @zm-api-field-tag test-data-source
   * @zm-api-field-description boolean field for client to denote if it wants to test the data
   *     source before creating
   */
  @XmlAttribute(name = MailConstants.A_DS_TEST, required = false)
  private ZmBoolean test;

  public MailImapDataSource() {}

  public MailImapDataSource(ImapDataSource data) {
    super(data);
    setOAuthToken(((MailImapDataSource) data).getOAuthToken());
    setClientId(((MailImapDataSource) data).getClientId());
    setClientSecret(((MailImapDataSource) data).getClientSecret());
  }

  public void setOAuthToken(String oauthToken) {
    this.oauthToken = oauthToken;
  }

  public String getOAuthToken() {
    return oauthToken;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setTest(boolean test) {
    this.test = ZmBoolean.fromBool(test, false);
  }

  public boolean isTest() {
    return ZmBoolean.toBool(test, false);
  }

  @Override
  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper
        .add("oauthToken", oauthToken)
        .add("clientId", clientId)
        .add("clientSecret", clientSecret)
        .add("refreshToken", this.getRefreshToken())
        .add("refreshTokenUrl", this.getRefreshTokenUrl());
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
