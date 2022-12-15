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
@XmlRootElement(name=MailConstants.E_CREATE_SEARCH_FOLDER_RESPONSE)
public class CreateSearchFolderResponse {

    /**
     * @zm-api-field-description Details of newly created search folder
     */
    @XmlElement(name=MailConstants.E_SEARCH, required=false)
    private SearchFolder searchFolder;

    public CreateSearchFolderResponse() {
    }

    public void setSearchFolder(SearchFolder searchFolder) {
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
