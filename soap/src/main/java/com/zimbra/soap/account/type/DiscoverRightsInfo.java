// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.Lists;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.AccountConstants;

import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(name=GqlConstants.CLASS_DISCOVER_RIGHTS_INFO, description="")
public class DiscoverRightsInfo {

    /**
     * @zm-api-field-tag targets-right
     * @zm-api-field-description Right the targets relate to
     */
    @XmlAttribute(name=AccountConstants.A_RIGHT /* right */, required=true)
    private String right;

    /**
     * @zm-api-field-description Targets
     */
    @XmlElement(name=AccountConstants.E_TARGET /* target */, required=true)
    private List<DiscoverRightsTarget> targets = Lists.newArrayList();

    public DiscoverRightsInfo() {
        this(null);
    }

    public DiscoverRightsInfo(String right) {
        this(right, null);
    }

    public DiscoverRightsInfo(String right, Iterable<DiscoverRightsTarget> targets) {
        setRight(right);
        if (targets != null) {
            setTargets(targets);
        }
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setTargets(Iterable<DiscoverRightsTarget> targets) {
        this.targets = Lists.newArrayList(targets);
    }

    public void addTarget(DiscoverRightsTarget target) {
        targets.add(target);
    }

    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.RIGHT, description="Right the targets relate to")
    public String getRight() {
        return right;
    }

    @GraphQLNonNull
    @GraphQLQuery(name=GqlConstants.TARGETS, description="Targets")
    public List<DiscoverRightsTarget> getTargets() {
        return targets;
    }


}
