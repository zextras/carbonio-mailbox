// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * <account> element created for backwards compatibility with old API that uses "name" attribute:
 * <account name='username@domain' />
 */
@XmlAccessorType(XmlAccessType.NONE)
public class AccountNameSelector {

  /**
   * @zm-api-field-tag acct-selector-by
   * @zm-api-field-description Select the meaning of <b>{acct-selector-key}</b>
   */
  @XmlAttribute(name = AdminConstants.A_BY, required = false)
  private final AccountBy accountBy;

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description Name
   * @deprecated
   */
  @XmlAttribute(name = AdminConstants.A_NAME, required = false)
  private final String name;

  /**
   * @zm-api-field-tag acct-selector-key
   * @zm-api-field-description The key used to identify the account. Meaning determined by
   *     <b>{acct-selector-by}</b>
   */
  @XmlValue private final String key;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AccountNameSelector() {
    this.accountBy = null;
    this.key = null;
    this.name = null;
  }

  public AccountNameSelector(AccountBy by, String key) {
    this.accountBy = by;
    this.key = key;
    if (by == AccountBy.name) {
      this.name = key;
    } else {
      this.name = null;
    }
  }

  public String getKey() {
    if (key == null || key.length() == 0) {
      return name;
    } else {
      return key;
    }
  }

  public AccountBy getBy() {
    if (accountBy == null) {
      return AccountBy.name;
    } else {
      return accountBy;
    }
  }

  public String getName() {
    return name;
  }

  public AccountNameSelector(String name) {
    this.name = name;
    this.key = name;
    this.accountBy = AccountBy.name;
  }

  public static AccountNameSelector fromId(String id) {
    return new AccountNameSelector(AccountBy.id, id);
  }

  public static AccountNameSelector fromName(String name) {
    return new AccountNameSelector(AccountBy.name, name);
  }
}
