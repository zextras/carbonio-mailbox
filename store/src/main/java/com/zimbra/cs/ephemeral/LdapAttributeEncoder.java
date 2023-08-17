// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.ephemeral;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;

/**
 * Attribute encoder specific to LdapEphemeralBackend.
 *
 * @author iraykin
 */
public class LdapAttributeEncoder extends DynamicExpirationEncoder {

  public LdapAttributeEncoder() {
    setKeyEncoder(new StaticKeyEncoder());
    setValueEncoder(new LdapValueEncoder());
  }

  @Override
  public ExpirableEphemeralKeyValuePair decode(String key, String value) throws ServiceException {
    if (key.equalsIgnoreCase(Provisioning.A_zimbraAuthTokens)) {
      return decodeAuthToken(value);
    } else {
      return super.decode(key, value);
    }
  }

  private ExpirableEphemeralKeyValuePair decodeAuthToken(String value) throws ServiceException {
    String[] parts = value.split("\\|");
    if (parts.length != 3) {
      throw ServiceException.PARSE_ERROR(
          String.format("LDAP auth token %s cannot be parsed", value), null);
    }
    String token = parts[0];
    Long expirationMillis;
    try {
      expirationMillis = Long.parseLong(parts[1]);
    } catch (NumberFormatException e) {
      throw ServiceException.PARSE_ERROR(
          String.format("LDAP auth token %s does not have a valid expiration value", value), e);
    }
    String serverVersion = parts[2];
    EphemeralKey key = new EphemeralKey(Provisioning.A_zimbraAuthTokens, token);
    return new ExpirableEphemeralKeyValuePair(key, serverVersion, expirationMillis);
  }
}
