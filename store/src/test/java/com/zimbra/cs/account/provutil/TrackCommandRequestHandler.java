package com.zimbra.cs.account.provutil;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.CreateIdentityResponse;
import com.zimbra.soap.account.message.CreateSignatureResponse;
import com.zimbra.soap.account.type.Identity;
import com.zimbra.soap.account.type.NameId;
import com.zimbra.soap.admin.message.AddAccountLoggerResponse;
import com.zimbra.soap.admin.message.CheckRightResponse;
import com.zimbra.soap.admin.message.CopyCosResponse;
import com.zimbra.soap.admin.message.CountAccountResponse;
import com.zimbra.soap.admin.message.CreateAccountResponse;
import com.zimbra.soap.admin.message.CreateCalendarResourceResponse;
import com.zimbra.soap.admin.message.CreateCosResponse;
import com.zimbra.soap.admin.message.CreateDataSourceResponse;
import com.zimbra.soap.admin.message.CreateDistributionListResponse;
import com.zimbra.soap.admin.message.CreateDomainResponse;
import com.zimbra.soap.admin.message.CreateServerResponse;
import com.zimbra.soap.admin.message.CreateXMPPComponentResponse;
import com.zimbra.soap.admin.message.GetAccountResponse;
import com.zimbra.soap.admin.message.GetCosResponse;
import com.zimbra.soap.admin.message.GetDistributionListResponse;
import com.zimbra.soap.admin.message.GetDomainResponse;
import com.zimbra.soap.admin.message.GetServerResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.admin.type.CalendarResourceInfo;
import com.zimbra.soap.admin.type.CheckedRight;
import com.zimbra.soap.admin.type.CosCountInfo;
import com.zimbra.soap.admin.type.CosInfo;
import com.zimbra.soap.admin.type.DataSourceInfo;
import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.soap.admin.type.DistributionListInfo;
import com.zimbra.soap.admin.type.DomainInfo;
import com.zimbra.soap.admin.type.GranteeWithType;
import com.zimbra.soap.admin.type.LoggerInfo;
import com.zimbra.soap.admin.type.RightViaInfo;
import com.zimbra.soap.admin.type.ServerInfo;
import com.zimbra.soap.admin.type.TargetWithType;
import com.zimbra.soap.admin.type.XMPPComponentInfo;
import com.zimbra.soap.type.LoggingLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TrackCommandRequestHandler extends DocumentHandler {
  private static final String ACCOUNT_UUID = "186c1c23-d2ad-46b4-9efd-ddd890b1a4a2";
  private static final String ACCOUNT_NAME = "test@test.com";
  private static List<String> command;
  private static List<String> requests = new ArrayList<>();

  public static void setCommand(List<String> cmd) {
      command = new ArrayList<>(cmd);
  }

  public static void reset() {
    command = null;
    requests = new ArrayList<>();
  }

  public static List<String> getRequestString() {
    return requests;
  }

  Map<String, Supplier<Element>> responseMapping = new HashMap<>();
  {
    responseMapping.put("AddAccountLoggerRequest", () -> {
      AddAccountLoggerResponse resp = AddAccountLoggerResponse.create(Arrays.asList(
              LoggerInfo.createForCategoryAndLevel("mycat", LoggingLevel.debug)
      ));
      return jaxbToElement(resp);
    });
    responseMapping.put("CheckRightRequest", () -> {
      CheckRightResponse resp = new CheckRightResponse(true, new RightViaInfo(
              new TargetWithType("target-type", "value"),
              new GranteeWithType("grantee-type", "value"),
              new CheckedRight("value")
      ));
      return jaxbToElement(resp);
    });
    responseMapping.put("CopyCosRequest", () -> {
      var resp = new CopyCosResponse();
      resp.setCos(CosInfo.createForIdAndName("cos-id", "cos-name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CountAccountRequest", () -> {
      var resp = new CountAccountResponse();
      resp.setCos(List.of(new CosCountInfo("cos-id", "cos-name", 42)));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateAccountRequest", () -> {
      CreateAccountResponse resp = new CreateAccountResponse();
      resp.setAccount(new AccountInfo(ACCOUNT_UUID, ACCOUNT_NAME));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateCalendarResourceRequest", () -> {
      var resp = new CreateCalendarResourceResponse(new CalendarResourceInfo("calendarResourceId", "calendarResourceName"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateCosRequest", () -> {
      var resp = new CreateCosResponse();
      resp.setCos(CosInfo.createDefaultCosForIdNameAndAttrs("cos-id", "cos-name", List.of()));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateDataSourceRequest", () -> {
      var resp = new CreateDataSourceResponse(new DataSourceInfo("datasource-name", "datasource-id", DataSourceType.pop3));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateDistributionListRequest", () -> {
      var resp = new CreateDistributionListResponse(new DistributionListInfo("distribution-list-id", "distribution-list-name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateDomainRequest", () -> {
      var resp = new CreateDomainResponse();
      resp.setDomain(new DomainInfo(MailboxTestUtil.DEFAULT_DOMAIN_ID, MailboxTestUtil.DEFAULT_DOMAIN, Arrays.asList(
              new Attr(ZAttrProvisioning.A_zimbraDomainType, "local")
      )));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateIdentityRequest", () -> {
      var resp = new CreateIdentityResponse(new Identity("identity-name", "identity-id"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateServerRequest", () -> {
      var resp = new CreateServerResponse();
      resp.setServer(new ServerInfo("server-id", "server-name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateSignatureRequest", () -> {
      var resp = new CreateSignatureResponse(new NameId("signature-name", "signature-id"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateXMPPComponentRequest", () -> {
      CreateXMPPComponentResponse resp = new CreateXMPPComponentResponse(new XMPPComponentInfo(ACCOUNT_UUID, "name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAccountRequest", () -> {
      GetAccountResponse resp = new GetAccountResponse();
      resp.setAccount(new AccountInfo(ACCOUNT_UUID, ACCOUNT_NAME));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDomainRequest", () -> {
      GetDomainResponse resp = new GetDomainResponse(
              new DomainInfo(MailboxTestUtil.DEFAULT_DOMAIN_ID, MailboxTestUtil.DEFAULT_DOMAIN, Arrays.asList(
                      new Attr(ZAttrProvisioning.A_zimbraDomainType, "local")
              ))
      );
      return jaxbToElement(resp);
    });
    responseMapping.put("GetServerRequest", () -> {
      GetServerResponse resp = new GetServerResponse(new ServerInfo(ACCOUNT_UUID, "name"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDistributionListRequest", () -> {
      var resp = new GetDistributionListResponse();
      resp.setDl(new DistributionListInfo("dlId", "dlName"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetCosRequest", () -> {
      var resp = new GetCosResponse();
      resp.setCos(CosInfo.createForIdAndName("cos-id", "cos-name"));
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
    requests.add(request.toString());
    if (command == null) throw new NullPointerException("command");
    Supplier<Element> elementSupplier = responseMapping.get(request.getName());
    if (elementSupplier != null) {
      return elementSupplier.get();
    } else {
      return Element.XMLElement.parseXML("<Response />");
    }
  }

}
