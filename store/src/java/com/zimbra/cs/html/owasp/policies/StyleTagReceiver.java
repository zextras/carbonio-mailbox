// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp.policies;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;

public class StyleTagReceiver implements HtmlStreamEventReceiver {

    private static final String STYLE_TAG = "style";
    private static final String STYLE_OPENING_TAG = "<style>";
    private static final String STYLE_CLOSING_TAG = "</style>";
    private static final AntiSamy as = new AntiSamy();
    private static Policy policy = null;
    private final HtmlStreamEventReceiver wrapped;
    private boolean inStyleTag;

    static {
        final String FS = File.separator;
        String antisamyXML = LC.zimbra_home.value() + FS + "conf" + FS + "antisamy.xml";
        File myFile = new File(antisamyXML);
        try {
            URL url = myFile.toURI().toURL();
            policy = Policy.getInstance(url);
            ZimbraLog.mailbox.info("Antisamy policy loaded");
        } catch (PolicyException | MalformedURLException e) {
            ZimbraLog.mailbox.debug("Failed to load antisamy policy", e);
            ZimbraLog.mailbox.warn("Failed to load antisamy policy: %s", e.getMessage());
        }
    }

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
        inStyleTag = STYLE_TAG.equalsIgnoreCase(elementName);
    }

    @Override
    public void closeTag(String elementName) {
        wrapped.closeTag(elementName);
        inStyleTag = false;
    }

    @Override
    public void text(String text) {
        if (inStyleTag) {
            String sanitizedStyle = "";
            if (policy != null) {
                try {
                    sanitizedStyle = as
                        .scan(STYLE_OPENING_TAG + text + STYLE_CLOSING_TAG, policy, AntiSamy.DOM)
                        .getCleanHTML();
                    sanitizedStyle = sanitizedStyle.replace(STYLE_OPENING_TAG, "")
                        .replace(STYLE_CLOSING_TAG, "");
                    sanitizedStyle = sanitizedStyle.replace("<![CDATA[", "/*<![CDATA[*/");
                    sanitizedStyle = sanitizedStyle.replace("]]>", "/*]]>*/");
                } catch (ScanException | PolicyException e) {
                    ZimbraLog.mailbox.debug("Failed to sanitize html style element", e);
                    ZimbraLog.mailbox.warn("Failed to sanitize html style element: %s", e.getMessage());
                    sanitizedStyle = "";
                }
            }
            wrapped.text(sanitizedStyle);
        } else {
            wrapped.text(text);
        }
    }
}
