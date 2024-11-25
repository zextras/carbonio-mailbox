package com.zimbra.cs.account.provutil;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.CreateAccountResponse;
import com.zimbra.soap.admin.message.CreateXMPPComponentResponse;
import com.zimbra.soap.admin.message.GetDomainResponse;
import com.zimbra.soap.admin.message.GetServerResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import com.zimbra.soap.admin.type.DomainInfo;
import com.zimbra.soap.admin.type.ServerInfo;
import com.zimbra.soap.admin.type.XMPPComponentInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TrackCommandRequestHandler extends DocumentHandler {
  private static AtomicReference<List<String>> command = new AtomicReference<>();

  public static void setCommand(String... cmd) {
      command.set(Arrays.asList(cmd));
  }

  public static void reset() {
    command.set(null);
  }

  Map<String, Supplier<Element>> responseMapping = new HashMap<>();
  {
    responseMapping.put("CreateAccountRequest", () -> {
      CreateAccountResponse resp = new CreateAccountResponse();
      resp.setAccount(new AccountInfo("0723fe7d-a381-4669-8522-f1a57ac4bfb7", "name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDomainRequest", () -> {
      GetDomainResponse resp = new GetDomainResponse(new DomainInfo("0723fe7d-a381-4669-8522-f1a57ac4bfb7", "name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetServerRequest", () -> {
      GetServerResponse resp = new GetServerResponse(new ServerInfo("0723fe7d-a381-4669-8522-f1a57ac4bfb7", "name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateXMPPComponentRequest", () -> {
      CreateXMPPComponentResponse resp = new CreateXMPPComponentResponse(new XMPPComponentInfo("0723fe7d-a381-4669-8522-f1a57ac4bfb7", "name"));
      return jaxbToElement(resp);
    });
  }

  private static Element jaxbToElement(Object resp) {
    try {
      return JaxbUtil.jaxbToElement(resp);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }


  public TrackCommandRequestHandler() {
    super();
  }

  @Override public boolean needsAuth(Map<String, Object> context) {
    return false;
  }

  @Override public boolean needsAdminAuth(Map<String, Object> context) {
    return false;
  }

  @Override public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    System.out.println(request);
    System.out.println(command.get());
    Supplier<Element> elementSupplier = responseMapping.get(request.getName());
    if (elementSupplier != null) {
      return elementSupplier.get();
    } else {
      return Element.XMLElement.parseXML("<response />");
    }
  }
}
