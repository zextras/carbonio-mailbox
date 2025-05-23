// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.html.owasp;

import static com.zimbra.cs.html.owasp.HtmlElementsBuilder.COMMA;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.W3cDomUtil;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;

/*
 * Read html elemets, attributes and other owasp configuration from policy file
 * Populate them in respective map/set
 */
public class OwaspPolicy {

    public static final String E_OWASP_POLICY = "owasp_policy";
    public static final String E_DISALLOW_TEXT_IN = "disallow_text_in";
    public static final String E_ALLOW_TEXT_IN = "allow_text_in";
    public static final String E_CSS_WHITELIST = "css_whitelist";
    public static final String E_URL_PROTOCOLS = "url_protocols";
    public static final String E_URL_PROTOCOL_Attrs = "url_protocol_attributes";
    public static final String E_ELEMENT = "element";
    public static final String A_NAME = "name";
    public static final String A_REMOVE_TEXT = "removeText";
    public static final String E_ATTRIBUTES = "attributes";
    private static String mPolicyFile;
    /**
     * The singleton instance. This is a volatile variable, so that we can
     * reload the policy file on the fly without locking.
     */
    private static volatile OwaspPolicy mOwaspPolicy;
    private static final Map<String, String> mConfiguredElements = new HashMap<>();
    private static final Set<String> mDisallowTextElements = new HashSet<>();
    private static final Set<String> mAllowTextElements = new HashSet<>();
    private static final Set<String> mCssWhitelist = new HashSet<>();
    private static final Set<String> mURLProtocols = new HashSet<>();
    private static final Set<String> mURLProtocolAttrs = new HashSet<>();
    private static final Map<String, String> mElementUrlProtocols = new HashMap<>();

    static {
        try {
            load(null);
        } catch (Exception e) {
            ZimbraLog.mailbox.debug("Failed to load OWASP policy file", e);
            ZimbraLog.mailbox.warn("Failed to load OWASP policy file: %s", e.getMessage());
        }
    }

    private OwaspPolicy(String file) throws DocumentException, Exception {
        mPolicyFile = file;
        if (mPolicyFile == null) {
            mPolicyFile = defaultPolicyFile();
        }
        File policyFile = new File(mPolicyFile);
        if (policyFile.exists() && policyFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(policyFile)) {
                Document document = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(fis);
                Element root = document.getRootElement();
                if (!root.getName().equals(E_OWASP_POLICY)) {
                    ZimbraLog.mailbox
                        .warn(String.format("OWASP policy file '%s' root tag is not '%s'",
                            mPolicyFile, E_OWASP_POLICY));
                    throw new DocumentException(
                        String.format("OWASP policy file '%s' root tag is not '%s'", mPolicyFile, E_OWASP_POLICY));
                }
                for (Iterator<?> iter = root.elementIterator(E_ELEMENT); iter.hasNext();) {
                    Element element = (Element) iter.next();
                    String name = element.attributeValue(A_NAME);
                    String attributes = element.elementText(E_ATTRIBUTES);
                    String urlProtocols = element.elementText(E_URL_PROTOCOLS);
                    attributes = attributes.replace("CORE", "id,class,title,style")
                        .replace("LANG", "dir,lang,xml:lang").replace("KBD", "accesshtml,tabindex");
                    set(name, attributes, urlProtocols);
                }
                String disallowTextElements = root.elementText(E_DISALLOW_TEXT_IN);
                if (!StringUtil.isNullOrEmpty(disallowTextElements)) {
                    mDisallowTextElements.addAll(Arrays.asList(disallowTextElements.split(COMMA)));
                }
                String allowTextElements = root.elementText(E_ALLOW_TEXT_IN);
                if (!StringUtil.isNullOrEmpty(allowTextElements)) {
                    mAllowTextElements.addAll(Arrays.asList(allowTextElements.split(COMMA)));
                }
                String cssWhitelist = root.elementText(E_CSS_WHITELIST);
                if (!StringUtil.isNullOrEmpty(cssWhitelist)) {
                    mCssWhitelist.addAll(Arrays.asList(cssWhitelist.split(COMMA)));
                }
                String urlProtocols = root.elementText(E_URL_PROTOCOLS);
                if (!StringUtil.isNullOrEmpty(urlProtocols)) {
                    mURLProtocols.addAll(Arrays.asList(urlProtocols.split(COMMA)));
                }
                String urlProtocolAttrs = root.elementText(E_URL_PROTOCOL_Attrs);
                if (!StringUtil.isNullOrEmpty(urlProtocolAttrs)) {
                    mURLProtocolAttrs.addAll(Arrays.asList(urlProtocolAttrs.split(COMMA)));
                }
            } catch (IOException | XmlParseException e) {
                ZimbraLog.mailbox
                    .warn(String.format("Problem parsing owasp policy file '%s'", policyFile), e);
                throw new DocumentException(
                    String.format("Problem parsing owasp policy file '%s'", policyFile));
            }
            ZimbraLog.mailbox.info("OWASP policy '%s' loaded", mPolicyFile);
        } else {
            ZimbraLog.mailbox
                .warn(String.format("Owasp policy file '%s' is not readable", mPolicyFile));
            throw new Exception(String.format("Owasp policy file '%s' is not readable", mPolicyFile));
        }
    }

    private String defaultPolicyFile() {
        return LC.owasp_policy_file.value();
    }

    private void set(String key, String value, String urlProtocols) {
        mConfiguredElements.put(key, value);
        mElementUrlProtocols.put(key, urlProtocols);
    }

    static OwaspPolicy getInstance() {
        return mOwaspPolicy;
    }

    /**
     * Loads the OWASP policy file.
     *
     * @param path policy file path or null to use the default path
     * @throws DocumentException if the policy file was syntactically invalid
     * @throws Exception if the policy file was semantically invalid
     *             
     */
    static synchronized void load(String path) throws DocumentException, Exception {
        mOwaspPolicy = new OwaspPolicy(path);
    }

    public static Set<String> getAllowedElements() {
        return mConfiguredElements.keySet();
    }

    public static String getAttributes(String element) {
        return mConfiguredElements.get(element);
    }

    public static String getElementUrlProtocols(String element) {
        return mElementUrlProtocols.get(element);
    }

    public static Set<String> getDisallowTextElements() {
        return mDisallowTextElements;
    }

    public static Set<String> getAllowTextElements() {
        return mAllowTextElements;
    }

    public static Set<String> getCssWhitelist() {
        return mCssWhitelist;
    }

    public static Set<String> getURLProtocols() {
        return mURLProtocols;
    }

    public static Set<String> getElementUrlProtocolAttributes() {
        return mURLProtocolAttrs;
    }
}
