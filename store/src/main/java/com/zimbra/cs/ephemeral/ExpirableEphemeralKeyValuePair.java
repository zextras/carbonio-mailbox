// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.ephemeral;

/**
 * A representation of an ephemeral key/value pair that supports an optional
 * expiration value
 *
 * @author iraykin
 *
 */
public class ExpirableEphemeralKeyValuePair extends EphemeralKeyValuePair {
    private Long expiration;

    public ExpirableEphemeralKeyValuePair(EphemeralKey key, String value, Long expiration) {
        super(key, value);
        this.expiration = expiration;
    }

    public Long getExpiration() {
        return expiration;
    }
}