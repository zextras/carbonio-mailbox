// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ActionResult;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_ITEM_ACTION_RESPONSE)
public class ItemActionResponse {

    /**
     * @zm-api-field-description The <b>&lt;action></b> element in the response always contains the same id list that
     * the client sent in the request.  In particular, IDs that were ignored due to constraints are included in the
     * id list.
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_ACTION /* action */, required=true)
    private final ActionResult action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ItemActionResponse() {
        this((ActionResult) null);
    }

    public ItemActionResponse(ActionResult action) {
        this.action = action;
    }

    public ActionResult getAction() { return action; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("action", action);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
