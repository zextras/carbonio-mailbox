// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_SPELL_DICTIONARIES_RESPONSE)
public class GetSpellDictionariesResponse {

  /**
   * @zm-api-field-tag dictionaries
   * @zm-api-field-description Dictionaries
   */
  @XmlElement(name = MailConstants.E_DICTIONARY, required = false)
  private List<String> dictionaries = Lists.newArrayList();

  public GetSpellDictionariesResponse() {}

  public void setDictionaries(Iterable<String> dictionaries) {
    this.dictionaries.clear();
    if (dictionaries != null) {
      Iterables.addAll(this.dictionaries, dictionaries);
    }
  }

  public GetSpellDictionariesResponse addDictionary(String dictionary) {
    this.dictionaries.add(dictionary);
    return this;
  }

  public List<String> getDictionaries() {
    return Collections.unmodifiableList(dictionaries);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("dictionaries", dictionaries);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
