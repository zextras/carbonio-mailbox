// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;

import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_ACTION_RESULT, description="Action response")
public class ActionResult {

    private final static Splitter COMMA_SPLITTER = Splitter.on(",");

    /**
     * @zm-api-field-tag success-ids
     * @zm-api-field-description Comma-separated list of ids which have been successfully processed
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.IDS, description="Comma-separated list of ids which have been successfully processed")
    private final String id;

    /**
     * @zm-api-field-tag operation
     * @zm-api-field-description Operation
     */
    @XmlAttribute(name=MailConstants.A_OPERATION /* op */, required=true)
    @GraphQLIgnore
    private final String operation;

    /**
     * @zm-api-field-tag non-existent-ids
     * @zm-api-field-description Comma-separated list of non-existent ids (if requested)
     */
    @XmlAttribute(name=MailConstants.A_NON_EXISTENT_IDS /* nei */, required=false)
    @GraphQLQuery(name=GqlConstants.NON_EXISTENT_IDS, description="Comma-separated list of non-existent ids (if requested)")
    protected String nonExistentIds;

    /**
     * @zm-api-field-tag newly-created-ids
     * @zm-api-field-description Comma-separated list of newly created ids (if requested)
     */
    @XmlAttribute(name=MailConstants.A_NEWLY_CREATED_IDS /* nci */, required=false)
    @GraphQLIgnore
    private String newlyCreatedIds;

    /**
     * no-argument constructor wanted by JAXB
     */
    protected ActionResult() {
        this((String) null, (String) null);
    }

    public ActionResult(String id, String operation) {
        this.id = id;
        this.operation = operation;
    }

    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.IDS, description="Comma-separated list of ids which have been successfully processed")
    public String getId() { return id; }
    @GraphQLIgnore
    public String getOperation() { return operation; }

    public void setNonExistentIds(String ids) { this.nonExistentIds = ids; };
    @GraphQLQuery(name=GqlConstants.NON_EXISTENT_IDS, description="Comma-separated list of non-existent ids (if requested)")
    public String getNonExistentIds() { return nonExistentIds; };
    public void setNewlyCreatedIds(String newlyCreatedIds) { this.newlyCreatedIds = newlyCreatedIds; }
    @XmlTransient
    @GraphQLIgnore
    public Iterable<String> getNewlyCreatedIds() {
        if (null == newlyCreatedIds) {
            return Collections.emptyList();
        }
        return COMMA_SPLITTER.split(newlyCreatedIds);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("operation", operation)
            .add("nei", nonExistentIds)
            .add("nci", newlyCreatedIds);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

}
