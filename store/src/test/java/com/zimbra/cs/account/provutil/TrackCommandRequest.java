package com.zimbra.cs.account.provutil;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.DocumentHandler;
import org.dom4j.QName;

import java.util.Map;

public class TrackCommandRequest extends DocumentHandler {
  public TrackCommandRequest(QName qname) {
    super();
  }

  @Override public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    return null;
  }
}
