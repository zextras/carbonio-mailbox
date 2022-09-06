// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class BounceMsgSpec {

  /**
   * @zm-api-field-tag id-of-msg-to-resend
   * @zm-api-field-description ID of message to resend
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-description Email addresses
   */
  @XmlElement(name = MailConstants.E_EMAIL /* e */, required = false)
  private List<EmailAddrInfo> emailAddresses = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private BounceMsgSpec() {
    this((String) null);
  }

  public BounceMsgSpec(String id) {
    this.id = id;
  }

  public void setEmailAddresses(Iterable<EmailAddrInfo> emailAddresses) {
    this.emailAddresses.clear();
    if (emailAddresses != null) {
      Iterables.addAll(this.emailAddresses, emailAddresses);
    }
  }

  public void addEmailAddresse(EmailAddrInfo emailAddresse) {
    this.emailAddresses.add(emailAddresse);
  }

  public String getId() {
    return id;
  }

  public List<EmailAddrInfo> getEmailAddresses() {
    return Collections.unmodifiableList(emailAddresses);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("emailAddresses", emailAddresses);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
