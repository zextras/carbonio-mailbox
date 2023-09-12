// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class AdminSoapServlet extends SoapServlet {

  private static final long serialVersionUID = 5242741895888594479L;

  @Inject
  public AdminSoapServlet(
      @Named("adminSOAPAPIs") List<DocumentService> documentServices,
      @Named("adminSOAPPorts") List<Integer> allowedPorts) {
    super(documentServices, allowedPorts);
    this.allowedPorts = allowedPorts;
  }

  private final List<Integer> allowedPorts;
}
