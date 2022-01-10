// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlEnum;

import com.zimbra.common.gql.GqlConstants;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import io.leangen.graphql.annotations.types.GraphQLType;


/**
 * Message Content the client expects in response
 *
 */
@XmlEnum
@GraphQLType(name=GqlConstants.CLASS_MESSAGE_CONTENT, description="Message content the cient expects in response")
public enum MsgContent {

    @GraphQLEnumValue(description="The complete message")
    full, // The complete message
    @GraphQLEnumValue(description="Only the Message and not quoted text")
    original, // Only the Message and not quoted text
    @GraphQLEnumValue(description="The complete message and also this message without quoted text")
    both; // The complete message and also this message without quoted text

    public static MsgContent fromString(String msgContent) {
        try {
            if (msgContent != null)
                return MsgContent.valueOf(msgContent);
            else
                return null;
        } catch (final Exception e) {
            return null;
        }
    }
}

