// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.AutoCompleteResponse;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.AutoCompleteMatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A variant of {@link AutoComplete} that returns all contacts of authenticated account
 * + contacts of all accounts shared with it.
 * It doesn't support delegated requests.
 *
 */
public class FullAutoComplete extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final List<AutoCompleteResponse> autoCompleteResponses = new ArrayList<>();
    final FullAutocompleteRequest fullAutocompleteRequest = JaxbUtil.elementToJaxb(request);

    final AutoCompleteRequest autoCompleteRequest = fullAutocompleteRequest.getAutoCompleteRequest();
    Map<String, AutoCompleteMatch> matchesComparator = new HashMap<>();
    final AutoCompleteResponse selfAutoComplete;
    selfAutoComplete = doSelfAutoComplete(autoCompleteRequest, context);
    autoCompleteResponses.add(selfAutoComplete);

    for (String requestedAccountId: fullAutocompleteRequest.getExtraAccountIds()) {
      final AutoCompleteResponse accountAutoComplete;
      accountAutoComplete = doAutoCompleteOnAccount(requestedAccountId, autoCompleteRequest, context);
      autoCompleteResponses.add(accountAutoComplete);
    }

    autoCompleteResponses.forEach(
        autoCompleteResponse -> autoCompleteResponse.getMatches().forEach(
            match -> matchesComparator.putIfAbsent(match.getEmail(), match)
        )
    );

    AutoCompleteResponse autoCompleteResponse = new FullAutocompleteResponse();
    autoCompleteResponse.setMatches(matchesComparator.values());
    autoCompleteResponse.setCanBeCached(false);
    return JaxbUtil.jaxbToElement(autoCompleteResponse);

  }

  /**
   * Executes and {@link AutoCompleteRequest} from the requested Account.
   *
   * @param requestedAccountId requested account id
   * @param autoCompleteRequest the request to execute against requested target
   * @param context request context
   * @return {@link AutoCompleteResponse}
   */
  private AutoCompleteResponse doAutoCompleteOnAccount(String requestedAccountId,
      AutoCompleteRequest autoCompleteRequest, Map<String, Object> context) {
    try {
      var requestedAccount = Provisioning.getInstance().getAccountById(requestedAccountId);
      if(requestedAccount.getServer().isLocalServer()){
        var zimbraSoapContext = getZimbraSoapContext(context);
        var operationContext = getOperationContext(zimbraSoapContext, context);
        operationContext.setmRequestedAccountId(requestedAccountId);
        return JaxbUtil.elementToJaxb(
            new AutoComplete().handle(JaxbUtil.jaxbToElement(autoCompleteRequest), requestedAccount,
                operationContext, zimbraSoapContext));
      }else{
        return proxyRequestInternal(requestedAccountId, autoCompleteRequest, context);
      }
    } catch (ServiceException e) {
      ZimbraLog.misc.warn(e.getMessage());
      return new AutoCompleteResponse();
    }
  }

  AutoCompleteResponse proxyRequestInternal(String requestedAccountId, AutoCompleteRequest autoCompleteRequest,
      Map<String, Object> context) throws ServiceException {
    return JaxbUtil.elementToJaxb(proxyRequest(JaxbUtil.jaxbToElement(autoCompleteRequest), context,
        requestedAccountId));
  }

  private AutoCompleteResponse doSelfAutoComplete(AutoCompleteRequest autoCompleteRequest,
      Map<String, Object> context) throws ServiceException {
    return JaxbUtil.elementToJaxb(
        new AutoComplete().handle(JaxbUtil.jaxbToElement(autoCompleteRequest), context));
  }
}
