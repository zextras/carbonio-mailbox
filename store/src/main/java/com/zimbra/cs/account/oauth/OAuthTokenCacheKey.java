// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.oauth;

import com.zimbra.common.util.memcached.MemcachedKey;

public class OAuthTokenCacheKey implements MemcachedKey {

  public static final String REQUEST_TOKEN_PREFIX = "req:";
  public static final String ACCESS_TOKEN_PREFIX = "acc:";

  private String mToken;
  private String mKeyPrefix;
  private String mKeyVal;

  public OAuthTokenCacheKey(String consumer_token, String key_prefix) {

    mToken = consumer_token;
    mKeyPrefix = key_prefix;
    mKeyVal = mToken;
  }

  public String getCounsumerToken() {
    return mToken;
  }

  public boolean equals(Object other) {
    if (other instanceof OAuthTokenCacheKey) {
      OAuthTokenCacheKey otherKey = (OAuthTokenCacheKey) other;
      return mKeyVal.equals(otherKey.mKeyVal);
    }
    return false;
  }

  public int hashCode() {
    return mKeyVal.hashCode();
  }

  public String toString() {
    return mKeyVal;
  }

  // MemcachedKey interface
  public String getKeyPrefix() {
    return mKeyPrefix;
  }

  public String getKeyValue() {
    return mKeyVal;
  }
}
