// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(propOrder = {})
@GraphQLType(name = GqlConstants.CLASS_SIGNATURE_CONTENT, description = "Content of the signature")
public class SignatureContent {

  /**
   * @zm-api-field-tag signature-content-type
   * @zm-api-field-description Content Type - <b>"text/plain"</b> or <b>"text/html"</b>
   */
  @XmlAttribute(name = AccountConstants.A_TYPE)
  private String contentType;

  /**
   * @zm-api-field-tag signature-value
   * @zm-api-field-description Signature value
   */
  @XmlValue private String content;

  public SignatureContent() {}

  public SignatureContent(String content, String contentType) {
    this.content = content;
    this.contentType = contentType;
  }

  @GraphQLQuery(
      name = GqlConstants.CONTENT_TYPE,
      description = "Content Type - \"text/plain\" or \"text/html\"")
  public String getContentType() {
    return contentType;
  }

  @GraphQLInputField(
      name = GqlConstants.CONTENT_TYPE,
      description = "Content Type - \"text/plain\" or \"text/html\"")
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @GraphQLQuery(name = GqlConstants.CONTENT, description = "Signature value")
  public String getContent() {
    return content;
  }

  @GraphQLInputField(name = GqlConstants.CONTENT, description = "Signature value")
  public void setContent(String content) {
    this.content = content;
  }
}
