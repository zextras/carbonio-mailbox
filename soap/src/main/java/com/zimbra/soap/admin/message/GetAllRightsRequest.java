// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get all system defined rights
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_RIGHTS_REQUEST)
public class GetAllRightsRequest {

    /**
     * @zm-api-field-tag target-type
     * @zm-api-field-description Target type on which a right is grantable
     * <br />
     * e.g. createAccount right is only grantable on domain entries and the globalgrant entry.
     * <br />
     * Don't confuse this with "whether a right is executable on a target type".
     * <br />
     * e.g. the renameAccount right is "executable" on account entries, but it is "grantable" on account,
     * distribuiton list, domain, and globalgrant entries.
     */
    @XmlAttribute(name=AdminConstants.A_TARGET_TYPE, required=false)
    private final String targetType;

    /**
     * @zm-api-field-tag expand-all-attrs
     * @zm-api-field-description Flags whether to include all attribute names in the <b>&lt;attrs></b> elements in
     * GetRightResponse if the right is meant for all attributes 
     */
    @XmlAttribute(name=AdminConstants.A_EXPAND_ALL_ATTRS, required=false)
    private final ZmBoolean expandAllAttrs;

    /**
     * @zm-api-field-tag right-class-to-return
     * @zm-api-field-description Right class to return
     * <table>
     * <tr> <td> <b>ADMIN</b> </td> <td> return admin rights only </td> </tr>
     * <tr> <td> <b>USER</b> </td> <td> return user rights only </td> </tr>
     * <tr> <td> <b>ALL</b> </td> <td> return both admin rights and user rights </td> </tr>
     * </table>
     */
    @XmlAttribute(name=AdminConstants.A_RIGHT_CLASS, required=false)
    private final String rightClass;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetAllRightsRequest() {
        this((String) null, (Boolean) null, (String) null);
    }

    public GetAllRightsRequest(String targetType, Boolean expandAllAttrs,
            String rightClass) {
        this.targetType = targetType;
        this.expandAllAttrs = ZmBoolean.fromBool(expandAllAttrs);
        this.rightClass = rightClass;
    }

    public String getTargetType() { return targetType; }
    public Boolean isExpandAllAttrs() { return ZmBoolean.toBool(expandAllAttrs); }
    public String getRightClass() { return rightClass; }
}
