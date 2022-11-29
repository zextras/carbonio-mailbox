// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class DocumentActionGrant {

    /**
     * @zm-api-field-tag rights-rwd
     * @zm-api-field-description Permissions - (r)ead, (w)rite, (d)elete
     */
    @XmlAttribute(name=MailConstants.A_RIGHTS /* perm */, required=true)
    private String rights;

    /**
     * @zm-api-field-tag grant-type-all|pub
     * @zm-api-field-description Grant type - <b>all|pub</b>
     */
    @XmlAttribute(name=MailConstants.A_GRANT_TYPE /* gt */, required=true)
    private String grantType;

    /**
     * @zm-api-field-tag expiry-millis
     * @zm-api-field-description (Optional) Time when this grant expires in milliseconds since the Epoch
     */
    @XmlAttribute(name=MailConstants.A_EXPIRY /* expiry */, required=false)
    private Long expiry;

    public DocumentActionGrant() {
        this((String)null, (String)null, (Long)null);
    }

    private DocumentActionGrant(String rights, String grantType) {
        this(rights, grantType, (Long)null);
    }

    private DocumentActionGrant(String rights, String grantType, Long expiry) {
        setRights(rights);
        setGrantType(grantType);
        setExpiry(expiry);
    }

    public static DocumentActionGrant createForRightsAndGrantType(String rights, String grantType) {
        return new DocumentActionGrant(rights, grantType);
    }

    public static DocumentActionGrant createForRightsGrantTypeAndExpiry(String rights, String grantType, Long expiry) {
        return new DocumentActionGrant(rights, grantType, expiry);
    }

    public void setRights(String rights) { this.rights = rights; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
    public void setExpiry(Long expiry) { this.expiry = expiry; }
    public String getRights() { return rights; }
    public String getGrantType() { return grantType; }
    public Long getExpiry() { return expiry; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("rights", rights)
            .add("grantType", grantType)
            .add("expiry", expiry);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
