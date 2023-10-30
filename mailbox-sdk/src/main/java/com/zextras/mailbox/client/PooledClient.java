// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.zextras.mailbox.client.requests.Request;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import zimbra.AuthTokenControl;
import zimbra.HeaderContext;
import zimbra.ObjectFactory;

public abstract class PooledClient<Service> implements Client<Service> {
  private final ConcurrentLinkedQueue<Service> servicePool;
  protected final HeaderContext soapHeaderContext;

  public PooledClient(List<Service> services) {
    this.servicePool = new ConcurrentLinkedQueue<>(services);
    AuthTokenControl tokenControl = new AuthTokenControl();
    tokenControl.setVoidOnExpired(true);
    soapHeaderContext = new HeaderContext();
    soapHeaderContext.setAuthTokenControl(tokenControl);
  }

  @Override
  public <Res> Res send(Request<Service, Res> request) {
    return usingPool(service -> request.call(service, soapHeaderContext));
  }

  private Service getService() {
    Service service = null;
    while (service == null) {
      service = servicePool.poll();
    }
    return service;
  }

  protected <U> U usingPool(Function<Service, U> func) {
    Service service = null;

    try {
      service = getService();
      Header header = createServiceSoapHeader(soapHeaderContext);
      ((WSBindingProvider) service).setOutboundHeaders(header);
      return func.apply(service);
    } finally {
      if (service != null) {
        servicePool.add(service);
      }
    }
  }

  private static Header createServiceSoapHeader(HeaderContext soapHeaderContext) {
    try {
      // Create a SOAP header marshalling the actual header (JAXBElement) into a W3C Document
      Document xmlDocumentHeader =
          DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      JAXBElement<HeaderContext> jaxbElementHeader =
          new ObjectFactory().createContext(soapHeaderContext);

      JAXBContext.newInstance(HeaderContext.class)
          .createMarshaller()
          .marshal(jaxbElementHeader, xmlDocumentHeader);

      return Headers.create(xmlDocumentHeader.getDocumentElement());
    } catch (ParserConfigurationException | JAXBException e) {
      throw new MailboxClientException(e);
    }
  }
}
