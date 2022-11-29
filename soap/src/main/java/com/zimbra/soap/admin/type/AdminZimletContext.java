// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.base.ZimletContextInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class AdminZimletContext
implements ZimletContextInterface {

    /**
     * @zm-api-field-tag zimlet-base-url
     * @zm-api-field-description Zimlet Base URL
     */
    @XmlAttribute(name=AccountConstants.A_ZIMLET_BASE_URL /* baseUrl */, required=true)
    private String zimletBaseUrl;

    /**
     * @zm-api-field-tag zimlet-priority
     * @zm-api-field-description Zimlet Priority
     */
    @XmlAttribute(name=AccountConstants.A_ZIMLET_PRIORITY /* priority */, required=false)
    private Integer zimletPriority;

    /**
     * @zm-api-field-tag zimlet-presence
     * @zm-api-field-description Zimlet presence
     * <br />
     * Valid values: <b>mandatory</b> | <b>enabled</b> | <b>disabled</b>
     */
    @XmlAttribute(name=AccountConstants.A_ZIMLET_PRESENCE /* presence */, required=true)
    private String zimletPresence;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private AdminZimletContext() {
        this((String) null, (Integer) null, (String) null);
    }

    public AdminZimletContext(String zimletBaseUrl, Integer zimletPriority,
                            String zimletPresence) {
        this.setZimletBaseUrl(zimletBaseUrl);
        this.setZimletPriority(zimletPriority);
        this.setZimletPresence(zimletPresence);
    }

    public static AdminZimletContext createForBaseUrlPriorityAndPresence(
            String zimletBaseUrl, Integer zimletPriority, String zimletPresence) {
        return new AdminZimletContext(zimletBaseUrl, zimletPriority, zimletPresence);
    }

    @Override
    public void setZimletBaseUrl(String zimletBaseUrl) { this.zimletBaseUrl = zimletBaseUrl; }
    @Override
    public void setZimletPriority(Integer zimletPriority) { this.zimletPriority = zimletPriority; }
    @Override
    public void setZimletPresence(String zimletPresence) { this.zimletPresence = zimletPresence; }

    @Override
    public String getZimletBaseUrl() { return zimletBaseUrl; }
    @Override
    public Integer getZimletPriority() { return zimletPriority; }
    @Override
    public String getZimletPresence() { return zimletPresence; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("zimletBaseUrl", getZimletBaseUrl())
            .add("zimletPriority", getZimletPriority())
            .add("zimletPresence", getZimletPresence());
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
