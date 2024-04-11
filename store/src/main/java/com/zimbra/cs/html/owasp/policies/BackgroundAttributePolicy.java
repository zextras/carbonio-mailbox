// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import java.net.URI;
import java.net.URISyntaxException;

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only
import org.owasp.html.AttributePolicy;

import com.zimbra.cs.html.owasp.OwaspHtmlSanitizer;

public class BackgroundAttributePolicy implements AttributePolicy {

    @Override
    public String apply(String elementName, String attributeName, String bgValue) {
        String base = OwaspHtmlSanitizer.zThreadLocal.get().getBaseHref();

        if (base != null && bgValue != null) {
            URI baseHrefURI = null;
            try {
                baseHrefURI = new URI(base);
            } catch (URISyntaxException e) {
                if (!base.endsWith("/"))
                    base += "/";
            }
            if (!bgValue.contains(":")) {
                if (!bgValue.startsWith("/")) {
                    bgValue = "/" + bgValue;
                }

                if (baseHrefURI != null) {
                    try {
                        bgValue = baseHrefURI.resolve(bgValue).toString();
                        return bgValue;
                    } catch (IllegalArgumentException e) {
                        // ignore and do string-logic
                    }
                }
                bgValue = base + bgValue;
            }
        }
        return bgValue;
    }

}
