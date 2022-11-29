// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Joiner;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.InfoSection;

import io.leangen.graphql.annotations.GraphQLInputField;

/**
 * <GetInfoRequest [sections="mbox,prefs,attrs,zimlets,props,idents,sigs,dsrcs,children"]/>
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get information about an account.
 * @zm-api-request-description By default, GetInfo returns all data; to limit the returned data, specify only the
 *     sections you want in the "sections" attr.
 */
@XmlRootElement(name=AccountConstants.E_GET_INFO_REQUEST)
public class GetInfoRequest {
    private static Joiner COMMA_JOINER = Joiner.on(",");

    private List<InfoSection> sections = new ArrayList<InfoSection>();

    private List<String> rights = new ArrayList<String>();

    public GetInfoRequest() {
    }

    public GetInfoRequest(Iterable<InfoSection> sections) {
        addSections(sections);
    }

    /**
     * @zm-api-field-description Comma separated list of sections to return information about.
     * <br />
     * Sections are: mbox,prefs,attrs,zimlets,props,idents,sigs,dsrcs,children
     */
    @XmlAttribute(name=AccountConstants.A_SECTIONS /* sections */, required=false)
    public String getSections() {
        return COMMA_JOINER.join(sections);
    }

    /**
     * @zm-api-field-description comma-separated-rights
     * @zm-api-field-description Comma separated list of rights to return information about.
     */
    @XmlAttribute(name=AccountConstants.A_RIGHTS)
    public String getRights() {
        return COMMA_JOINER.join(rights);
    }

    @GraphQLInputField(name=GqlConstants.SECTIONS, description="Comma separated list of sections to return information about")
    public GetInfoRequest setSections(String sections)
    throws ServiceException {
        this.sections.clear();
        if (sections != null) {
            addSections(sections.split(","));
        }
        return this;
    }

    public GetInfoRequest addSection(String sectionName)
    throws ServiceException {
        addSection(InfoSection.fromString(sectionName));
        return this;
    }

    public GetInfoRequest addSection(InfoSection section) {
        sections.add(section);
        return this;
    }

    public GetInfoRequest addSections(String ... sectionNames)
    throws ServiceException {
        for (String sectionName : sectionNames) {
            addSection(sectionName);
        }
        return this;
    }

    public GetInfoRequest addSections(Iterable<InfoSection> sections) {
        if (sections != null) {
            for (InfoSection section : sections) {
                addSection(section);
            }
        }
        return this;
    }

    @GraphQLInputField(name=GqlConstants.RIGHTS, description="Comma separated list of rights to return information about")
    public void setRights(String rights)
    throws ServiceException {
        setRights(rights.split(","));
    }

    public GetInfoRequest setRights(String... rights)
    throws ServiceException {
        this.rights.clear();
        if (rights != null) {
            for (String right : rights) {
                addRight(right);
            }
        }
        return this;
    }

    public GetInfoRequest addRight(String right)
    throws ServiceException {
        rights.add(right);
        return this;
    }
}
