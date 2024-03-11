package com.zextras.mailbox.smartlinks;

import com.zextras.mailbox.AuthenticationInfo;
import com.zimbra.common.service.ServiceException;

import java.util.List;

public interface SmartLinksGenerator {
  List<SmartLink> smartLinksFrom(List<Attachment> smartLinks, AuthenticationInfo authenticationInfo) throws ServiceException;
}
