// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.clam.client.exceptions;

/**
 * Thrown if clamd size limit is exceeded during scanning
 *
 * <p>
 *
 * @author Keshav Bhatt
 * @since 23.7.0
 */
public class ClamAVSizeLimitException extends RuntimeException {

  public ClamAVSizeLimitException(String msg) {
    super(msg);
  }
}
