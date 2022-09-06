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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Delete a signature
 *     <p>must specify either <b>{name}</b> or <b>{id}</b> attribute to <b>&lt;signature></b>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_DELETE_SIGNATURE_REQUEST)
public class DeleteSignatureRequest {

  /**
   * @zm-api-field-description The signature to delete
   */
  @XmlElement(name = AccountConstants.E_SIGNATURE, required = true)
  private final NameId signature;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DeleteSignatureRequest() {
    this((NameId) null);
  }

  public DeleteSignatureRequest(NameId signature) {
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
