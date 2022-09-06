// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.service;

@SuppressWarnings("serial")
public class DeliveryServiceException extends ServiceException {
  public static final String DELIVERY_REJECTED = "deliveryRejected.DELIVERY_REJECTED";

  private DeliveryServiceException(String message, String code, boolean isReceiversFault) {
    super(message, code, isReceiversFault);
  }

  private DeliveryServiceException(
      String message, String code, boolean isReceiversFault, Throwable cause) {
    super(message, code, isReceiversFault, cause);
  }

  public static DeliveryServiceException DELIVERY_REJECTED(String message, Throwable cause) {
    return new DeliveryServiceException(
        "Message delivery refused, reason: " + message, DELIVERY_REJECTED, SENDERS_FAULT, cause);
  }
}
