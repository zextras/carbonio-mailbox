// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.BulkAction;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description SearchAction
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SEARCH_ACTION_REQUEST)
public final class SearchActionRequest {

    /**
     * @zm-api-field-tag search-request
     * @zm-api-field-description Search request
     */
    @XmlElement(name = MailConstants.E_SEARCH_REQUEST /* SearchRequest */ , required = true)
    private SearchRequest searchRequest;

    /**
     * @zm-api-field-tag bulk-action
     * @zm-api-field-description Bulk action
     */
    @XmlElement(name = MailConstants.E_BULK_ACTION /* BulkAction */, required = true)
    private BulkAction bulkAction;

    public SearchRequest getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(SearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    public BulkAction getBulkAction() {
        return bulkAction;
    }

    public void setBulkAction(BulkAction bulkAction) {
        this.bulkAction = bulkAction;
    }
}
