// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.zimbra.common.gql.GqlConstants;

import io.leangen.graphql.annotations.types.GraphQLType;

@XmlEnum
@GraphQLType(name=GqlConstants.CLASS_INCLUDE_RECIPS_SETTING)
public enum WantRecipsSetting {
    @XmlEnumValue("0") PUT_SENDERS,
    @XmlEnumValue("1") PUT_RECIPIENTS,
    @XmlEnumValue("2") PUT_BOTH,
    @Deprecated @XmlEnumValue("false") LEGACY_PUT_SENDERS,
    @Deprecated @XmlEnumValue("true") LEGACY_PUT_RECIPS;

    /**
     * @return sanitized (i.e. non-legacy) value
     */
    public static WantRecipsSetting usefulValue(WantRecipsSetting setting) {
        if (setting == null) {
            return PUT_SENDERS;
        }
        if (WantRecipsSetting.LEGACY_PUT_SENDERS.equals(setting)) {
            return PUT_SENDERS;
        } else if (WantRecipsSetting.LEGACY_PUT_RECIPS.equals(setting)) {
            return PUT_RECIPIENTS;
        }
        return setting;
    }
}