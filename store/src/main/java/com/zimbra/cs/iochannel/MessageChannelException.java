// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.iochannel;

import com.zimbra.common.iochannel.IOChannelException;

public class MessageChannelException extends IOChannelException {

  private static final long serialVersionUID = -3595831838657110474L;

  public MessageChannelException(String msg) {
    super(Code.Error, msg);
  }

  public static MessageChannelException NoSuchMessage(String message) {
    return new MessageChannelException("message doesn't exist " + message);
  }

  public static MessageChannelException CannotCreate(String error) {
    return new MessageChannelException(error);
  }
}
