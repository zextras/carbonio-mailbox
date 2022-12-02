// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.SearchFolder;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_SEARCH_FOLDER_RESPONSE)
public class GetSearchFolderResponse {

    /**
     * @zm-api-field-description Search folder information
     */
    @XmlElement(name=MailConstants.E_SEARCH, required=false)
    private List<SearchFolder> searchFolders = Lists.newArrayList();

    public GetSearchFolderResponse() {
    }

    public void setSearchFolders(Iterable <SearchFolder> searchFolders) {
        this.searchFolders.clear();
        if (searchFolders != null) {
            Iterables.addAll(this.searchFolders,searchFolders);
        }
    }

    public GetSearchFolderResponse addSearchFolder(SearchFolder searchFolder) {
        this.searchFolders.add(searchFolder);
        return this;
    }

    public List<SearchFolder> getSearchFolders() {
        return Collections.unmodifiableList(searchFolders);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("searchFolders", searchFolders)
            .toString();
    }
}
