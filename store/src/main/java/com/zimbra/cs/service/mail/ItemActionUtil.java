package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;

public class ItemActionUtil {

  /**
   * Validates the grant expiry time against the maximum allowed expiry duration and returns the
   * effective expiry time for the grant.
   *
   * @param grantExpiry Grant expiry XML attribute value
   * @param maxLifetime Maximum allowed grant expiry duration
   * @return Effective expiry time for the grant. Return value of 0 indicates that grant never
   *     expires.
   * @throws ServiceException If the grant expiry time is not valid according to the expiration
   *     policy.
   */
  public long validateGrantExpiry(String grantExpiry, long maxLifetime) throws ServiceException {
    long now = System.currentTimeMillis();
    long ret =
        grantExpiry == null ? maxLifetime == 0 ? 0 : now + maxLifetime : Long.valueOf(grantExpiry);
    if (grantExpiry != null && maxLifetime != 0 && (ret == 0 || ret > now + maxLifetime)) {
      throw ServiceException.PERM_DENIED("share expiration policy conflict");
    }
    return ret;
  }
}
