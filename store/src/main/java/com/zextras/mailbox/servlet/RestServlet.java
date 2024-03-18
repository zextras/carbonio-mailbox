// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;


import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("hello")
public class RestServlet {

  @GET
  @Path("name")
  public String getSmartLinks() {
    return "hello!";
  }

}
