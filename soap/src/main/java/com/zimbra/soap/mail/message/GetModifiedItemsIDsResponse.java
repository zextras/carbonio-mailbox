// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;

@XmlRootElement(name=MailConstants.E_GET_MODIFIED_ITEMS_IDS_RESPONSE)
public class GetModifiedItemsIDsResponse {
    /**
     * @zm-api-field-description IDs of modified items
     */
    @XmlElement(name=MailConstants.A_IDS /* ids */, required=false)
    private List<Integer> ids = Lists.newArrayList();

    public GetModifiedItemsIDsResponse() {
    }

    public void setIds(Iterable <Integer> ids) {
        this.ids.clear();
        if (ids != null) {
            Iterables.addAll(this.ids,ids);
        }
    }

    public GetModifiedItemsIDsResponse addId(Integer id) {
        this.ids.add(id);
        return this;
    }

    public List<Integer> getIds() {
        return Collections.unmodifiableList(ids);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper.add("ids", ids);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
