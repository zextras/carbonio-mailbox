// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.base.DistributionListGranteeInfoInterface;
import com.zimbra.soap.type.KeyValuePair;
import com.zimbra.soap.type.ZmBoolean;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_DL)
public class DistributionListInfo extends ObjectInfo {

    /**
     * @zm-api-field-tag isOwner
     * @zm-api-field-description Flags whether user is the owner of the group.
     * <br />
     * Only returned if <b>ownerOf</b> on the request is <b>1 (true)</b>
     */
    @XmlAttribute(name=AccountConstants.A_IS_OWNER, required=false)
    ZmBoolean isOwner;

    /**
     * @zm-api-field-tag isMember
     * @zm-api-field-description Flags whether user is a member of the group.
     * <br />
     * Only returned if <b>memberOf</b> on the request is <b>1 (true)</b>
     */
    @XmlAttribute(name=AccountConstants.A_IS_MEMBER, required=false)
    ZmBoolean isMember;

    /**
     * @zm-api-field-tag dl-is-dynamic
     * @zm-api-field-description Flags whether the group is dynamic or not
     */
    @XmlAttribute(name=AccountConstants.A_DYNAMIC, required=false)
    ZmBoolean dynamic;
    /**
     * @zm-api-field-description Group members
     */
    @XmlElement(name=AccountConstants.E_DLM /* dlm */, required=false)
    private List<String> members;

    /**
     * @zm-api-field-description Group owners
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AccountConstants.E_DL_OWNERS /* owners */, required=false)
    @XmlElement(name=AccountConstants.E_DL_OWNER /* owner */, required=false)
    private List<DistributionListGranteeInfo> owners;

    /**
     * @zm-api-field-description Rights
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AccountConstants.E_RIGHTS, required=false)
    @XmlElement(name=AccountConstants.E_RIGHT, required=false)
    private List<DistributionListRightInfo> rights;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DistributionListInfo() {
        this((String) null, (String) null,
            (Collection <String>) null, (Collection <KeyValuePair>) null);
    }

    public DistributionListInfo(String id, String name) {
        this((String)id, (String)name,
            (Collection <String>) null, (Collection <KeyValuePair>) null);
    }

    public DistributionListInfo(String id, String name,
            Collection <String> members, Collection <KeyValuePair> attrs) {
        super(id, name, attrs);
        setMembers(members);
    }

    public void setIsOwner(Boolean isOwner) {
        this.isOwner = ZmBoolean.fromBool(isOwner);
    }

    public Boolean isOwner() {
        return ZmBoolean.toBool(isOwner, false);
    }

    public void setIsMember(Boolean isMember) {
        this.isMember = ZmBoolean.fromBool(isMember);
    }

    public Boolean isMember() {
        return ZmBoolean.toBool(isMember, false);
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

    public void setOwners(List<DistributionListGranteeInfo> owners) {
        this.owners = owners;
    }

    public void addOwner(DistributionListGranteeInfo owner) {
        if (owners == null) {
            owners = Lists.newArrayList();
        }
        owners.add(owner);
    }

    public List<? extends DistributionListGranteeInfoInterface> getOwners() {
        return (owners == null) ? null : Collections.unmodifiableList(owners);
    }

    public void setRights(List<DistributionListRightInfo> rights) {
        this.rights = rights;
    }

    public void addRight(DistributionListRightInfo right) {
        if (rights == null) {
            rights = Lists.newArrayList();
        }
        rights.add(right);
    }

    public List<? extends DistributionListRightInfo> getRights() {
        return (rights == null) ? null : Collections.unmodifiableList(rights);
    }
}
