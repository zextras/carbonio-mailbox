package com.zimbra.cs.account;

import java.io.InputStream;

public interface AttributeStream {

  InputStream open(String attributesFileName);
}
