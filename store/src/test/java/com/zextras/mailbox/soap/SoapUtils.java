// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.soap;

import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.JaxbUtil;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class SoapUtils {

  public static String getResponse(HttpResponse response) throws Exception {
    return EntityUtils.toString(response.getEntity());
  }

  public static <T> T getSoapResponse(SoapResponse response, String bodyKey, Class<T> expected) throws Exception {
    return JaxbUtil.elementToJaxb(Element.parseXML(response.body()).getElement("Body").getElement(
        bodyKey), expected);
  }

  public static <T> T getSoapResponse(String response, String bodyKey, Class<T> expected) throws Exception {
    return JaxbUtil.elementToJaxb(Element.parseXML(response).getElement("Body").getElement(
        bodyKey), expected);
  }
}
