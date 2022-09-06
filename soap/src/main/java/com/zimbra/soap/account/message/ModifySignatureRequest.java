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
 * @zm-api-command-description Change attributes of the given signature. Only the attributes
 *     specified in the request are modified. <br>
 *     Note: The Server identifies the signature by <b>id</b>, if the <b>name</b> attribute is
 *     present and is different from the current name of the signature, the signature will be
 *     renamed.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_MODIFY_SIGNATURE_REQUEST)
public class ModifySignatureRequest {

  /**
   * @zm-api-field-description Specifies the changes to the signature
   */
  @XmlElement(name = AccountConstants.E_SIGNATURE, required = true)
  private final Signature signature;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ModifySignatureRequest() {
    this((Signature) null);
  }

  public ModifySignatureRequest(Signature signature) {
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
