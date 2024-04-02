// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description FullAutoComplete This API executes an AutoComplete on current logged in account and all
 * extra requested accounts. If one of the requests in extra accounts fails, the whole API will fail. The API returns
 * the same content of AutoComplete by merging the result and discarding duplicates following the order of requested
 * accounts, starting from logged user response.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_FULL_AUTO_COMPLETE_REQUEST)
public class FullAutocompleteRequest {

  @XmlElement(name = MailConstants.E_AUTO_COMPLETE_REQUEST, required = true)
  private AutoCompleteRequest autoCompleteRequest;
  /**
   * @zm-api-field-tag orderedAccountIds
   * @zm-api-field-description ordered, comma seperated list of account IDs whose matches will be included in the
   * response.
   */
  @XmlElement(name = MailConstants.E_ORDERED_ACCOUNT_IDS, required = false)
  private String orderedAccountIds;


  public FullAutocompleteRequest() {
  }

  public FullAutocompleteRequest(AutoCompleteRequest autoCompleteRequest) {
    this.autoCompleteRequest = autoCompleteRequest;
  }

  public AutoCompleteRequest getAutoCompleteRequest() {
    return autoCompleteRequest;
  }

  public void setAutoCompleteRequest(AutoCompleteRequest autoCompleteRequest) {
    this.autoCompleteRequest = autoCompleteRequest;
  }

  public String getOrderedAccountIds() {
    return orderedAccountIds;
  }

  public void setOrderedAccountIds(String orderedAccountIds) {
    this.orderedAccountIds = orderedAccountIds;
  }
}
