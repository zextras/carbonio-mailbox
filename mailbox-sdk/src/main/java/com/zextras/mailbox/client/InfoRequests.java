package com.zextras.mailbox.client;

import com.zextras.mailbox.client.requests.AuthRequest;
import https.www_zextras_com.wsdl.zimbraservice.ZcsPortType;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import zimbraaccount.GetInfoRequest;
import zimbraaccount.GetInfoResponse;

public class InfoRequests {

  public AuthRequest<ZcsPortType, GetInfoResponse> allSections() {
    GetInfoRequest request = new GetInfoRequest();
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getInfoRequest(request, soapHeaderContext));
  }

  public AuthRequest<ZcsPortType, GetInfoResponse> sections(String first, String... rest) {
    final var sections =
        Stream.concat(Stream.of(first), Arrays.stream(rest)).collect(Collectors.toList());
    final var joined = String.join(",", sections);

    GetInfoRequest request = new GetInfoRequest();
    request.setSections(joined);
    return AuthRequest.requireAuth(
        (service, soapHeaderContext) -> service.getInfoRequest(request, soapHeaderContext));
  }
}
