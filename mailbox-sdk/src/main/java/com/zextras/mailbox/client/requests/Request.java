package com.zextras.mailbox.client.requests;

import zimbra.HeaderContext;

public interface Request<Service, Res> {
  Res call(Service service, HeaderContext soapHeaderContext);
}
