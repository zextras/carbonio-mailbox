// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.NONE)
public class TwoFactorCredentials {

  @XmlElement(name = AccountConstants.E_TWO_FACTOR_SECRET)
  private String sharedSecret;

  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = AccountConstants.E_TWO_FACTOR_SCRATCH_CODES)
  @XmlElement(name = AccountConstants.E_TWO_FACTOR_SCRATCH_CODE, type = String.class)
  private List<String> scratchCodes;

  public String getSharedSecret() {
    return sharedSecret;
  }

  public void setSharedSecret(String secret) {
    this.sharedSecret = secret;
  }

  public List<String> getScratchCodes() {
    return scratchCodes;
  }

  public void setScratchCodes(List<String> codes) {
    scratchCodes = codes;
  }
}
