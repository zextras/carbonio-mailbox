// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.oauth;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;
import com.zimbra.cs.account.oauth.utils.MemcachedMapPlusPutWithExtraParam;
import com.zimbra.cs.memcached.MemcachedConnector;
import net.oauth.OAuthAccessor;

public class OAuthTokenCache {
  private static OAuthTokenCache sTheInstance = new OAuthTokenCache();

  private static final Log LOG = ZimbraLog.oauth;

  private MemcachedMapPlusPutWithExtraParam<OAuthTokenCacheKey, OAuthAccessor> mMemcachedLookup;

  public static OAuthTokenCache getInstance() {
    return sTheInstance;
  }

  public static final String REQUEST_TOKEN_TYPE = "req_token";

  public static final String ACCESS_TOKEN_TYPE = "access_token";

  public static final int OAUTH_TOKEN_EXPIRY = 300;

  OAuthTokenCache() {
    ZimbraMemcachedClient memcachedClient = MemcachedConnector.getClient();
    OAuthAccessorSerializer serializer = new OAuthAccessorSerializer();
    mMemcachedLookup =
        new MemcachedMapPlusPutWithExtraParam<OAuthTokenCacheKey, OAuthAccessor>(
            memcachedClient, serializer);
  }

  private OAuthAccessor get(OAuthTokenCacheKey key) throws ServiceException {
    return mMemcachedLookup.get(key);
  }

  private void put(OAuthTokenCacheKey key, OAuthAccessor accessor) throws ServiceException {
    mMemcachedLookup.put(key, accessor, OAUTH_TOKEN_EXPIRY, ZimbraMemcachedClient.DEFAULT_TIMEOUT);
  }

  public static OAuthAccessor get(String consumer_token, String token_type)
      throws ServiceException {

    String key_prefix = null;

    if (token_type == OAuthTokenCache.ACCESS_TOKEN_TYPE) {
      key_prefix = OAuthTokenCacheKey.ACCESS_TOKEN_PREFIX;
    } else if (token_type == OAuthTokenCache.REQUEST_TOKEN_TYPE) {
      key_prefix = OAuthTokenCacheKey.REQUEST_TOKEN_PREFIX;
    }

    OAuthTokenCacheKey key = new OAuthTokenCacheKey(consumer_token, key_prefix);
    LOG.debug(
        "get type: "
            + token_type
            + " token from memcache with key: "
            + key.getKeyPrefix()
            + key.getKeyValue()
            + ".");

    OAuthAccessor cache = sTheInstance.get(key);

    if (cache != null) {
      if (token_type == OAuthTokenCache.ACCESS_TOKEN_TYPE) {
        cache.accessToken = consumer_token;
        cache.requestToken = null;
      } else {
        cache.requestToken = consumer_token;
        cache.accessToken = null;
      }
    }
    return cache;
  }

  public static void put(OAuthAccessor accessor, String token_type) throws ServiceException {
    String consumer_token = null;
    String key_prefix = null;

    if (token_type == OAuthTokenCache.ACCESS_TOKEN_TYPE) {
      consumer_token = accessor.accessToken;

      if (accessor.requestToken != null) {
        // consumer_token = accessor.requestToken;
        OAuthTokenCacheKey removable_key =
            new OAuthTokenCacheKey(accessor.requestToken, OAuthTokenCacheKey.REQUEST_TOKEN_PREFIX);

        LOG.debug(
            "remove type: req_token token from memcache with key: "
                + removable_key.getKeyPrefix()
                + removable_key.getKeyValue()
                + ".");

        sTheInstance.remove(removable_key);
      }
      key_prefix = OAuthTokenCacheKey.ACCESS_TOKEN_PREFIX;
    } else if (token_type == OAuthTokenCache.REQUEST_TOKEN_TYPE) {
      consumer_token = accessor.requestToken;
      key_prefix = OAuthTokenCacheKey.REQUEST_TOKEN_PREFIX;
    }

    OAuthTokenCacheKey key = new OAuthTokenCacheKey(consumer_token, key_prefix);

    LOG.debug(
        "put type: "
            + token_type
            + " token into memcache with key: "
            + key.getKeyPrefix()
            + key.getKeyValue()
            + ".");

    sTheInstance.put(key, accessor);
  }

  private void remove(OAuthTokenCacheKey key) throws ServiceException {
    mMemcachedLookup.remove(key);
  }

  public static void remove(String consumer_token, String token_type) throws ServiceException {
    String key_prefix = null;

    if (token_type == OAuthTokenCache.ACCESS_TOKEN_TYPE) {
      key_prefix = OAuthTokenCacheKey.ACCESS_TOKEN_PREFIX;
    } else if (token_type == OAuthTokenCache.REQUEST_TOKEN_TYPE) {
      key_prefix = OAuthTokenCacheKey.REQUEST_TOKEN_PREFIX;
    }

    OAuthTokenCacheKey key = new OAuthTokenCacheKey(consumer_token, key_prefix);

    LOG.debug(
        "remove type: " + token_type + " token from memcache with key: " + key.getKeyValue() + ".");

    sTheInstance.remove(key);
  }
}
