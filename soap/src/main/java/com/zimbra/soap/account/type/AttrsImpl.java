// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.StringUtil;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AttrsImpl implements Attrs {

    /**
     * @zm-api-field-description Attributes
     */
    @ZimbraKeyValuePairs
    @XmlElement(name=AdminConstants.E_A)
    private List<Attr> attrs = Lists.newArrayList();

    public AttrsImpl() {
        this.setAttrs((Iterable<Attr>) null);
    }

    public AttrsImpl(Iterable<Attr> attrs) {
        this.setAttrs(attrs);
    }

    public AttrsImpl (Map<String, ? extends Object> attrs)
    throws ServiceException {
        this.setAttrs(attrs);
    }

    @Override
    public Attrs setAttrs(Iterable<? extends Attr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs, attrs);
        }
        return this;
    }

    @Override
    public Attrs setAttrs(Map<String, ? extends Object> attrs)
    throws ServiceException {
        this.setAttrs(Attr.fromMap(attrs));
        return this;
    }

    @Override
    public Attrs addAttr(Attr attr) {
        attrs.add(attr);
        return this;
    }

    @Override
    public List<Attr> getAttrs() {
        return Collections.unmodifiableList(attrs);
    }

    @Override
    public Multimap<String, String> getAttrsMultimap() {
        return Attr.toMultimap(attrs);
    }

    /**
     * @param name name of attr to get
     * @return null if unset, or first value in list
     */
    @Override
    public String getFirstMatchingAttr(String name) {
        Collection<String> values = getAttrsMultimap().get(name);
        Iterator<String> iter = values.iterator();
        if (!iter.hasNext()) {
            return null;
        }
        return iter.next();
    }

    @Override
    public Map<String, Object> getAttrsAsOldMultimap() {
        return StringUtil.toOldMultimap(getAttrsMultimap());
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add(AdminConstants.E_A, attrs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
