// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

import org.apache.commons.codec.digest.DigestUtils;

public class TrackingUtil {

  public static String anonymize(String value) {
    return new DigestUtils("SHA-256").digestAsHex(value);
  }

}
