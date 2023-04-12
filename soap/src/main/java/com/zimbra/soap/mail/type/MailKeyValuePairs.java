// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.type.KeyValuePair;
import com.zimbra.soap.type.KeyValuePairs;





/*
 * Used for JAXB objects representing elements which have child node(s) of form:
 *     <a n="{key}">{value}</a>
 */
@XmlAccessorType(XmlAccessType.NONE)
public class MailKeyValuePairs implements KeyValuePairs {

    /**
     * @zm-api-field-description Key value pairs
     */
    @ZimbraKeyValuePairs
    @XmlElement(name=MailConstants.E_A)
    private List<KeyValuePair> keyValuePairs;

    public MailKeyValuePairs() {
    }

    public MailKeyValuePairs(Iterable<KeyValuePair> keyValuePairs) {
        setKeyValuePairs(keyValuePairs);
    }

    public MailKeyValuePairs (Map<String, ? extends Object> keyValuePairs)
    throws ServiceException {
        setKeyValuePairs(keyValuePairs);
    }

    public void setKeyValuePairs(
                    List<KeyValuePair> keyValuePairs) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = Lists.newArrayList();
        }
        this.keyValuePairs.clear();
        if (keyValuePairs != null) {
            Iterables.addAll(this.keyValuePairs, keyValuePairs);
        }
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        if (keyValuePairs == null) {
            keyValuePairs = Lists.newArrayList();
        }
        // Making the return of this unmodifiable causes
        // "UnsupportedOperationException" on unmarshalling - see Bug 62187.
        //     return Collections.unmodifiableList(keyValuePairs);
        return keyValuePairs;
    }

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
    public Multimap<String, String> getKeyValuePairsMultimap() {
        return KeyValuePair.toMultimap(keyValuePairs);
    }

    @Override
    public Map<String, Object> getKeyValuePairsAsOldMultimap() {
        return StringUtil.toOldMultimap(getKeyValuePairsMultimap());
    }

    /**
     * Returns the first value matching {@link key} or null if {@link key} not found.
     */
    @Override
    public String firstValueForKey(String key) {
        for (final KeyValuePair kvp : keyValuePairs) {
            if (key.equals(kvp.getKey())) {
                return kvp.getValue();
            }
        }
        return null;
    }

    @Override
    public List<String> valuesForKey(String key) {
        final List<String> values = Lists.newArrayList();
        for (final KeyValuePair kvp : keyValuePairs) {
            if (key.equals(kvp.getKey())) {
                values.add(kvp.getValue());
            }
        }
        return Collections.unmodifiableList(values);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("keyValuePairs", keyValuePairs);
    }
}
