// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Attr;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/** <ResetPasswordResponse> </ResetPasswordResponse> */
@XmlRootElement(name = AccountConstants.E_RESET_PASSWORD_RESPONSE /*ResetPasswordResponse*/)
public class ResetPasswordResponse {
  @ZimbraKeyValuePairs
  @XmlElementWrapper(name = AccountConstants.E_ATTRS /* attrs */, required = false)
  @XmlElement(name = AccountConstants.E_ATTR /* attr */, required = false)
  private List<Attr> attrs = Lists.newArrayList();
}
