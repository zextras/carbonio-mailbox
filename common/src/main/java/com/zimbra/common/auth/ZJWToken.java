// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.auth;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraCookie;
import java.util.HashMap;
import java.util.Map;

public class ZJWToken extends ZAuthToken {

  private String salt;

  public ZJWToken(String value, String salt) {
    super(value, null);
    this.salt = salt;
  }

  @Override
  public Element encodeSoapCtxt(Element ctxt) {
    Element ejwToken = ctxt.addNonUniqueElement(HeaderConstants.E_JWT_TOKEN);
    if (mProxyAuthToken != null) {
      ejwToken.setText(mProxyAuthToken);
    } else if (mValue != null) {
      ejwToken.setText(mValue);
    }
    Element saltEl = ctxt.addNonUniqueElement(HeaderConstants.E_JWT_SALT);
    saltEl.setText(salt);
    return ejwToken;
  }

  @Override
  public Element encodeAuthReq(Element authReq, boolean isAdmin) {
    return encodeSoapCtxt(authReq);
  }

  @Override
  public Map<String, String> cookieMap(boolean isAdmin) {
    Map<String, String> cookieMap = null;
    if (!StringUtil.isNullOrEmpty(salt)) {
      cookieMap = new HashMap<String, String>();
      cookieMap.put(ZimbraCookie.COOKIE_ZM_JWT, salt);
    }
    return cookieMap;
  }

  public String getSalt() {
    return salt;
  }
}
