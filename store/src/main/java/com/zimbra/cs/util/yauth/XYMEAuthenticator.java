// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import com.zimbra.cs.mailclient.MailConfig;
import com.zimbra.cs.mailclient.auth.Authenticator;
import java.io.UnsupportedEncodingException;

/**
 * Support for IMAP XYMECookie authentication.
 *
 * @see <a
 *     href="http://twiki.corp.yahoo.com/view/Mail/IMAPGATEExtendedCommands#AUTHENTICATE_XYMECOOKIE">XYMECOOKIE
 *     Method</a>
 */
public class XYMEAuthenticator extends Authenticator {
  private final Auth auth;
  private final String partner;

  public static final String MECHANISM = "XYMECOOKIE";

  public XYMEAuthenticator(Auth auth, String partner) {
    this.auth = auth;
    this.partner = partner;
  }

  @Override
  public void init(MailConfig config, String password) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getMechanism() {
    return MECHANISM;
  }

  @Override
  public byte[] evaluateChallenge(byte[] challenge) {
    try {
      String response =
          String.format(
              "cookies=%s appid=%s wssid=%s src=%s",
              auth.getCookie(), auth.getAppId(), auth.getWSSID(), partner);
      return response.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new InternalError();
    }
  }

  @Override
  public boolean isComplete() {
    return true;
  }
}
