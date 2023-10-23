package com.zextras.mailbox.client.admin.service;

import com.zextras.mailbox.client.requests.AuthRequest;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;
import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbraadmin.GetAccountInfoRequest;
import zimbraadmin.GetAccountInfoResponse;

public class AccountInfoRequests {
  public AuthRequest<ZcsAdminPortType, GetAccountInfoResponse> byId(String id) {
    AccountSelector selector = new AccountSelector();
    selector.setBy(AccountBy.ID);
    selector.setValue(id);

    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(selector);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getAccountInfoRequest(request, soapHeaderContext));
  }

  public AuthRequest<ZcsAdminPortType, GetAccountInfoResponse> byEmail(String email) {
    AccountSelector selector = new AccountSelector();
    selector.setBy(AccountBy.NAME);
    selector.setValue(email);

    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(selector);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getAccountInfoRequest(request, soapHeaderContext));
  }
}
