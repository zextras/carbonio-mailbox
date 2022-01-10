// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import com.zimbra.soap.voice.type.Attr;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=VoiceConstants.E_GET_UC_INFO_RESPONSE)
public class GetUCInfoResponse {

    /**
     * @zm-api-field-description Attributes
     */
    @ZimbraKeyValuePairs
    @XmlElementWrapper(name=AccountConstants.E_ATTRS /* attrs */, required=false)
    @XmlElement(name=AccountConstants.E_ATTR /* attr */, required=false)
    private List<Attr> attrs = Lists.newArrayList();

    public GetUCInfoResponse() {
    }

    public void setAttrs(Iterable <Attr> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs, attrs);
        }
    }

    public void addAttr(Attr attr) {
        this.attrs.add(attr);
    }

    public List<Attr> getAttrs() {
        return attrs;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("attrs", attrs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
