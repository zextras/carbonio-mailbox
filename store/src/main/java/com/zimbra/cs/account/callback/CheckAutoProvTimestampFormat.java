package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import java.util.Map;

public class CheckAutoProvTimestampFormat extends AttributeCallback {

  @Override
  public void preModify(CallbackContext context, String attrName, Object attrValue,
      Map attrsToModify, Entry entry) throws ServiceException {

  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {

  }
}
