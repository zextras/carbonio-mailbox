// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Pref;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get preferences for the authenticated account <br>
 *     If no <b>&lt;pref></b> elements are provided, all known prefs are returned in the response.
 *     <br>
 *     If <b>&lt;pref></b> elements are provided, only those prefs are returned in the response.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_PREFS_REQUEST)
@GraphQLType(
    name = GqlConstants.CLASS_GET_PREFS_REQUEST,
    description = "Get preferences for the authenticated account")
public class GetPrefsRequest {
  /**
   * @zm-api-field-description If any of these are specified then only get these preferences
   */
  @XmlElement(name = AccountConstants.E_PREF)
  @GraphQLQuery(
      name = GqlConstants.PREFERENCES,
      description = "List of prefs that is wanted in the response")
  private List<Pref> pref;

  public void setPref(List<Pref> pref) {
    this.pref = pref;
  }

  public List<Pref> getPref() {
    return Collections.unmodifiableList(pref);
  }
}
