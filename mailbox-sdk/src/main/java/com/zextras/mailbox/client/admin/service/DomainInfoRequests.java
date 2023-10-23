package com.zextras.mailbox.client.admin.service;

import com.zextras.mailbox.client.requests.AuthRequest;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;
import zimbraadmin.DomainBy;
import zimbraadmin.DomainSelector;
import zimbraadmin.GetDomainInfoRequest;
import zimbraadmin.GetDomainInfoResponse;

public class DomainInfoRequests {
  public AuthRequest<ZcsAdminPortType, GetDomainInfoResponse> byId(String id) {
    DomainSelector selector = new DomainSelector();
    selector.setBy(DomainBy.ID);
    selector.setValue(id);

    GetDomainInfoRequest request = new GetDomainInfoRequest();
    request.setDomain(selector);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getDomainInfoRequest(request, soapHeaderContext));
  }

  public AuthRequest<ZcsAdminPortType, GetDomainInfoResponse> byName(String name) {
    DomainSelector selector = new DomainSelector();
    selector.setBy(DomainBy.NAME);
    selector.setValue(name);

    GetDomainInfoRequest request = new GetDomainInfoRequest();
    request.setDomain(selector);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getDomainInfoRequest(request, soapHeaderContext));
  }
}
