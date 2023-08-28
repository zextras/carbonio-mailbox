// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class UCServiceSelector {
    // TODO: Change com.zimbra.cs.account.Provisioning.UCServiceBy to use this
    @XmlEnum
    public enum UCServiceBy {
        // case must match protocol
        id, name;

        public static UCServiceBy fromString(String s) throws ServiceException {
            try {
                return UCServiceBy.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST("unknown key: "+s, e);
            }
        }
    }

    /**
     * @zm-api-field-tag ucservice-key
     * @zm-api-field-description Key for choosing ucservice
     * <table>
     * <tr> <td> <b>{ucservice-by}="id"</b> </td> <td> Use ucservice's zimbraId as the Key </td> </tr>
     * <tr> <td> <b>{ucservice-by}="name"</b> </td> <td> Use name of the ucservice as the Key </td> </tr>
     * </table>
     */
    @XmlValue private final String key;

    /**
     * @zm-api-field-tag ucservice-by
     * @zm-api-field-description Selects the meaning of <b>{ucservice-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY) private final UCServiceBy ucServiceBy;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private UCServiceSelector() {
        this(null, null);
    }

    public UCServiceSelector(UCServiceBy by, String key) {
        this.ucServiceBy = by;
        this.key = key;
    }

    public String getKey() { return key; }
    public UCServiceBy getBy() { return ucServiceBy; }

    public static UCServiceSelector fromId(String id) {
        return new UCServiceSelector(UCServiceBy.id, id);
    }

    public static UCServiceSelector fromName(String name) {
        return new UCServiceSelector(UCServiceBy.name, name);
    }
}
