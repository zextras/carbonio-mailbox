// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Browse
 * <br />
 * EXAMPLE:
 * <pre>
 *     &lt;mail:BrowseRequest xmlns:mail="urn:zimbraMail">
 *        &lt;query>from:roland&lt;/query>
 *        &lt;browseBy>attachments&lt;/browseBy>
 *     &lt;/mail:BrowseRequest>
 *
 *     &lt;mail:BrowseResponse xmlns:mail="urn:zimbraMail">
 *       &lt;bd freq="3">application/pdf&lt;/bd>
 *       &lt;bd freq="1">application/msword&lt;/bd>
 *       &lt;bd freq="1">application/vnd.ms-powerpoint&lt;/bd>
 *       &lt;bd freq="8">image/jpeg&lt;/bd>
 *       &lt;bd freq="11">application/octet-stream&lt;/bd>
 *       &lt;bd freq="23">text/plain&lt;/bd>
 *       &lt;bd freq="1">image/gif&lt;/bd>
 *       &lt;bd freq="12">message/rfc822&lt;/bd>
 *     &lt;/mail:BrowseResponse>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_BROWSE_REQUEST)
public class BrowseRequest {

    // Valid values are case insensitive values from BrowseBy
    /**
     * @zm-api-field-tag browse-by-domains|attachments|objects
     * @zm-api-field-description Browse by setting - <b>domains|attachments|objects</b>
     * <br/>where <b>objects</b> means objects in message body content recognized by Zimlets via &lt;contentObject&gt;
     * configuration.
     */
    @XmlAttribute(name=MailConstants.A_BROWSE_BY, required=true)
    private final String browseBy;

    /**
     * @zm-api-field-tag regex-string
     * @zm-api-field-description Regex string.  Return only those results which match the specified regular expression
     */
    @XmlAttribute(name=MailConstants.A_REGEX, required=false)
    private final String regex;

    /**
     * @zm-api-field-tag max-entries
     * @zm-api-field-description Return only a maximum number of entries as requested.  If more than
     * <b>{max-entries}</b> results exist, the server will return the first {max-entries}, sorted by frequency
     */
    @XmlAttribute(name=MailConstants.A_MAX_TO_RETURN, required=false)
    private final Integer max;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BrowseRequest() {
        this(null, null, null);
    }

    public BrowseRequest(String browseBy, String regex, Integer max) {
        this.browseBy = browseBy;
        this.regex = regex;
        this.max = max;
    }

    public String getBrowseBy() { return browseBy; }
    public String getRegex() { return regex; }
    public Integer getMax() { return max; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("browseBy", browseBy)
            .add("regex", regex)
            .add("max", max)
            .toString();
    }
}
