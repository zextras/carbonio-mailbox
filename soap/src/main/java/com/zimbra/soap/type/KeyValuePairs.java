// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import java.util.List;
import java.util.Map;

/*
 * Used for JAXB objects representing elements which have child node(s) of form:
 *     <a n="{key}">{value}</a>
 */
public interface KeyValuePairs {
  public void setKeyValuePairs(Iterable<KeyValuePair> keyValues);

  public void setKeyValuePairs(Map<String, ? extends Object> keyValues) throws ServiceException;

  public void addKeyValuePair(KeyValuePair keyValue);

  public List<KeyValuePair> getKeyValuePairs();

  public Multimap<String, String> getKeyValuePairsMultimap();

  public Map<String, Object> getKeyValuePairsAsOldMultimap();

  public String firstValueForKey(String key);

  public List<String> valuesForKey(String key);
}
