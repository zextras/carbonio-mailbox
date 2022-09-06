// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_WHITE_BLACK_LIST_RESPONSE)
@XmlType(propOrder = {"whiteListEntries", "blackListEntries"})
public class GetWhiteBlackListResponse {

  /**
   * @zm-api-field-description White list
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = AccountConstants.E_WHITE_LIST, required = true)
  @XmlElement(name = AccountConstants.E_ADDR, required = false)
  private List<String> whiteListEntries = Lists.newArrayList();

  /**
   * @zm-api-field-description Black list
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = AccountConstants.E_BLACK_LIST, required = true)
  @XmlElement(name = AccountConstants.E_ADDR, required = false)
  private List<String> blackListEntries = Lists.newArrayList();

  public GetWhiteBlackListResponse() {}

  public void setWhiteListEntries(Iterable<String> whiteListEntries) {
    this.whiteListEntries.clear();
    if (whiteListEntries != null) {
      Iterables.addAll(this.whiteListEntries, whiteListEntries);
    }
  }

  public GetWhiteBlackListResponse addWhiteListEntry(String whiteListEntry) {
    this.whiteListEntries.add(whiteListEntry);
    return this;
  }

  public void setBlackListEntries(Iterable<String> blackListEntries) {
    this.blackListEntries.clear();
    if (blackListEntries != null) {
      Iterables.addAll(this.blackListEntries, blackListEntries);
    }
  }

  public GetWhiteBlackListResponse addBlackListEntry(String blackListEntry) {
    this.blackListEntries.add(blackListEntry);
    return this;
  }

  public List<String> getWhiteListEntries() {
    return Collections.unmodifiableList(whiteListEntries);
  }

  public List<String> getBlackListEntries() {
    return Collections.unmodifiableList(blackListEntries);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("whiteListEntries", whiteListEntries)
        .add("blackListEntries", blackListEntries)
        .toString();
  }
}
