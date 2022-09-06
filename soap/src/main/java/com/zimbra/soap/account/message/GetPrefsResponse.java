// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Pref;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_PREFS_RESPONSE)
@XmlType(propOrder = {AccountConstants.E_PREF})
public class GetPrefsResponse {

  /**
   * @zm-api-field-description Preferences
   */
  @XmlElement(name = AccountConstants.E_PREF)
  private List<Pref> pref = new ArrayList<Pref>();

  public void setPref(List<Pref> pref) {
    this.pref = pref;
  }

  public List<Pref> getPref() {
    return Collections.unmodifiableList(pref);
  }
}
