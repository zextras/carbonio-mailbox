package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import java.io.InputStream;

class ResourceRightStream implements RightStream {

  private final String rightResourcePath;

  public ResourceRightStream(String rightResourcePath) {
    this.rightResourcePath = rightResourcePath;
  }

  @Override
  public InputStream open(final String rightResourceName) throws ServiceException {
    return RightManager.class.getResourceAsStream(rightResourcePath + rightResourceName);
  }
}
