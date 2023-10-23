package com.zextras.mailbox.client.requests;

public interface AuthRequest<Service, Res> {
  Request<Service, Res> withAuthToken(String token);

  static <Service, Res> AuthRequest<Service, Res> requireAuth(Request<Service, Res> inner) {
    return (token) ->
        (service, soapHeaderContext) -> {
          try {
            soapHeaderContext.setAuthToken(token);
            return inner.call(service, soapHeaderContext);
          } finally {
            soapHeaderContext.setAuthToken(null);
          }
        };
  }
}
