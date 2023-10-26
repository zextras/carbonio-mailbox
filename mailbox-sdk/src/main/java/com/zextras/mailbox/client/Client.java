package com.zextras.mailbox.client;

import com.zextras.mailbox.client.requests.Request;

public interface Client<Service> {
  <Res> Res send(Request<Service, Res> request);
}
