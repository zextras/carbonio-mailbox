// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.NameId;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_CREATE_SIGNATURE_RESPONSE)
public class CreateSignatureResponse {

  /**
   * @zm-api-field-description Information about created signature
   */
  @XmlElement(name = AccountConstants.E_SIGNATURE, required = true)
  private final NameId signature;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CreateSignatureResponse() {
    this((NameId) null);
  }

  public CreateSignatureResponse(NameId signature) {
    this.signature = signature;
  }

  public NameId getSignature() {
    return signature;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("signature", signature).toString();
  }
}
