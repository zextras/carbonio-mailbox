// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.ephemeral;

/**
 * Key encoder for backends that don't support dynamic keys.
 * Simply returns the main key component.
 *
 * The EphemeralLocation is not used.
 *
 * @author iraykin
 *
 */
public class StaticKeyEncoder extends KeyEncoder {

    @Override
    public String encodeKey(EphemeralKey key, EphemeralLocation target) {
        return key.getKey();
    }

}
