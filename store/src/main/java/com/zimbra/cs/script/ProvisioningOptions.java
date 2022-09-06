// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.script;

public class ProvisioningOptions {

  private String mUsername;
  private String mPassword;
  private String mSoapURI;
  private String mUserAgent;
  private String mUserAgentVersion;

  public String getUsername() {
    return mUsername;
  }

  public String getPassword() {
    return mPassword;
  }

  public String getSoapURI() {
    return mSoapURI;
  }

  public String getUserAgent() {
    return mUserAgent;
  }

  public String getUserAgentVersion() {
    return mUserAgentVersion;
  }

  public ProvisioningOptions setUsername(String username) {
    mUsername = username;
    return this;
  }

  public ProvisioningOptions setPassword(String password) {
    mPassword = password;
    return this;
  }

  public ProvisioningOptions setSoapURI(String soapURI) {
    mSoapURI = soapURI;
    return this;
  }

  public ProvisioningOptions setUserAgent(String userAgent) {
    mUserAgent = userAgent;
    return this;
  }

  public ProvisioningOptions setUserAgentVersion(String version) {
    mUserAgentVersion = version;
    return this;
  }
}
