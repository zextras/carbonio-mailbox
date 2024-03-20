// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util.memcached;

import com.zimbra.common.service.ServiceException;

/**
 * Serializes an object of type V to byte array, and deserializes a byte array to a V object.
 *
 * @param <V>
 */
public interface ByteArraySerializer<V> {

    byte[] serialize(V value) throws ServiceException;
    public V deserialize(byte[] bytes) throws ServiceException;
}
