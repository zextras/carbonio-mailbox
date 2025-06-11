package com.zimbra.cs.imap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.MapUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import java.util.Collections;
import java.util.Map;

/**
 * A utility class to manage the caching of AuthTokens for accounts.
 * It provides methods to retrieve a valid AuthToken, either from the cache or by generating a new one.
 * The cache size is controlled by the local config parameter zimbra_authtoken_cache_size.
 */
public class AuthTokenCacheHelper {
  /**
   * A synchronized map to cache AuthTokens for accounts.
   * The size of the cache is controlled by the local config parameter zimbra_authtoken_cache_size.
   */
  private static final Map<String, AuthToken> CACHE =
      Collections.synchronizedMap(MapUtil.newLruMap(LC.zimbra_authtoken_cache_size.intValue()));


  private AuthTokenCacheHelper() {
    // Utility class — prevent instantiation
  }

  /**
   * Retrieves a valid AuthToken for the given account, either from the cache or by generating a new one.
   * If the cached token is invalid, it will be removed from the cache and a new token will be generated.
   *
   * @param account the account for which to retrieve the AuthToken
   * @return a valid AuthToken for the account
   * @throws ServiceException if there is an error retrieving or validating the AuthToken
   */
  public static AuthToken getValidAuthToken(Account account) throws ServiceException {
    String cacheKey = account.getId();
    AuthToken token = CACHE.get(cacheKey);

    if (token != null) {
      try {
        AuthProvider.validateAuthToken(Provisioning.getInstance(), token, true);
        ZimbraLog.imap.debug("Using cached valid AuthToken for account %s", cacheKey);
        return token;
      } catch (ServiceException e) {
        CACHE.remove(cacheKey);
        ZimbraLog.imap.debug(
            "Cached AuthToken for account %s is invalid: %s", cacheKey, e.getMessage());
      }
    }

    // no valid token is found in the cache, generate a new one
    token = AuthProvider.getAuthToken(account);
    CACHE.put(cacheKey, token);
    ZimbraLog.imap.debug("Generated new AuthToken for account %s", cacheKey);
    return token;
  }

  public static void clearCache() {
    CACHE.clear();
    ZimbraLog.imap.debug("AuthToken cache cleared");
  }
}
