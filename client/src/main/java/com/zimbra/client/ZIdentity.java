// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.soap.account.type.Attr;
import com.zimbra.soap.account.type.Identity;

public class ZIdentity  implements ToZJSONObject {

    private Identity data;

    public ZIdentity(Identity data) {
        this.data = data;
        
    }

    public ZIdentity(String name, Map<String, Object> attrs) {
        data = Identity.fromName(name);
        data.setAttrs(Attr.fromMultimap(StringUtil.toNewMultimap(attrs)));
        data.setId(data.getFirstMatchingAttr(ZAttrProvisioning.A_zimbraPrefIdentityId));
    }
    
    public Identity getData() {
        return new Identity(data);
    }

    public String getRawName() {
        return data.getName();
    }

    public String getName() {
        return get(ZAttrProvisioning.A_zimbraPrefIdentityName);
    }

    public String getId() {
        return data.getId();
    }
    
    /**
     * @param name name of pref to get
     * @return null if unset, or first value in list
     */
    public String get(String name) {
        return data.getFirstMatchingAttr(name);
    }

    public Element toElement(Element parent) {
        Element identity = parent.addElement(AccountConstants.E_IDENTITY);
        identity.addAttribute(AccountConstants.A_NAME, getRawName()).addAttribute(AccountConstants.A_ID, getId());
        for (Map.Entry<String,String> entry : data.getAttrsMultimap().entries()) {
            identity.addKeyValuePair(entry.getKey(), entry.getValue(), AccountConstants.E_A,  AccountConstants.A_NAME);
        }
        return identity;
    }

  @Override
    public ObjectNode toObjectNode() {
        ObjectNode zjo = JsonNodeFactory.instance.objectNode();
        if (getName() != null) zjo.put("name", getName());
        if (getId() != null) zjo.put("id", getId());
        ObjectNode jo = JsonNodeFactory.instance.objectNode();
        zjo.set("attrs", jo);
        for (Map.Entry<String, String> entry : data.getAttrsMultimap().entries()) {
            if (entry.getValue() != null) jo.put(entry.getKey(), entry.getValue());
        }
        return zjo;
    }

    @Override
    public String toString() {
        return String.format("[ZIdentity %s]", getName());
    }

    public String dump() {
        return ToZJSONObject.toString(this);
    }

}
