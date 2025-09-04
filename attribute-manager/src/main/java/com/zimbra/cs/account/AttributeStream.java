package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import java.io.InputStream;

public interface AttributeStream {

  InputStream open(String attributesFileName) throws ServiceException;
}
