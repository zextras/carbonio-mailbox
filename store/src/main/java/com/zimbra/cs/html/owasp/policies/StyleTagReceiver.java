// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import java.util.List;

import org.owasp.html.HtmlStreamEventReceiver;

public class StyleTagReceiver implements HtmlStreamEventReceiver {

    private static final String STYLE_TAG_ELEMENT_NAME = "style";
  private final HtmlStreamEventReceiver wrapped;
    private boolean inStyleTag;



    public StyleTagReceiver(HtmlStreamEventReceiver wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void openDocument() {
        wrapped.openDocument();
        inStyleTag = false;
    }

    @Override
    public void closeDocument() {
        wrapped.closeDocument();
    }

    @Override
    public void openTag(String elementName, List<String> attrs) {
        wrapped.openTag(elementName, attrs);
        inStyleTag = STYLE_TAG_ELEMENT_NAME.equalsIgnoreCase(elementName);
    }

    @Override
    public void closeTag(String elementName) {
        wrapped.closeTag(elementName);
        inStyleTag = false;
    }

    @Override
    public void text(String text) {
        if (inStyleTag) {
            String sanitizedStyle = "/*removed by owasp*/";
            wrapped.text(sanitizedStyle);
        } else {
            wrapped.text(text);
        }
    }
}
