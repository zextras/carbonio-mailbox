// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.soap.type.KeyValuePair;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_A)
public class Attr extends KeyValuePair {

    public Attr() {
        this(null, null);
    }

    public Attr(String key, String value) {
        super(key, value);
    }

    public static Attr fromNameValue(String key, String value) {
        return new Attr(key, value);
    }

    public static List <Attr> mapToList(Map<String, ? extends Object> attrs)
    throws ServiceException {
        List<Attr> newAttrs = Lists.newArrayList();
        if (attrs == null) {
            return newAttrs;
        }

        for (Entry<String, ? extends Object> entry : attrs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                newAttrs.add(new Attr(key, null));
            } else if (value instanceof String) {
                newAttrs.add(new Attr(key, (String) value));
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                if (values.length == 0) {
                    // an empty array == removing the attr
                    newAttrs.add(new Attr(key, null));
                } else {
                    for (String v: values) {
                        newAttrs.add(new Attr(key, v));
                    }
                }
            } else {
                throw ZClientException.CLIENT_ERROR(
                        "invalid attr type: " + key + " " + value.getClass().getName(), null);
            }
        }
        return newAttrs;
    }

    public static Map<String, Object> collectionToMap(Collection <Attr> attrs, boolean ignoreEmptyValues)
    throws ServiceException {
        Map<String, Object> result = new HashMap<>();
        for (Attr a : attrs) {
            String value = a.getValue();
            if (!ignoreEmptyValues || (value != null && value.length() > 0)) {
                StringUtil.addToMultiMap(result, a.getKey(), a.getValue());
            }
        }
        return result;
    }

    public static Map<String, Object> collectionToMap(Collection <Attr> attrs)
    throws ServiceException {
        return collectionToMap(attrs, false);
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return super.addToStringInfo(helper);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
