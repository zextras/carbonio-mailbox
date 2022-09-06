// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.ephemeral;

/**
 * Base class for a value encoder; to be composed with a @KeyEncoder to make an @AttributeEncoder
 *
 * @author iraykin
 */
public abstract class ValueEncoder {

  public abstract String encodeValue(EphemeralInput input, EphemeralLocation target);
}
