// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.LocaleInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_ALL_LOCALES_RESPONSE)
public class GetAllLocalesResponse {

  /**
   * @zm-api-field-description Information about locales
   */
  @XmlElement(name = AccountConstants.E_LOCALE, required = false)
  private List<LocaleInfo> locales = Lists.newArrayList();

  public GetAllLocalesResponse() {}

  public void setLocales(Iterable<LocaleInfo> locales) {
    this.locales.clear();
    if (locales != null) {
      Iterables.addAll(this.locales, locales);
    }
  }

  public GetAllLocalesResponse addLocal(LocaleInfo local) {
    this.locales.add(local);
    return this;
  }

  public List<LocaleInfo> getLocales() {
    return Collections.unmodifiableList(locales);
  }
}
