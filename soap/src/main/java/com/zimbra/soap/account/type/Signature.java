// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Iterables;
import com.zimbra.common.soap.AccountConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_SIGNATURE)
@XmlType(propOrder = {"contentList", AccountConstants.E_CONTACT_ID})
public class Signature {

    /**
     * @zm-api-field-tag signature-id
     * @zm-api-field-description ID for the signature
     */
    @XmlAttribute(name=AccountConstants.A_ID, required=false)
    private String id;

    /**
     * @zm-api-field-tag signature-name
     * @zm-api-field-description Name for the signature
     */
    @XmlAttribute(name=AccountConstants.A_NAME, required=false)
    private String name;

    /**
     * @zm-api-field-description Content of the signature
     */
    @XmlElement(name=AccountConstants.E_CONTENT)
    private List<SignatureContent> contentList = new ArrayList<>();

    /**
     * @zm-api-field-tag contact-id
     * @zm-api-field-description Contact ID
     */
    @XmlElement(name=AccountConstants.E_CONTACT_ID)
    private String cid;

    @SuppressWarnings("unused")
    private Signature() {
    }

    public Signature(Signature sig) {
        this.id = sig.getId();
        this.name = sig.getName();
        this.contentList.addAll(sig.getContent());
        this.cid = sig.getCid();
    }

    public Signature(String id, String name, List<SignatureContent> contentList, String cid) {
        this.id = id;
        this.name = name;
        this.contentList = contentList;
        this.contentList.addAll(contentList);
        this.cid = cid;
    }

    public Signature(String id, String name, String content, String contentType, String cid) {
        this.id = id;
        this.name = name;
        this.cid = cid;
        if (content != null) {
            contentList.add(new SignatureContent(content, contentType));
        }
    }

    public Signature(String id, String name, String content, String contentType) {
        this(id, name, content, contentType, null);
    }

    public String getName() { return name; }
    public String getId() { return id; }
    public String getCid() { return cid; }
    public List<SignatureContent> getContent() {
        return Collections.unmodifiableList(contentList);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addContent(SignatureContent content) {
        this.contentList.add(content);
    }

    public void setContent(Iterable<SignatureContent> content) {
        this.contentList.clear();
        if (content != null) {
            Iterables.addAll(this.contentList, content);
        }
    }

    public void setCid(String cid) {
        this.cid = cid;
    }
}
