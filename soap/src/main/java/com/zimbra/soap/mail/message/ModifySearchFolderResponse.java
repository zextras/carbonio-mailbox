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
import com.zimbra.soap.mail.type.SearchFolder;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_MODIFY_SEARCH_FOLDER_RESPONSE)
public class ModifySearchFolderResponse {

    /**
     * @zm-api-field-description Information about search folder, if and only if Search folder was modified.
     */
    @XmlElement(name=MailConstants.E_SEARCH, required=true)
    private final SearchFolder searchFolder;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ModifySearchFolderResponse() {
        this((SearchFolder) null);
    }

    public ModifySearchFolderResponse(SearchFolder searchFolder) {
        this.searchFolder = searchFolder;
    }

    public SearchFolder getSearchFolder() { return searchFolder; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("searchFolder", searchFolder)
            .toString();
    }
}
