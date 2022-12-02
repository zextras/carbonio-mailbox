// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import com.zimbra.soap.admin.type.Attr;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify attributes for a Distribution List
 * <br />
 * Notes: an empty attribute value removes the specified attr
 * <br />
 * <b>Access</b>: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_DISTRIBUTION_LIST_REQUEST)
public class ModifyDistributionListRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.E_ID, required=true)
    private String id;

    public ModifyDistributionListRequest() {
        this((String) null, (Collection<Attr>) null);
    }

    public ModifyDistributionListRequest(String id) {
        this((String) id, (Collection<Attr>) null);
    }

    public ModifyDistributionListRequest(String id, Collection<Attr> attrs) {
        super(attrs);
        this.id = id;
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
}
