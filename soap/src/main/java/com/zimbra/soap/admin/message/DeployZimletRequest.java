// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.admin.type.AttachmentIdAttrib;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Deploy Zimlet(s)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DEPLOY_ZIMLET_REQUEST)
public class DeployZimletRequest {

    /**
     * @zm-api-field-tag action
     * @zm-api-field-description Action - valid values : deployAll|deployLocal|status
     */
    @XmlAttribute(name=AdminConstants.A_ACTION /* action */, required=true)
    private final String action;

    /**
     * @zm-api-field-tag flush-cache
     * @zm-api-field-description Flag whether to flush the cache
     */
    @XmlAttribute(name=AdminConstants.A_FLUSH /* flush */, required=false)
    private final ZmBoolean flushCache;

    /**
     * @zm-api-field-description Synchronous flag
     */
    @XmlAttribute(name=AdminConstants.A_SYNCHRONOUS /* synchronous */, required=false)
    private final ZmBoolean synchronous;

    /**
     * @zm-api-field-description Content
     */
    @XmlElement(name=MailConstants.E_CONTENT /* content */, required=true)
    private final AttachmentIdAttrib content;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DeployZimletRequest() {
        this(null, null, null,
            null);
    }

    public DeployZimletRequest(String action, Boolean flushCache,
                    Boolean synchronous, AttachmentIdAttrib content) {
        this.action = action;
        this.flushCache = ZmBoolean.fromBool(flushCache);
        this.synchronous = ZmBoolean.fromBool(synchronous);
        this.content = content;
    }

    public String getAction() { return action; }
    public Boolean getFlushCache() { return ZmBoolean.toBool(flushCache); }
    public Boolean getSynchronous() { return ZmBoolean.toBool(synchronous); }
    public AttachmentIdAttrib getContent() { return content; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("action", action)
            .add("flushCache", flushCache)
            .add("synchronous", synchronous)
            .add("content", content);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
