package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import java.io.InputStream;

public class ResourceAttributeStream implements AttributeStream {

  private final String rightResourcePath;

  public ResourceAttributeStream(String rightResourcePath) {
    this.rightResourcePath = rightResourcePath;
  }

  @Override
  public InputStream open(String attributeFileName) throws ServiceException {
    return AttributeManager.class.getResourceAsStream(rightResourcePath + attributeFileName);

  }
}
