// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.jaxb;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;

import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.type.KeyValuePair;
import com.zimbra.soap.type.KeyValuePairs;

/**
 * Test JAXB class which implements KeyValuePairs
 * Also tests {@link ZimbraKeyValuePairs} annotation
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="key-value-pairs")
public class KVPairs implements KeyValuePairs {
    // Note: This is the default name for a KeyValuePair - although it can be different
    @XmlElement(name=Element.XMLElement.E_ATTRIBUTE /* a */)
    @ZimbraKeyValuePairs
    private List<KeyValuePair> keyValuePairs;

    public KVPairs() {}

    @Override
    public void setKeyValuePairs(Iterable<KeyValuePair> keyValues) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = Lists.newArrayList();
        }
        this.keyValuePairs.clear();
        if (keyValues != null) {
            Iterables.addAll(this.keyValuePairs, keyValues);
        }
    }

    @Override
    public void setKeyValuePairs(Map<String, ? extends Object> keyValues)
    throws ServiceException {
        this.setKeyValuePairs(KeyValuePair.fromMap(keyValues));
    }

    @Override
    public void addKeyValuePair(KeyValuePair keyValue) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = Lists.newArrayList();
        }
        keyValuePairs.add(keyValue);
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() { return keyValuePairs; }
    @Override
    public Multimap<String, String> getKeyValuePairsMultimap() { return KeyValuePair.toMultimap(keyValuePairs); }
    @Override
    public Map<String, Object> getKeyValuePairsAsOldMultimap() {
        return StringUtil.toOldMultimap(getKeyValuePairsMultimap());
    }

    @Override
    public String firstValueForKey(String key) { return null; /* just here to satisfy interface */ }
    @Override
    public List<String> valuesForKey(String key) { return null /* just here to satisfy interface */; }
}
