// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("hello")
public class RestServlet {

  @GET
  @Path("name")
  @Produces("application/json")
  public HelloResponse getSmartLinks() {
    final HelloResponse helloResponse = new HelloResponse();
    helloResponse.setValue("hello!");
    return helloResponse;
  }

}
