package com.zimbra.cs.account;

import java.io.InputStream;

public class ResourceAttributeStream implements AttributeStream {

  private final String rightResourcePath;

  public ResourceAttributeStream(String rightResourcePath) {
    this.rightResourcePath = rightResourcePath;
  }

  @Override
  public InputStream open(String attributesFileName) {
    return AttributeManager.class.getResourceAsStream(rightResourcePath + attributesFileName);

  }
}
