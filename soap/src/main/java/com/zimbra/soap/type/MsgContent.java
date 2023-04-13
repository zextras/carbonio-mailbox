// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlEnum;




/**
 * Message Content the client expects in response
 *
 */
@XmlEnum
public enum MsgContent {

    full, // The complete message
    original, // Only the Message and not quoted text
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

