// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.soap;

import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.HeaderConstants;
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

  /**
   * Extracts the session ID from a SOAP response header.
   *
   * @param response the SOAP response
   * @return the session ID, or null if not present
   * @throws Exception if parsing fails
   */
  public static String getSessionId(SoapResponse response) throws Exception {
    Element envelope = Element.parseXML(response.body());
    Element header = envelope.getOptionalElement("Header");
    if (header != null) {
      Element context = header.getOptionalElement(HeaderConstants.CONTEXT);
      if (context != null) {
        Element session = context.getOptionalElement(HeaderConstants.E_SESSION);
        if (session != null) {
          // Try to get from 'id' attribute first
          String sessionId = session.getAttribute(HeaderConstants.A_ID, null);
          if (sessionId == null) {
            // Try to get from element text
            sessionId = session.getTextTrim();
          }
          return sessionId;
        }
      }
    }
    return null;
  }
}
