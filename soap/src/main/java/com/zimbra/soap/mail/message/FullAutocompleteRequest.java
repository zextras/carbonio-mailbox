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
 * @zm-api-command-description Retrieves AutoComplete matches from multiple source Accounts defined by
 * 'orderedAccountIds'. The ordering logic ensures that the most relevant results are returned based on the values
 * supplied in the 'orderedAccountIds' element.
 *
 * <p>
 * The sorting/ordering and limiting algorithm works as follows:
 * <ul>
 *     <li>Matches from the most preferred source account (the first accountId in 'orderedAccountIds') are given higher priority and are placed on top.</li>
 *     <li>Matches from other source accounts are sorted primarily based on ranking, followed by alphabetical ordering.</li>
 *     <li>Duplicate matches from other source accounts are omitted, with comparison against matches from the preferred source account.</li>
 *     <li>If the optional 'orderedAccountIds' element is not passed, matches from the authenticated Account are returned, which is equivalent to a regular AutoCompleteRequest.</li>
 *     <li>The number of matches returned depends on the value set for authenticated account's ContactAutoCompleteMaxResults.</li>
 * </ul>
 * </p>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_FULL_AUTO_COMPLETE_REQUEST)
public class FullAutocompleteRequest {

  @XmlElement(name = MailConstants.E_AUTO_COMPLETE_REQUEST, required = true)
  private AutoCompleteRequest autoCompleteRequest;

  /**
   * Represents an ordered, comma-separated list of account IDs whose autocomplete matches will be included in the
   * {@link FullAutocompleteResponse}.
   *
   * <p>
   * This field is used to specify the accounts from which autocomplete matches should be retrieved. The order of the
   * account IDs determines the preference order for autocomplete matches.
   * </p>
   *
   * @zm-api-field-tag orderedAccountIds
   * @zm-api-field-description ordered, comma-separated list of account IDs whose matches will be included in the
   * FullAutocompleteResponse.
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
