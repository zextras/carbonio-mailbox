package com.zimbra.cs.account.callback;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.ldap.AutoProvision;
import com.zimbra.cs.ldap.LdapDateUtil;
import java.util.Date;
import java.util.Map;

/**
 * This {@link AttributeCallback} implementation provides way to validate multiple timestamp formats
 * supported by the {@link AutoProvision} service.
 *
 * @author Keshav Bhatt
 * @author Soner Sivri
 * @since 23.12.0
 */
public class CheckAutoProvTimestampFormat extends AttributeCallback {

  @Override
  public void preModify(CallbackContext context, String attrName, Object attrValue,
      Map attrsToModify, Entry entry) throws ServiceException {
    if (!isAutoProvTimestampValid(attrValue.toString())) {
      throw ServiceException.FAILURE(
          "Supplied timestamp format value is not valid, and cannot be set!");
    }
  }

  @Override
  public void postModify(CallbackContext context, String attrName, Entry entry) {
    // not required
  }


  /**
   * Checks whether the given timestamp format value is valid and supported by the {@link
   * AutoProvision} service.
   *
   * @param formatString the timestamp format string
   * @return true if the format string is valid and supported by the {@link AutoProvision} service,
   * false otherwise.
   */
  boolean isAutoProvTimestampValid(String formatString) {
    try {
      LdapDateUtil.toGeneralizedTime(new Date(), formatString);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}
