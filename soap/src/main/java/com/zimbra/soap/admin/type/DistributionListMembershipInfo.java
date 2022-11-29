// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import com.zimbra.common.soap.AdminConstants;

/**
 * Information about a DL that another particular DL is a member of
 */
@XmlAccessorType(XmlAccessType.NONE)
public class DistributionListMembershipInfo {

    /**
     * @zm-api-field-tag dl-id
     * @zm-api-field-description Distribution list ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final String id;

    /**
     * @zm-api-field-tag dl-name
     * @zm-api-field-description Distribution list name
     */
    @XmlAttribute(name=AdminConstants.A_NAME, required=true)
    private final String name;

    /**
     * @zm-api-field-tag via-dl-name
     * @zm-api-field-description Present if the dl is a member of the returned list because they are either a direct
     * or indirect member of another list that is a member of the returned list.
     * For example, if a dl is a member of engineering@domain.com, and engineering@domain.com is a member of
     * all@domain.com, then 
     * <pre>
     * &lt;dl name="all@domain.com" ... via="engineering@domain.com"/>
     * </pre>
     * would be returned.
     */
    @XmlAttribute(name=AdminConstants.A_VIA, required=false)
    private final String via;
    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DistributionListMembershipInfo() {
        this(null, null, null);
    }

    public DistributionListMembershipInfo(String id, String name) {
        this(id, name, null);
    }

    public DistributionListMembershipInfo(String id, String name, String via) {
        this.id = id;
        this.name = name;
        this.via = via;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getVia() { return via; }
}
