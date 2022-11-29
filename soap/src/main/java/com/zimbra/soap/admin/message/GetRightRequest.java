// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get definition of a right
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_RIGHT_REQUEST)
public class GetRightRequest {

    /**
     * @zm-api-field-tag expand-all-attrs
     * @zm-api-field-description whether to include all attribute names in the <b>&lt;attrs></b> elements in
     * the response if the right is meant for all attributes 
     * <table>
     * <tr> <td> <b>0 (false) [default]</b> </td>
     *            <td> default, do not include all attribute names in the <b>&lt;attrs></b> elements </td> </tr>
     * <tr> <td> <b>1 (true)</b> </td>
     *            <td> include all attribute names in the <b>&lt;attrs></b> elements </td> </tr>
     * </table>
     */
    @XmlAttribute(name=AdminConstants.A_EXPAND_ALL_ATTRS, required=false)
    private final ZmBoolean expandAllAttrs;

    /**
     * @zm-api-field-tag right-name
     * @zm-api-field-description Right name
     */
    @XmlElement(name=AdminConstants.E_RIGHT, required=true)
    private final String right;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetRightRequest() {
        this((String) null, (Boolean) null);
    }

    public GetRightRequest(String right, Boolean expandAllAttrs) {
        this.right = right;
        this.expandAllAttrs = ZmBoolean.fromBool(expandAllAttrs);
    }

    public String getRight() { return right; }
    public Boolean getExpandAllAttrs() { return ZmBoolean.toBool(expandAllAttrs); }
}
