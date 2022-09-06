// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Signature;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create a signature.
 *     <p>If an id is provided it will be honored as the id for the signature.
 *     <p>CreateSignature will set account default signature to the signature being created if there
 *     is currently no default signature for the account.
 *     <p>There can be at most one text/plain signatue and one text/html signature.
 *     <b>{contact-id}</b> contact id associated with this signature
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_CREATE_SIGNATURE_REQUEST)
public class CreateSignatureRequest {

  /**
   * @zm-api-field-description Details of the signature to be created
   */
  @XmlElement(name = AccountConstants.E_SIGNATURE, required = true)
  private final Signature signature;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CreateSignatureRequest() {
    this((Signature) null);
  }

  public CreateSignatureRequest(Signature signature) {
    this.signature = signature;
  }

  public Signature getSignature() {
    return signature;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("signature", signature).toString();
  }
}
