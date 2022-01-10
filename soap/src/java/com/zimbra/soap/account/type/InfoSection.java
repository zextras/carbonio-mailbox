// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.service.ServiceException;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;

@XmlEnum
@GraphQLType(name=GqlConstants.ENUM_INFO_SECTION)
public enum InfoSection {
    // mbox,prefs,attrs,zimlets,props,idents,sigs,dsrcs,children
    @GraphQLEnumValue @XmlEnumValue("mbox") mbox,
    @GraphQLEnumValue @XmlEnumValue("prefs") prefs,
    @GraphQLEnumValue @XmlEnumValue("attrs") attrs,
    @GraphQLEnumValue @XmlEnumValue("zimlets") zimlets,
    @GraphQLEnumValue @XmlEnumValue("props") props,
    @GraphQLEnumValue @XmlEnumValue("idents") idents,
    @GraphQLEnumValue @XmlEnumValue("sigs") sigs,
    @GraphQLEnumValue @XmlEnumValue("dsrcs") dsrcs,
    @GraphQLEnumValue @XmlEnumValue("children") children;

    public static InfoSection fromString(String s) throws ServiceException {
        try {
            return InfoSection.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("invalid sortBy: "+s+", valid values: "+Arrays.asList(InfoSection.values()), e);
        }
    }
}
