// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.DomUtil;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class LmcModifyPrefsRequest extends LmcSoapRequest {

  private Map mPrefMods;

  public void setPrefMods(Map m) {
    mPrefMods = m;
  }

  public Map getPrefMods() {
    return mPrefMods;
  }

  protected Element getRequestXML() {
    Element request = DocumentHelper.createElement(AccountConstants.MODIFY_PREFS_REQUEST);

    Set s = mPrefMods.entrySet();
    Iterator i = s.iterator();
    while (i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();
      Element pe = DomUtil.add(request, AccountConstants.E_PREF, (String) entry.getValue());
      DomUtil.addAttr(pe, AccountConstants.A_NAME, (String) entry.getKey());
    }

    return request;
  }

  protected LmcSoapResponse parseResponseXML(Element responseXML) {
    // there is no data provided in the response
    return new LmcModifyPrefsResponse();
  }
}
