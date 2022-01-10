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
public class ServerSelector {
    // TODO: Change com.zimbra.cs.account.Provisioning.ServerBy to use this
    @XmlEnum
    public enum ServerBy {
        // case must match protocol
        id, name, serviceHostname;

        public static ServerBy fromString(String s) throws ServiceException {
            try {
                return ServerBy.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST("unknown key: "+s, e);
            }
        }
    }

    /**
     * @zm-api-field-tag server-key
     * @zm-api-field-description Key for choosing server
     * <table>
     * <tr> <td> <b>{server-by}="id"</b> </td> <td> Use server's zimbraId as the Key </td> </tr>
     * <tr> <td> <b>{server-by}="name"</b> </td> <td> Use name of the server as the Key </td> </tr>
     * <tr> <td> <b>{server-by}="serviceHostname"</b> </td>
     *               <td> Use server's value for attr <b>zimbraServiceHostname</b> as the Key </td> </tr>
     * </table>
     */
    @XmlValue private final String key;

    /**
     * @zm-api-field-tag server-by
     * @zm-api-field-description Selects the meaning of <b>{server-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY) private final ServerBy serverBy;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ServerSelector() {
        this(null, null);
    }

    public ServerSelector(ServerBy by, String key) {
        this.serverBy = by;
        this.key = key;
    }

    public String getKey() { return key; }
    public ServerBy getBy() { return serverBy; }

    public static ServerSelector fromId(String id) {
        return new ServerSelector(ServerBy.id, id);
    }

    public static ServerSelector fromName(String name) {
        return new ServerSelector(ServerBy.name, name);
    }
}
