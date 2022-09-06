// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import com.zimbra.common.localconfig.LC;

/** Network configuration settings. */
public final class NetConfig {
  private boolean socksEnabled;
  private boolean allowUntrustedCerts;
  private boolean allowMismatchedCerts;
  private boolean allowAcceptUntrustedCerts;

  private static NetConfig INSTANCE = new NetConfig();

  public static NetConfig getInstance() {
    return INSTANCE;
  }

  private NetConfig() {
    socksEnabled = LC.socks_enabled.booleanValue();
    allowUntrustedCerts = LC.ssl_allow_untrusted_certs.booleanValue();
    allowMismatchedCerts = LC.ssl_allow_mismatched_certs.booleanValue();
    allowAcceptUntrustedCerts = LC.ssl_allow_accept_untrusted_certs.booleanValue();
  }

  public boolean isSocksEnabled() {
    return socksEnabled;
  }

  public NetConfig setSocksEnabled(boolean socksEnabled) {
    this.socksEnabled = socksEnabled;
    return this;
  }

  public boolean isAllowUntrustedCerts() {
    return allowUntrustedCerts;
  }

  public NetConfig setAllowUntrustedCerts(boolean allowUntrustedCerts) {
    this.allowUntrustedCerts = allowUntrustedCerts;
    return this;
  }

  public boolean isAllowMismatchedCerts() {
    return allowMismatchedCerts;
  }

  public NetConfig setAllowMismatchedCerts(boolean allowMismatchedCerts) {
    this.allowMismatchedCerts = allowMismatchedCerts;
    return this;
  }

  public boolean isAllowAcceptUntrustedCerts() {
    return allowAcceptUntrustedCerts;
  }

  public NetConfig setAllowAcceptUntrustedCerts(boolean allowAcceptUntrustedCerts) {
    this.allowAcceptUntrustedCerts = allowAcceptUntrustedCerts;
    return this;
  }
}
