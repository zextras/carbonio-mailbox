// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AccountConstants.E_GET_SCRATCH_CODES_RESPONSE)
public class GetScratchCodesResponse {
  @XmlElementWrapper(name = AccountConstants.E_TWO_FACTOR_SCRATCH_CODES)
  @XmlElement(name = AccountConstants.E_TWO_FACTOR_SCRATCH_CODE, type = String.class)
  private List<String> scratchCodes;

  public List<String> getScratchCodes() {
    return scratchCodes;
  }

  public void setScratchCodes(List<String> codes) {
    scratchCodes = codes;
  }
}
