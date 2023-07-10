// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.*;
import com.zimbra.common.util.Log.Level;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.AddAccountLoggerResponse;
import com.zimbra.soap.admin.type.LoggerInfo;
import com.zimbra.soap.type.LoggingLevel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Adds a custom logger for the given account.
 *
 * @author bburtin
 */
public class AddAccountLogger extends AdminDocumentHandler {

  protected static String CATEGORY_ALL = "all";

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Server localServer = Provisioning.getInstance().getLocalServer();
    checkRight(zsc, context, localServer, Admin.R_manageAccountLogger);

    /* would be nice to use JAXB to process the request but probably need to accept different
     * cases for the levels ("TRACE" as well as "trace") and would need to update the JAXB class with
     * an adapter to sort that out.
     */
    Account account = getAccountFromLoggerRequest(request);

    Element eLogger = request.getElement(AdminConstants.E_LOGGER);
    String category = eLogger.getAttribute(AdminConstants.A_CATEGORY);
    String sLevel = eLogger.getAttribute(AdminConstants.A_LEVEL);

    // Handle level.
    Level level = null;
    try {
      level = Level.valueOf(sLevel.toLowerCase());
    } catch (IllegalArgumentException e) {
      String error =
          String.format(
              "Invalid level: %s.  Valid values are %s.",
              sLevel, StringUtil.join(",", Level.values()));
      throw ServiceException.INVALID_REQUEST(error, null);
    }

    if (!category.equalsIgnoreCase(CATEGORY_ALL) && !LogManager.logExists(category)) {
      throw ServiceException.INVALID_REQUEST("Log category " + category + " does not exist.", null);
    }

    Collection<Log> loggers = addAccountLogger(account, category, level);

    // Build response.
    List<LoggerInfo> loggerInfos = new ArrayList<>(loggers.size());
    for (Log log : loggers) {
      loggerInfos.add(
          LoggerInfo.createForCategoryAndLevel(log.getCategory(), LoggingLevel.toJaxb(level)));
    }
    return zsc.jaxbToElement(AddAccountLoggerResponse.create(loggerInfos));
  }

  public static Collection<Log> addAccountLogger(Account account, String category, Level level) {
    // Handle category.
    Collection<Log> loggers;
    if (category.equalsIgnoreCase(CATEGORY_ALL)) {
      loggers = LogManager.getAllLoggers();
    } else {
      loggers = Arrays.asList(LogFactory.getLog(category));
    }
    // Add custom loggers.
    for (Log log : loggers) {
      ZimbraLog.misc.info(
          "Adding custom logger: account=%s, category=%s, level=%s",
          account.getName(), category, level);
      log.addAccountLogger(account.getName(), level);
    }

    return loggers;
  }

  /**
   * Returns the <tt>Account</tt> object based on the &lt;id&gt; or &lt;account&gt; element owned by
   * the given request element.
   */
  protected static Account getAccountFromLoggerRequest(Element request) throws ServiceException {
    Account account = null;
    Provisioning prov = Provisioning.getInstance();
    Element idElement = request.getOptionalElement(AdminConstants.E_ID);

    if (idElement != null) {
      // Handle deprecated <id> element.
      ZimbraLog.soap.info(
          "The <%s> element is deprecated for <%s>.  Use <%s> instead.",
          AdminConstants.E_ID, request.getName(), AdminConstants.E_ACCOUNT);
      String id = idElement.getText();
      account = prov.get(AccountBy.id, id);
      if (account == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(idElement.getText());
      }
    } else {
      // Handle <account> element.
      Element accountElement = request.getElement(AdminConstants.E_ACCOUNT);
      AccountBy by = AccountBy.fromString(accountElement.getAttribute(AdminConstants.A_BY));
      account = prov.get(by, accountElement.getText());
      if (account == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(accountElement.getText());
      }
    }
    return account;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_manageAccountLogger);
  }
}
