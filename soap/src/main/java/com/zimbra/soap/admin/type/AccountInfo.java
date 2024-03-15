// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class AccountInfo extends AdminObjectInfo {

    /**
     * @zm-api-field-tag is-external
     * @zm-api-field-description Whether the account's <b>zimbraMailTranport</b> points to the designated
     * protocol(lmtp) and server(home server of the account).
     */
    @XmlAttribute(name=AccountConstants.A_IS_EXTERNAL, required=false)
    private final ZmBoolean isExternal;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AccountInfo() {
        this(null, null, null, null);
    }

    public AccountInfo(String id, String name) {
        this(id, name, null, null);
    }

    public AccountInfo(String id, String name, Boolean isExternal) {
        this(id, name, isExternal, null);
    }

    public AccountInfo(String id, String name, Boolean isExternal, Collection <Attr> attrs) {
        super(id, name, attrs);
        this.isExternal = ZmBoolean.fromBool(isExternal);
    }

    public Boolean getIsExternal() { return ZmBoolean.toBool(isExternal); }
}
