// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.QName;

/**
 * @since May 26, 2004
 * @author schemers
 */
public final class DocumentDispatcher {

  private static final String REQUEST_SUFFIX = "Request";
  private static final String RESPONSE_SUFFIX = "Response";

  private final Map<QName, DocumentHandler> handlers;
  private final Map<QName, QName> responses;
  private ArrayList<String> includeList;

  DocumentDispatcher() {
    handlers = new HashMap<QName, DocumentHandler>();
    responses = new HashMap<QName, QName>();

    String whiteListString = LC.get("admin_soap_white_list");
    ZimbraLog.soap.debug("Loading admin SOAP white list");
    if (whiteListString.length() > 0) {
      includeList = new ArrayList<String>(Arrays.asList(whiteListString.split("(,[\n\r]*)")));
    } else {
      ZimbraLog.soap.debug(
          "No white list found. All available admin SOAP handlers will be loaded.");
    }
  }

  void clearSoapWhiteList() {
    if (includeList != null) {
      ZimbraLog.soap.debug("clearing admin SOAP white list");
      includeList.clear();
    }
  }

  public void registerHandler(QName qname, DocumentHandler handler) {
    if (handler instanceof AdminDocumentHandler) {
      if (!(includeList == null)
          && !includeList.isEmpty()
          && !includeList.contains(
              String.format("%s::%s", qname.getNamespaceURI(), qname.getQualifiedName()))) {
        ZimbraLog.soap.debug("skipping %s::%s", qname.getNamespaceURI(), qname.getQualifiedName());
        return;
      }
      ZimbraLog.soap.debug("Registering %s::%s", qname.getNamespaceURI(), qname.getQualifiedName());
    }

    handlers.put(qname, handler);
    QName respQName = responses.get(qname);
    if (respQName == null) {
      String reqName = qname.getName();
      String respName;
      if (reqName.endsWith(REQUEST_SUFFIX)) {
        respName =
            reqName.substring(0, reqName.length() - REQUEST_SUFFIX.length()) + RESPONSE_SUFFIX;
      } else {
        respName = reqName + RESPONSE_SUFFIX;
      }
      respQName = new QName(respName, qname.getNamespace());
      responses.put(qname, respQName);
    }
    handler.setResponseQName(respQName);
  }

  public void unRegisterHandler(QName qname) {
    DocumentHandler qHandler = handlers.get(qname);
    if (qHandler != null) {
      handlers.remove(qname);
    }

    QName respQName = responses.get(qname);
    if (respQName != null) {
      responses.remove(qname);
    }
  }

  DocumentHandler getHandler(Element doc) {
    DocumentHandler handler = handlers.get(doc.getQName());
    return handler;
  }

  public Map<QName, DocumentHandler> getHandlers() {
    return Collections.unmodifiableMap(handlers);
  }
}
