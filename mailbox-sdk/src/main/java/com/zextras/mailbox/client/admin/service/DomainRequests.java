// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.client.admin.service;

import com.zextras.mailbox.client.requests.AuthRequest;
import https.www_zextras_com.wsdl.zimbraservice.ZcsAdminPortType;
import zimbraadmin.DomainBy;
import zimbraadmin.DomainSelector;
import zimbraadmin.GetDomainRequest;
import zimbraadmin.GetDomainResponse;

public class DomainRequests {
  public AuthRequest<ZcsAdminPortType, GetDomainResponse> byId(String id) {
    DomainSelector selector = new DomainSelector();
    selector.setBy(DomainBy.ID);
    selector.setValue(id);

    GetDomainRequest request = new GetDomainRequest();
    request.setDomain(selector);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getDomainRequest(request, soapHeaderContext));
  }

  public AuthRequest<ZcsAdminPortType, GetDomainResponse> byName(String name) {
    DomainSelector selector = new DomainSelector();
    selector.setBy(DomainBy.NAME);
    selector.setValue(name);

    GetDomainRequest request = new GetDomainRequest();
    request.setDomain(selector);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getDomainRequest(request, soapHeaderContext));
  }
}
