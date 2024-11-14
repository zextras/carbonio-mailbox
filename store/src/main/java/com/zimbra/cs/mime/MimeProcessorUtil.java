// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import java.util.Map;

public final class MimeProcessorUtil {

    private MimeProcessorUtil() {}

    public static MimeProcessor getMimeProcessor(Element request, Map<String, Object> context) {

        Object mimeProcessor = context.get(MailConstants.A_MIME_PROCESSOR);
        if (mimeProcessor instanceof MimeProcessor mimeProcessorObj) {
            return mimeProcessorObj;
        }
        return null;
    }
}
