// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class DistributionListInfo extends AdminObjectInfo {

    /**
     * @zm-api-field-tag dl-is-dynamic
     * @zm-api-field-description Flags whether this is a dynamic distribution list or not
     */
    @XmlAttribute(name=AdminConstants.A_DYNAMIC /* dynamic */, required=false)
    ZmBoolean dynamic;
    /**
     * @zm-api-field-description dl-members
     */
    @XmlElement(name=AdminConstants.E_DLM /* dlm */, required=false)
    private List<String> members;

    /**
     * @zm-api-field-description Owner information
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_DL_OWNERS /* owners */, required=false)
    @XmlElement(name=AdminConstants.E_DL_OWNER /* owner */, required=false)
    private List<GranteeInfo> owners = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DistributionListInfo() {
        this((String) null, (String) null,
            (Collection <String>) null, (Collection <Attr>) null);
    }

    public DistributionListInfo(String id, String name) {
        this(id, name,
            (Collection <String>) null, (Collection <Attr>) null);
    }

    public DistributionListInfo(String id, String name,
            Collection <String> members, Collection <Attr> attrs) {
        super(id, name, attrs);
        setMembers(members);
    }

    public void setMembers(Collection <String> members) {
        this.members = Lists.newArrayList();
        if (members != null) {
            this.members.addAll(members);
        }
    }

    public List<String> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = ZmBoolean.fromBool(dynamic);
    }

    public Boolean isDynamic() {
        return ZmBoolean.toBool(dynamic, false);
    }

    public void setOwners(List<GranteeInfo> owners) {
        this.owners = owners;
    }

    public void addOwner(GranteeInfo owner) {
        owners.add(owner);
    }

    public List<GranteeInfo> getOwners() {
        return Collections.unmodifiableList(owners);
    }

}
