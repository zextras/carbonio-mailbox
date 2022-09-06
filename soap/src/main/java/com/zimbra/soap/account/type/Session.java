// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.HeaderConstants;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

// See ZimbraSoapContext.encodeSession:
// eSession.addAttribute(HeaderConstants.A_TYPE, typeStr).addAttribute(HeaderConstants.A_ID,
// sessionId).setText(sessionId);

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name = "Session", description = "A session")
public class Session {

  /**
   * @zm-api-field-tag session-type
   * @zm-api-field-description Session type - currently only set if value is "admin"
   */
  @XmlAttribute(name = HeaderConstants.A_TYPE, required = false)
  @GraphQLQuery(name = "type", description = "Session type - currently only set if value is admin")
  private String type;

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description Session ID
   */
  @XmlAttribute(name = HeaderConstants.A_ID, required = true)
  @GraphQLNonNull
  @GraphQLQuery(name = "id", description = "Session ID")
  private String id;

  public Session() {}

  public void setType(String type) {
    this.type = type;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setSessionId(String sessionId) {
    this.id = sessionId;
  }

  @GraphQLQuery(name = "type", description = "Session type - currently only set if value is admin")
  public String getType() {
    return type;
  }

  @GraphQLNonNull
  @GraphQLQuery(name = "id", description = "Session ID")
  public String getId() {
    return id;
  }
  /**
   * @zm-api-field-tag session-id
   * @zm-api-field-description Session ID (same as <b>id</b>)
   */
  @XmlValue
  @GraphQLIgnore
  public String getSessionId() {
    return id;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("type", type).add("id", id);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
