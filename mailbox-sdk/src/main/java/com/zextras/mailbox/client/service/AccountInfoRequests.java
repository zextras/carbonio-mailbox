package com.zextras.mailbox.client.service;

import com.zextras.mailbox.client.requests.AuthRequest;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;
import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbraaccount.GetAccountInfoRequest;
import zimbraaccount.GetAccountInfoResponse;

public class AccountInfoRequests {
  public AuthRequest<ZcsPortType, GetAccountInfoResponse> byId(String id) {
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(selector(AccountBy.ID, id));
    return AuthRequest.requireAuth((service, soapHeaderContext) -> service.getAccountInfoRequest(request, soapHeaderContext));
  }

  public AuthRequest<ZcsPortType, GetAccountInfoResponse> byEmail(String email) {
    GetAccountInfoRequest request = new GetAccountInfoRequest();
    request.setAccount(selector(AccountBy.NAME, email));
    return AuthRequest.requireAuth((service, soapHeaderContext) -> service.getAccountInfoRequest(request, soapHeaderContext));
  }

  private static AccountSelector selector(AccountBy by, String value) {
    AccountSelector selector = new AccountSelector();
    selector.setBy(by);
    selector.setValue(value);
    return selector;
  }
}
