// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.mime;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import javax.mail.internet.MimeMessage;

public abstract class MimeProcessor {

  private Boolean sign = false;
  private Boolean encrypt = false;
  private String certId = null;

  public abstract void process(MimeMessage mm, Mailbox mbox) throws ServiceException;

  public Boolean isSign() {
    return sign;
  }

  public void setSign(Boolean sign) {
    this.sign = sign;
  }

  public Boolean isEncrypt() {
    return encrypt;
  }

  public void setEncrypt(Boolean encrypt) {
    this.encrypt = encrypt;
  }

  public String getCertId() {
    return certId;
  }

  public void setCertId(String certId) {
    this.certId = certId;
  }
}
