// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.cs.html.owasp.OwaspDefang;

/**
 * This factory is used to determine the proper defanger based on content type for
 * content that can be natively displayed in most browsers and can have unsavory things
 * added to it (mostly xss script issues).
 * @author jpowers
 *
 */
public class DefangFactory {
    /**
     * The instance of the neko html defanger 
     */
    private static HtmlDefang htmlDefang = new HtmlDefang();
    
    /**
     * The xml defanger, used for xhtml and svg 
     */
    private static XHtmlDefang xhtmlDefang = new XHtmlDefang();

    /**
     * This defanger does nothing. Here for
     * backwards compatibility
     */
    private static NoopDefang noopDefang = new NoopDefang();

    /**
     * The instance of the owasp html defanger
     */
    private static OwaspDefang owaspDefang = new OwaspDefang();
    
    /**
     * if content type is null, returns noopDefang which does nothing
     * 
     * @param contentType
     * @return
     */
    public static BrowserDefang getDefanger(String contentType){
        if(contentType == null) {
            return noopDefang;
        }
        String contentTypeLowerCase = contentType.toLowerCase();
        boolean isOwaspEnabled = LC.zimbra_use_owasp_html_sanitizer.booleanValue();
        if (contentTypeLowerCase.startsWith(MimeConstants.CT_TEXT_HTML) && isOwaspEnabled) {
            return owaspDefang;
        }
        if (contentTypeLowerCase.startsWith(MimeConstants.CT_TEXT_HTML) ||
            contentTypeLowerCase.startsWith(MimeConstants.CT_APPLICATION_ZIMBRA_DOC) ||
            contentTypeLowerCase.startsWith(MimeConstants.CT_APPLICATION_ZIMBRA_SLIDES) ||
            contentTypeLowerCase.startsWith(MimeConstants.CT_APPLICATION_ZIMBRA_SPREADSHEET)) {
            return htmlDefang;
        }

        if(contentTypeLowerCase.startsWith(MimeConstants.CT_TEXT_XML) ||
           contentTypeLowerCase.startsWith(MimeConstants.CT_APPLICATION_XHTML) ||
           contentTypeLowerCase.startsWith(MimeConstants.CT_IMAGE_SVG) ||
           contentTypeLowerCase.startsWith(MimeConstants.CT_TEXT_XML_LEGACY)) {
            return xhtmlDefang;
        }
        return noopDefang;
    }
}
