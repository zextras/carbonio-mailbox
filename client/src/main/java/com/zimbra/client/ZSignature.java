// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.soap.account.type.Signature;
import com.zimbra.soap.account.type.SignatureContent;

public class ZSignature implements Comparable<ZSignature>, ToZJSONObject {

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    
    private Signature data;

    public ZSignature(Signature sig) {
        data = sig;
    }

    public ZSignature(String id, String name, String value, String type) {
        data = new Signature(id, name, value, type);
    }
    
    public ZSignature(String id, String name, String value) {
        this(id, name, value, DEFAULT_CONTENT_TYPE);
    }

    public ZSignature(String name, String value) {
        this(null, name, value, DEFAULT_CONTENT_TYPE);
    }
    
    public Signature getData() {
        return new Signature(data);
    }
    
    public String getName() {
        return data.getName();
    }

    public String getId() {
        return data.getId();
    }    

    public String getValue() {
        SignatureContent content = getFirstContent();
        if (content == null) {
            return null;
        }
        return content.getContent();
    }
    
    public String getType() {
        SignatureContent content = getFirstContent();
        if (content == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return SystemUtil.coalesce(content.getContentType(), DEFAULT_CONTENT_TYPE);
    }
    
    private SignatureContent getFirstContent() {
        if (data.getContent().isEmpty()) {
            return null;
        }
        return data.getContent().get(0);
    }

    public void setType(String type) {
        SignatureContent content = getFirstContent();
        if (content == null) {
            content = new SignatureContent(null, type);
            data.addContent(content);
        }
        content.setContentType(type);
    }
    
    public Map<String, Object> getAttrs() {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(ZAttrProvisioning.A_zimbraSignatureId, data.getId());
        attrs.put(ZAttrProvisioning.A_zimbraSignatureName, data.getName());
        
        String type = null;
        String value = null;
        SignatureContent content = getFirstContent();
        if (content != null) {
            type = content.getContentType();
            value = content.getContent();
            attrs.put(com.zimbra.common.account.SignatureUtil.mimeTypeToAttrName(type), value);
        }
        return attrs;
    }
    
    public Element toElement(Element parent) {
        Element sig = parent.addElement(AccountConstants.E_SIGNATURE);
        sig.addAttribute(AccountConstants.A_NAME, getName());
        if (getId() != null) sig.addAttribute(AccountConstants.A_ID, getId());
        if (getName() != null) sig.addAttribute(AccountConstants.A_NAME, getName());
        if (getValue() != null) {
            Element content = sig.addElement(AccountConstants.E_CONTENT);
            content.addAttribute(AccountConstants.A_TYPE, getType());
            content.setText(getValue());
        }
        return sig;
    }

    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject zjo = new ZJSONObject();
        zjo.put("name", getName());
        zjo.put("id", getId());
        zjo.put("value", getValue());
        zjo.put("type", getType());
        return zjo;
    }

    public String toString() {
        return String.format("[ZSignature %s]", getName());
    }

    public String dump() {
        return ZJSONObject.toString(this);
    }

    public int compareTo(ZSignature other) {
        return getName().compareTo(other.getName());
    }
}
