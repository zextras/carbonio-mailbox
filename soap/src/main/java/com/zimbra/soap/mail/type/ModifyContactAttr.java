// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;

import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_MODIFY_CONTACT_ATTRIBUTE, description="Contact attributes to modify")
public class ModifyContactAttr extends NewContactAttr {

    // See ParsedContact.FieldDelta.Op - values "+" or "-"
    /**
     * @zm-api-field-tag
     * @zm-api-field-description
     */
    @XmlAttribute(name=MailConstants.A_OPERATION /* op */, required=false)
    private String operation;

    public ModifyContactAttr() {
         this((String) null);
    }

    public ModifyContactAttr(String name) {
        super(name);
    }

    public static ModifyContactAttr fromNameAndValue(String name, String value) {
        final ModifyContactAttr mcs = new ModifyContactAttr(name);
        mcs.setValue(value);
        return mcs;
    }

    @GraphQLInputField(name=GqlConstants.OPERATION, description="Specify + or - to add or remove")
    public void setOperation(String operation) { this.operation = operation; }
    public String getOperation() { return operation; }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return super.addToStringInfo(helper)
            .add("operation", operation);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
