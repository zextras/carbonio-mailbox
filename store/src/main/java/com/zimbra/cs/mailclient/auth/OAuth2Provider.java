// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.auth;

import java.security.Provider;

public final class OAuth2Provider extends Provider {

  private static final long serialVersionUID = 1L;
  private static final String ZIMBRA_OAUTH2_PROVIDER = "Zimbra OAuth2 SASL Client";
  private static final String ZIMBRA_OAUTH2_PROVIDER_KEY = "SaslClientFactory.XOAUTH2";
  private static final String ZIMBRA_OAUTH2_PROVIDER_VALUE =
      "com.zimbra.cs.mailclient.auth.OAuth2SaslClientFactory";

  public OAuth2Provider(int version) {
    super(ZIMBRA_OAUTH2_PROVIDER, version, "Provides XOAUTH2 SASL Mechanism");
    put(ZIMBRA_OAUTH2_PROVIDER_KEY, ZIMBRA_OAUTH2_PROVIDER_VALUE);
  }
}
