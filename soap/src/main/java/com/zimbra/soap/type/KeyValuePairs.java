// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import com.zimbra.common.service.ServiceException;

/*
 * Used for JAXB objects representing elements which have child node(s) of form:
 *     <a n="{key}">{value}</a>
 */
public interface KeyValuePairs {
    void setKeyValuePairs(Iterable<KeyValuePair> keyValues);
    void setKeyValuePairs(
        Map<String, ? extends Object> keyValues)
    throws ServiceException;
    void addKeyValuePair(KeyValuePair keyValue);
    List<KeyValuePair> getKeyValuePairs();
    Multimap<String, String> getKeyValuePairsMultimap();
    Map<String, Object> getKeyValuePairsAsOldMultimap();
    String firstValueForKey(String key);
    List<String> valuesForKey(String key);
}
