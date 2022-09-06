package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import java.util.ArrayList;

class ZMLookupAvailableVar extends ProxyConfVar {

  public ZMLookupAvailableVar() {
    super(
        "lookup.available",
        null,
        false,
        ProxyConfValueType.ENABLER,
        ProxyConfOverride.CUSTOM,
        "Indicates whether there are available lookup handlers or not");
  }

  @Override
  public void update() throws ServiceException, ProxyConfException {
    ZMLookupHandlerVar lhVar = new ZMLookupHandlerVar();
    lhVar.update();
    @SuppressWarnings("unchecked")
    ArrayList<String> servers = (ArrayList<String>) lhVar.mValue;
    if (servers.isEmpty()) {
      mValue = false;
    } else {
      mValue = true;
    }
  }
}
