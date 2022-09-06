// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.LocaleInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_LOCALES_RESPONSE)
@XmlType(propOrder = {})
public class GetAllLocalesResponse {

  /**
   * @zm-api-field-description Information for system locales
   */
  @XmlElement(name = AccountConstants.E_LOCALE, required = false)
  private List<LocaleInfo> locales = Lists.newArrayList();

  public GetAllLocalesResponse() {}

  public GetAllLocalesResponse setLocales(Collection<LocaleInfo> locales) {
    this.locales.clear();
    if (locales != null) {
      this.locales.addAll(locales);
    }
    return this;
  }

  public GetAllLocalesResponse addLocale(LocaleInfo locale) {
    locales.add(locale);
    return this;
  }

  public List<LocaleInfo> getLocales() {
    return Collections.unmodifiableList(locales);
  }
}
