package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import java.io.InputStream;

interface RightStream {

  InputStream open(String rightFileName) throws ServiceException;
}
