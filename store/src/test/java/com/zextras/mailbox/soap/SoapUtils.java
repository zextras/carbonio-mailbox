// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.soap;

import org.apache.http.HttpResponse;

public class SoapUtils {

  public static String getResponse(HttpResponse response) throws Exception {
    return new String (response.getEntity().getContent().readAllBytes());
  }
}
