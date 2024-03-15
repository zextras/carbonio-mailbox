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
  private final Provisioning provisioning;

  protected static String CATEGORY_ALL = "all";

  public AddAccountLogger(Provisioning provisioning) {
    this.provisioning = provisioning;
  }

  /**
   * Handles the request, checks if log category exists and adds a logger for a given account.
   *
   * @param request {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.AddAccountLoggerRequest}
   * @param context request context
   * @return {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.AddAccountLoggerResponse}
   * @throws ServiceException in case of invalid request.
   */
  @Override
  public Element handle(final Element request, final Map<String, Object> context)
      throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);

    final Server localServer = provisioning.getLocalServer();
    checkRight(zsc, context, localServer, Admin.R_manageAccountLogger);

    /* would be nice to use JAXB to process the request but probably need to accept different
     * cases for the levels ("TRACE" as well as "trace") and would need to update the JAXB class with
     * an adapter to sort that out.
     */
    final Account account = getAccountFromLoggerRequest(request);

    final Element eLogger = request.getElement(AdminConstants.E_LOGGER);
    final String category = eLogger.getAttribute(AdminConstants.A_CATEGORY);
    final String sLevel = eLogger.getAttribute(AdminConstants.A_LEVEL);

    // Handle level.
    final Level level;
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

    final Collection<Log> loggers = addAccountLogger(account, category, level);

    // Build response.
    final List<LoggerInfo> loggerInfos = new ArrayList<>(loggers.size());
    for (Log log : loggers) {
      loggerInfos.add(
          LoggerInfo.createForCategoryAndLevel(log.getCategory(), LoggingLevel.toJaxb(level)));
    }
    return zsc.jaxbToElement(AddAccountLoggerResponse.create(loggerInfos));
  }

  /**
   * Adds a logger for a requested account.
   *
   * @param account {@link Account} to add a logger to
   * @param category {@link ZimbraLog} category
   * @param level {@link Level}
   * @return {@link Log} collection added to the account
   */
  public static Collection<Log> addAccountLogger(
      final Account account, final String category, final Level level) {
    // Handle category.
    final Collection<Log> loggers;
    if (category.equalsIgnoreCase(CATEGORY_ALL)) {
      loggers = LogManager.getAllLoggers();
    } else {
      loggers = List.of(LogFactory.getLog(category));
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
  protected static Account getAccountFromLoggerRequest(final Element request)
      throws ServiceException {
    final Account account;
    final Provisioning prov = Provisioning.getInstance();
    final Element idElement = request.getOptionalElement(AdminConstants.E_ID);

    if (idElement != null) {
      // Handle deprecated <id> element.
      ZimbraLog.soap.info(
          "The <%s> element is deprecated for <%s>.  Use <%s> instead.",
          AdminConstants.E_ID, request.getName(), AdminConstants.E_ACCOUNT);
      final String id = idElement.getText();
      account = prov.get(AccountBy.id, id);
      if (account == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(idElement.getText());
      }
    } else {
      // Handle <account> element.
      final Element accountElement = request.getElement(AdminConstants.E_ACCOUNT);
      final AccountBy by = AccountBy.fromString(accountElement.getAttribute(AdminConstants.A_BY));
      account = prov.get(by, accountElement.getText());
      if (account == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(accountElement.getText());
      }
    }
    return account;
  }

  @Override
  public void docRights(final List<AdminRight> relatedRights, final List<String> notes) {
    relatedRights.add(Admin.R_manageAccountLogger);
  }
}
