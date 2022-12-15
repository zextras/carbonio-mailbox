// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ModifySearchFolderSpec;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify Search Folder
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_MODIFY_SEARCH_FOLDER_REQUEST)
public class ModifySearchFolderRequest {

    /**
     * @zm-api-field-description Specification of Search folder modifications
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_SEARCH /* search */, required=true)
    private final ModifySearchFolderSpec searchFolder;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ModifySearchFolderRequest() {
        this((ModifySearchFolderSpec) null);
    }

    public ModifySearchFolderRequest(ModifySearchFolderSpec searchFolder) {
        this.searchFolder = searchFolder;
    }

    public ModifySearchFolderSpec getSearchFolder() { return searchFolder; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("searchFolder", searchFolder)
            .toString();
    }
}
