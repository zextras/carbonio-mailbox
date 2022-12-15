// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.session.Session;
import com.zimbra.cs.session.SessionCache;
import com.zimbra.cs.session.SoapSession;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.EndSessionRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * End the current session immediately cleaning up all resources used by the session including the
 * notification buffer and logging the session out from IM if applicable
 */
public class EndSession extends AccountDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    EndSessionRequest req = JaxbUtil.elementToJaxb(request);
    String sessionId = req.getSessionId();
    final boolean clearCookies = req.isLogOff();
    final boolean clearAllSessions = req.isClearAllSoapSessions();
    final boolean excludeCurrentSession = req.isExcludeCurrentSession();
    Account account = getAuthenticatedAccount(zsc);

    if (clearAllSessions) {
      clearAllSessions(zsc, excludeCurrentSession, account);
    } else if (!StringUtil.isNullOrEmpty(sessionId)) {
      Session s = SessionCache.lookup(sessionId, account.getId());
      if (s == null) {
        throw ServiceException.FAILURE("Failed to find session with given sessionId", null);
      } else {
        clearSession(s, null);
      }
    } else {
      if (zsc.hasSession()) {
        Session s = getSession(zsc);
        endSession(s);
      }
      if (clearCookies || account.isForceClearCookies()) {
        context.put(SoapServlet.INVALIDATE_COOKIES, true);
        try {
          AuthToken at = zsc.getAuthToken();
          HttpServletRequest httpReq =
              (HttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
          HttpServletResponse httpResp =
              (HttpServletResponse) context.get(SoapServlet.SERVLET_RESPONSE);
          at.encode(httpReq, httpResp, true);
          at.deRegister();
        } catch (AuthTokenException e) {
          throw ServiceException.FAILURE("Failed to de-register an auth token", e);
        }
      }
    }

    return zsc.createElement(AccountConstants.END_SESSION_RESPONSE);
  }

  /**
   * @param zsc SoapContext of the request
   * @param excludeCurrentSession exclude current session
   * @param account Account whose session will be cleared
   * @throws ServiceException
   */
  private void clearAllSessions(
      final ZimbraSoapContext zsc, boolean excludeCurrentSession, Account account)
      throws ServiceException {
    String currentSessionId = null;
    if (excludeCurrentSession && zsc.hasSession()) {
      Session currentSession = getSession(zsc);
      currentSessionId = currentSession.getSessionId();
    }
    Collection<Session> sessionCollection = SessionCache.getSoapSessions(account.getId());
    if (sessionCollection != null) {
      List<Session> sessions = new ArrayList<>(sessionCollection);
      Iterator<Session> itr = sessions.iterator();
      while (itr.hasNext()) {
        Session session = itr.next();
        itr.remove();
        clearSession(session, currentSessionId);
      }
    }
  }

  /**
   * @param session Session to clear
   * @param currentSessionId current session ID
   * @throws ServiceException exception if an error occurs during session cleanup
   */
  private void clearSession(Session session, String currentSessionId) throws ServiceException {
    if (session instanceof SoapSession
        && !session.getSessionId().equalsIgnoreCase(currentSessionId)) {
      AuthToken at = ((SoapSession) session).getAuthToken();
      if (at != null) {
        try {
          at.deRegister();
        } catch (AuthTokenException e) {
          throw ServiceException.FAILURE("Failed to de-register an auth token", e);
        }
      }
      endSession(session);
    }
  }
}
