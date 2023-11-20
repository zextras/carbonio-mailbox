// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= MailConstants.E_FULL_AUTO_COMPLETE_REQUEST)
public class FullAutocompleteRequest {

  public FullAutocompleteRequest() {
  }

  public FullAutocompleteRequest(AutoCompleteRequest autoCompleteRequest) {
    this.autoCompleteRequest = autoCompleteRequest;
  }


  @XmlElement(name=MailConstants.E_AUTO_COMPLETE_REQUEST, required=true)
  private AutoCompleteRequest autoCompleteRequest;

  public AutoCompleteRequest getAutoCompleteRequest() {
    return autoCompleteRequest;
  }

  public void setAutoCompleteRequest(AutoCompleteRequest autoCompleteRequest) {
    this.autoCompleteRequest = autoCompleteRequest;
  }

  /**
   * @zm-api-field-tag extraAccountIds
   * @zm-api-field-description Extra accounts where to execute the autocomplete on
   */
  @XmlElement(name=MailConstants.E_ACCOUNT, required=false)
  private final List<String> extraAccountIds = new ArrayList<>();

  public void addAccount(String account) {
    this.extraAccountIds.add(account);
  }

  public List<String> getExtraAccountIds() {
    return extraAccountIds;
  }
}
