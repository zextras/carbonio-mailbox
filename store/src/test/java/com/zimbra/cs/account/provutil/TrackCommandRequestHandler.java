package com.zimbra.cs.account.provutil;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.CreateIdentityResponse;
import com.zimbra.soap.account.message.CreateSignatureResponse;
import com.zimbra.soap.account.message.GetDistributionListMembersResponse;
import com.zimbra.soap.account.message.RevokeRightsResponse;
import com.zimbra.soap.account.type.Identity;
import com.zimbra.soap.account.type.NameId;
import com.zimbra.soap.admin.message.AddAccountLoggerResponse;
import com.zimbra.soap.admin.message.AuthResponse;
import com.zimbra.soap.admin.message.CheckRightResponse;
import com.zimbra.soap.admin.message.CompactIndexResponse;
import com.zimbra.soap.admin.message.CopyCosResponse;
import com.zimbra.soap.admin.message.CountAccountResponse;
import com.zimbra.soap.admin.message.CountObjectsResponse;
import com.zimbra.soap.admin.message.CreateAccountResponse;
import com.zimbra.soap.admin.message.CreateCalendarResourceResponse;
import com.zimbra.soap.admin.message.CreateCosResponse;
import com.zimbra.soap.admin.message.CreateDataSourceResponse;
import com.zimbra.soap.admin.message.CreateDistributionListResponse;
import com.zimbra.soap.admin.message.CreateDomainResponse;
import com.zimbra.soap.admin.message.CreateServerResponse;
import com.zimbra.soap.admin.message.GetAccountLoggersResponse;
import com.zimbra.soap.admin.message.GetAccountMembershipResponse;
import com.zimbra.soap.admin.message.GetAccountResponse;
import com.zimbra.soap.admin.message.GetAllAccountLoggersResponse;
import com.zimbra.soap.admin.message.GetAllConfigResponse;
import com.zimbra.soap.admin.message.GetAllCosResponse;
import com.zimbra.soap.admin.message.GetAllDistributionListsResponse;
import com.zimbra.soap.admin.message.GetAllDomainsResponse;
import com.zimbra.soap.admin.message.GetAllEffectiveRightsResponse;
import com.zimbra.soap.admin.message.GetAllRightsResponse;
import com.zimbra.soap.admin.message.GetAllServersResponse;
import com.zimbra.soap.admin.message.GetCalendarResourceResponse;
import com.zimbra.soap.admin.message.GetConfigResponse;
import com.zimbra.soap.admin.message.GetCosResponse;
import com.zimbra.soap.admin.message.GetDistributionListResponse;
import com.zimbra.soap.admin.message.GetDomainInfoResponse;
import com.zimbra.soap.admin.message.GetDomainResponse;
import com.zimbra.soap.admin.message.GetEffectiveRightsResponse;
import com.zimbra.soap.admin.message.GetIndexStatsResponse;
import com.zimbra.soap.admin.message.GetMailboxResponse;
import com.zimbra.soap.admin.message.GetQuotaUsageResponse;
import com.zimbra.soap.admin.message.GetRightResponse;
import com.zimbra.soap.admin.message.GetRightsDocResponse;
import com.zimbra.soap.admin.message.GetServerResponse;
import com.zimbra.soap.admin.message.GetShareInfoResponse;
import com.zimbra.soap.admin.message.ModifyAccountResponse;
import com.zimbra.soap.admin.message.ModifyCalendarResourceResponse;
import com.zimbra.soap.admin.message.ModifyCosResponse;
import com.zimbra.soap.admin.message.ModifyDataSourceResponse;
import com.zimbra.soap.admin.message.ModifyDistributionListResponse;
import com.zimbra.soap.admin.message.ModifyDomainResponse;
import com.zimbra.soap.admin.message.ModifyServerResponse;
import com.zimbra.soap.admin.message.PushFreeBusyResponse;
import com.zimbra.soap.admin.message.RecalculateMailboxCountsResponse;
import com.zimbra.soap.admin.message.SetPasswordResponse;
import com.zimbra.soap.admin.message.UnregisterMailboxMoveOutResponse;
import com.zimbra.soap.admin.message.VerifyIndexResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import com.zimbra.soap.admin.type.AccountLoggerInfo;
import com.zimbra.soap.admin.type.AccountQuotaInfo;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.admin.type.CalendarResourceInfo;
import com.zimbra.soap.admin.type.CheckedRight;
import com.zimbra.soap.admin.type.CosCountInfo;
import com.zimbra.soap.admin.type.CosInfo;
import com.zimbra.soap.admin.type.DLInfo;
import com.zimbra.soap.admin.type.DataSourceInfo;
import com.zimbra.soap.admin.type.DataSourceType;
import com.zimbra.soap.admin.type.DistributionListInfo;
import com.zimbra.soap.admin.type.DomainInfo;
import com.zimbra.soap.admin.type.EffectiveAttrsInfo;
import com.zimbra.soap.admin.type.EffectiveRightsTargetInfo;
import com.zimbra.soap.admin.type.GranteeInfo;
import com.zimbra.soap.admin.type.GranteeWithType;
import com.zimbra.soap.admin.type.IndexStats;
import com.zimbra.soap.admin.type.LoggerInfo;
import com.zimbra.soap.admin.type.MailboxQuotaInfo;
import com.zimbra.soap.admin.type.MailboxWithMailboxId;
import com.zimbra.soap.admin.type.PackageRightsInfo;
import com.zimbra.soap.admin.type.RightInfo;
import com.zimbra.soap.admin.type.RightViaInfo;
import com.zimbra.soap.admin.type.ServerInfo;
import com.zimbra.soap.admin.type.TargetWithType;
import com.zimbra.soap.type.GranteeType;
import com.zimbra.soap.type.LoggingLevel;
import com.zimbra.soap.type.ShareInfo;
import com.zimbra.soap.type.TargetType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TrackCommandRequestHandler extends DocumentHandler {
  private static final String ACCOUNT_UUID = "186c1c23-d2ad-46b4-9efd-ddd890b1a4a2";
  private static final String ACCOUNT_NAME = "test@test.com";
  private static List<String> requests = new ArrayList<>();
  private static Map<String, Supplier<Element>> customResponseMapping;

  public static void reset() {
    customResponseMapping = null;
    requests = new ArrayList<>();
  }

  public static void setCustomResponseMapping(Map<String, Supplier<Element>> customResponseMapping) {
    TrackCommandRequestHandler.customResponseMapping = customResponseMapping;
  }

  public static List<String> getRequestString() {
    return requests;
  }

  final Map<String, Supplier<Element>> responseMapping = new HashMap<>();
  {
    responseMapping.put("AuthRequest", () -> {
      var resp = new AuthResponse();
      resp.setLifetime(803400943);
      resp.setAuthToken("0_2b6c930a7ca1a02daad5f27528d6c9986317204e_69643d33363a62333134613231652d666137392d346533352d613765352d6437666637303834333866363b6578703d31333a313733323535383437303239303b76763d323a31363b747970653d363a7a696d6272613b753d313a613b7469643d31303a313131353331313832383b");
      return jaxbToElement(resp);
    });
    responseMapping.put("AddAccountLoggerRequest", () -> {
      AddAccountLoggerResponse resp = AddAccountLoggerResponse.create(List.of(
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
      resp.setCos(createCosInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("CountAccountRequest", () -> {
      var resp = new CountAccountResponse();
      resp.setCos(List.of(new CosCountInfo("cos-id", "cos-name", 42)));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateAccountRequest", () -> {
      CreateAccountResponse resp = new CreateAccountResponse();
      resp.setAccount(createAccountInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateCalendarResourceRequest", () -> {
      var resp = new CreateCalendarResourceResponse(new CalendarResourceInfo("calendarResourceId", "calendarResourceName"));
      return jaxbToElement(resp);
    });
    responseMapping.put("CreateCosRequest", () -> {
      var resp = new CreateCosResponse();
      resp.setCos(createCosInfo());
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
      resp.setDomain(createDomainInfo());
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
    responseMapping.put("GetAccountRequest", () -> {
      GetAccountResponse resp = new GetAccountResponse();
      resp.setAccount(createAccountInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAccountLoggersRequest", () -> {
      GetAccountLoggersResponse resp = new GetAccountLoggersResponse();
      resp.setLoggers(List.of(LoggerInfo.createForCategoryAndLevel("category", LoggingLevel.info)));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAccountMembershipRequest", () -> {
      GetAccountMembershipResponse resp = new GetAccountMembershipResponse();
      resp.setDlList(List.of(new DLInfo("DlId", "DlName")));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllAccountLoggersRequest", () -> {
      GetAllAccountLoggersResponse resp = new GetAllAccountLoggersResponse();
      resp.setLoggers(List.of(new AccountLoggerInfo("accountName", "accountId")));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllConfigRequest", () -> {
      GetAllConfigResponse resp = new GetAllConfigResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllCosRequest", () -> {
      GetAllCosResponse resp = new GetAllCosResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllDomainsRequest", () -> {
      GetAllDomainsResponse resp = new GetAllDomainsResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllDistributionListsRequest", () -> {
      GetAllDistributionListsResponse resp = new GetAllDistributionListsResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("GetCalendarResourceRequest", () -> {
      GetCalendarResourceResponse resp = new GetCalendarResourceResponse(
              new CalendarResourceInfo("calendarResourceId", "calendarResourceName")
      );
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDomainInfoRequest", () -> {
      var resp = new GetDomainInfoResponse();
      resp.setDomain(createDomainInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDomainRequest", () -> {
      GetDomainResponse resp = new GetDomainResponse(createDomainInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("GetServerRequest", () -> {
      GetServerResponse resp = new GetServerResponse(createServerInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDistributionListRequest", () -> {
      var resp = new GetDistributionListResponse();
      resp.setDl(new DistributionListInfo("dlId", "dlName"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetCosRequest", () -> {
      var resp = new GetCosResponse();
      resp.setCos(createCosInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllEffectiveRightsRequest", () -> {
      var resp = new GetAllEffectiveRightsResponse(new GranteeInfo(GranteeType.usr, "granteeId", "granteeName"));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllServersRequest", () -> {
      var resp = new GetAllServersResponse();
      resp.addServer(createServerInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("GetAllRightsRequest", () -> {
      var resp = new GetAllRightsResponse();
      RightInfo rightInfo = new RightInfo();
      rightInfo.setName("getAccount");
      resp.setRights(List.of(rightInfo));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetConfigRequest", () -> {
      var resp = new GetConfigResponse();
      resp.setAttrs(List.of(new Attr(ZAttrProvisioning.A_zimbraId, "attrId")));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetDistributionListMembersRequest", () -> {
      var resp = new GetDistributionListMembersResponse();
      resp.addDlMember("test@test.com");
      return jaxbToElement(resp);
    });
    responseMapping.put("GetEffectiveRightsRequest", () -> {
      var resp = new GetEffectiveRightsResponse(
              new GranteeInfo(GranteeType.usr, "granteeId", "granteeName"),
              new EffectiveRightsTargetInfo(
                      TargetType.account,
                      "targetTypeId", "targetTypeName",
                      new EffectiveAttrsInfo(true),
                      new EffectiveAttrsInfo(true)
              )
      );
      return jaxbToElement(resp);
    });
    responseMapping.put("GetQuotaUsageRequest", () -> {
      var resp = new GetQuotaUsageResponse(false, 1,
              List.of(new AccountQuotaInfo(
                      ACCOUNT_UUID, ACCOUNT_NAME, 500, 1000
              ))
      );
      return jaxbToElement(resp);
    });
    responseMapping.put("GetRightRequest", () -> {
      RightInfo rightInfo = new RightInfo();
      rightInfo.setName("getAccount");
      var resp = new GetRightResponse(rightInfo);
      return jaxbToElement(resp);
    });
    responseMapping.put("GetRightsDocRequest", () -> {
      var resp = new GetRightsDocResponse();
      resp.setPackages(List.of(new PackageRightsInfo("packageRightsInfoName")));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetShareInfoRequest", () -> {
      var resp = new GetShareInfoResponse();
      ShareInfo shareInfo = new ShareInfo();
      shareInfo.setOwnerDisplayName("ownerDisplayName");
      shareInfo.setOwnerEmail(ACCOUNT_NAME);
      shareInfo.setOwnerId(ACCOUNT_UUID);
      shareInfo.setGranteeType("usr");
      resp.setShares(List.of(shareInfo));
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyAccountRequest", () -> {
      var resp = new ModifyAccountResponse();
      resp.setAccount(createAccountInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyCalendarResourceRequest", () -> {
      var resp = new ModifyCalendarResourceResponse(
              new CalendarResourceInfo("calendarResourceId", "calendarResourceName")
      );
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyCosRequest", () -> {
      var resp = new ModifyCosResponse();
      resp.setCos(createCosInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyDataSourceRequest", () -> {
      var resp = new ModifyDataSourceResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyDistributionListRequest", () -> {
      var resp = new ModifyDistributionListResponse();
      resp.setDl(new DistributionListInfo("dlId", "dlName"));
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyDomainRequest", () -> {
      var resp = new ModifyDomainResponse();
      resp.setDomain(createDomainInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("ModifyServerRequest", () -> {
      var resp = new ModifyServerResponse();
      resp.setServer(createServerInfo());
      return jaxbToElement(resp);
    });
    responseMapping.put("PushFreeBusyRequest", () -> {
      var resp = new PushFreeBusyResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("RecalculateMailboxCountsRequest", () -> {
      var resp = new RecalculateMailboxCountsResponse();
      resp.setMailbox(new MailboxQuotaInfo(ACCOUNT_UUID, 42));
      return jaxbToElement(resp);
    });
    responseMapping.put("RevokeRightsRequest", () -> {
      var resp = new RevokeRightsResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("SetPasswordRequest", () -> {
      var resp = new SetPasswordResponse("SetPasswordRequest message");
      return jaxbToElement(resp);
    });
    responseMapping.put("UnregisterMailboxMoveOutRequest", () -> {
      var resp = new UnregisterMailboxMoveOutResponse();
      return jaxbToElement(resp);
    });
    responseMapping.put("VerifyIndexRequest", () -> {
      var resp = new VerifyIndexResponse(true, "VerifyIndexResponse message");
      return jaxbToElement(resp);
    });
    responseMapping.put("CountObjectsRequest", () -> {
      var resp = new CountObjectsResponse(42, "account");
      return jaxbToElement(resp);
    });
    responseMapping.put("CompactIndexRequest", () -> {
      var resp = new CompactIndexResponse("OK");
      return jaxbToElement(resp);
    });
    responseMapping.put("GetIndexStatsRequest", () -> {
      var resp = new GetIndexStatsResponse();
      resp.setStats(new IndexStats(100,100));
      return jaxbToElement(resp);
    });
    responseMapping.put("GetMailboxRequest", () -> {
      var resp = new GetMailboxResponse(new MailboxWithMailboxId(0, "id", 0L));
      return jaxbToElement(resp);
    });
  }

  private static ServerInfo createServerInfo() {
    return new ServerInfo(ACCOUNT_UUID, "localhost",
            List.of(new Attr(ZAttrProvisioning.A_zimbraServiceHostname, "localhost")));
  }

  private static DomainInfo createDomainInfo() {
    return new DomainInfo(MailboxTestUtil.DEFAULT_DOMAIN_ID, MailboxTestUtil.DEFAULT_DOMAIN, List.of(
            new Attr(ZAttrProvisioning.A_zimbraDomainType, "local"),
            new Attr(Provisioning.A_zimbraPreAuthKey, "PreAuthkey")
    ));
  }

  private static AccountInfo createAccountInfo() {
    return new AccountInfo(ACCOUNT_UUID, ACCOUNT_NAME, false, List.of(
            new Attr(ZAttrProvisioning.A_zimbraId, ACCOUNT_UUID),
            new Attr(ZAttrProvisioning.A_zimbraMailHost, "localhost"),
            new Attr(ZAttrProvisioning.A_zimbraAccountStatus, "active")
    ));
  }

  private static CosInfo createCosInfo() {
    return CosInfo.createForIdAndName("cos-id", "cos-name");
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
    Supplier<Element> elementSupplier = null;
    if (customResponseMapping != null) {
      elementSupplier = customResponseMapping.get(request.getName());
    }
    if (elementSupplier == null)  {
      elementSupplier =
              responseMapping.get(request.getName());
    }
    if (elementSupplier != null) {
      return elementSupplier.get();
    } else {
      return Element.XMLElement.parseXML("<Response />");
    }
  }

}
