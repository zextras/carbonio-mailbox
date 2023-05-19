// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util.memcached;

import com.zimbra.common.service.ServiceException;

/**
 * Serializes an object of type V to String, and deserializes a String to a V object.
 *
 * @param <V>
 */
public interface MemcachedSerializer<V> {

    Object serialize(V value) throws ServiceException;
    V deserialize(Object obj) throws ServiceException;
}
