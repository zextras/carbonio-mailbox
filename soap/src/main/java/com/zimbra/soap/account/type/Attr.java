// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.soap.base.KeyAndValue;
import com.zimbra.soap.type.ZmBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * e.g. For element named "attr":
 *          <attr name="{name}" [pd="true"]>{value}</attr>
 *
 * Note:  where the attribute name is "n" rather than "name" use {@link KeyValuePair}
 */
public class Attr implements KeyAndValue {

    public static Function<Attr, Attr> COPY = from -> new Attr(from);

    /**
     * @zm-api-field-tag attr-name
     * @zm-api-field-description Name of attribute
     */
    @XmlAttribute(name=AccountConstants.A_NAME /* name */, required=true)
    private String name;

    // If true, flags that the real value of this attribute has not been provided - i.e. value is set to ""
    /**
     * @zm-api-field-tag attr-perm-denied
     * @zm-api-field-description Flags whether permission has been denied (optional).
     * <br />
     * If <b>1 (true)</b>, flags that the real value of this attribute has not been provided.
     * <br />
     * The value is set to ""
     */
    @XmlAttribute(name=AccountConstants.A_PERM_DENIED /* pd */, required=false)
    private ZmBoolean permDenied;

    /**
     * @zm-api-field-tag attr-value
     * @zm-api-field-description Value of attribute
     */
    @XmlValue
    private String value;

    public Attr() {
    }

    public Attr(Attr attr) {
        name = attr.getName();
        value = attr.getValue();
        permDenied = ZmBoolean.fromBool(attr.getPermDenied());
    }

    public Attr(String name) {
        setName(name);
    }

    public Attr(String name, String value) {
        setName(name);
        setValue(value);
    }

    public static Attr forName(String name) {
        return new Attr(name);
    }

    public static Attr forNameAndValue(String name, String value) {
        return new Attr(name, value);
    }

    public static Attr forNameWithPermDenied(String name) {
        final Attr attr = new Attr(name, "");
        attr.setPermDenied(true);
        return attr;
    }

    public String getName() { return name; }

    public void setName( String name) { this.name = name; }

    public Boolean getPermDenied() { return ZmBoolean.toBool(permDenied); }
    public void setPermDenied(Boolean permDenied) { this.permDenied = ZmBoolean.fromBool(permDenied); }

    @Override
    public String getValue() { return value; }
    @Override
    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("value", value)
            .toString();
    }

    public static Multimap<String, String> toMultimap(List<? extends Attr> attrs) {
        final Multimap<String, String> map = ArrayListMultimap.create();
        if (attrs != null) {
            for (final Attr a : attrs) {
                map.put(a.getName(), a.getValue());
            }
        }
        return map;
    }

    public static List<Attr> fromMultimap(Multimap<String, String> attrMap) {
        final List<Attr> attrs = new ArrayList<>();
        if (attrMap != null) {
            for (final Map.Entry<String, String> entry : attrMap.entries()) {
                attrs.add(new Attr(entry.getKey(), entry.getValue()));
            }
        }
        return attrs;
    }

    public static List <Attr> fromMap(Map<String, ? extends Object> attrs)
    throws ServiceException {
        final List<Attr> newAttrs = Lists.newArrayList();
        if (attrs == null) return newAttrs;

        for (final Entry<String, ? extends Object> entry : attrs.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value == null) {
                newAttrs.add(new Attr(key, null));
            } else if (value instanceof String) {
                newAttrs.add(new Attr(key, (String) value));
            } else if (value instanceof String[]) {
                final String[] values = (String[]) value;
                if (values.length == 0) {
                    // an empty array == removing the attr
                    newAttrs.add(new Attr(key, null));
                } else {
                    for (final String v: values) {
                        newAttrs.add(new Attr(key, v));
                    }
                }
            } else {
                throw ZClientException.CLIENT_ERROR(
                        "invalid attr type: " + key + " "
                        + value.getClass().getName(), null);
            }
        }
        return newAttrs;
    }

    @Override
    public void setKey(String key) { setName(key); }

    @Override
    public String getKey() { return getName(); }

}
