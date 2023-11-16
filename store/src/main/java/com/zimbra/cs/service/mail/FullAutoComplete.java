// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.AutoCompleteResponse;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.AutoCompleteMatch;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.ArrayList;
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
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final List<AutoCompleteResponse> autoCompleteResponses = new ArrayList<>();
    final FullAutocompleteRequest fullAutocompleteRequest = JaxbUtil.elementToJaxb(request);

    final ZAuthToken zAuthToken = zsc.getAuthToken().toZAuthToken();
    final AutoCompleteRequest autoCompleteRequest = fullAutocompleteRequest.getAutoCompleteRequest();

    final AutoCompleteResponse selfAutoComplete;
    try {
      selfAutoComplete = doSelfAutoComplete(zsc.getAuthToken().getAccount(), zAuthToken,
          autoCompleteRequest);
      autoCompleteResponses.add(selfAutoComplete);
    } catch (IOException e) {
      ServiceException.FAILURE(e.getMessage());
    }

    for (String requestedAccountId: fullAutocompleteRequest.getExtraAccountIds()) {
      final AutoCompleteResponse accountAutoComplete = doAutoCompleteOnAccount(zAuthToken,
          requestedAccountId, autoCompleteRequest).getOrElseThrow(
          e -> ServiceException.FAILURE(e.getMessage()));
      autoCompleteResponses.add(accountAutoComplete);
    }



    List<AutoCompleteMatch> autoCompleteMatches = new ArrayList<>();
    autoCompleteResponses.forEach(
        autoCompleteResponse -> autoCompleteMatches.addAll(autoCompleteResponse.getMatches())
    );

    AutoCompleteResponse autoCompleteResponse = new FullAutocompleteResponse();
    autoCompleteResponse.setMatches(autoCompleteMatches);
    autoCompleteResponse.setCanBeCached(false);
    return JaxbUtil.jaxbToElement(autoCompleteResponse);

  }

  /**
   * Executes and {@link AutoCompleteRequest} from the requested Account.
   *
   * @param zAuthToken a {@link ZAuthToken}
   * @param requestedAccountId requested account id
   * @param autoCompleteRequest the request to execute against requested target
   * @return {@link AutoCompleteResponse}
   */
  private Try<AutoCompleteResponse> doAutoCompleteOnAccount(ZAuthToken zAuthToken, String requestedAccountId, AutoCompleteRequest autoCompleteRequest) {

    //TODO: use current server -> maybe LC.zimbra_server_hostname.value() ?
    return Try.of(() ->
        JaxbUtil.jaxbToElement(autoCompleteRequest)
    ).mapTry(
        autoCompleteElementRequest -> new SoapHttpTransport(zAuthToken, "").invoke(
            autoCompleteElementRequest, requestedAccountId)
    ).mapTry(
        elementResponse -> JaxbUtil.elementToJaxb(elementResponse, AutoCompleteResponse.class));
  }

  private AutoCompleteResponse doSelfAutoComplete(Account authenticatedAccount, ZAuthToken zAuthToken, AutoCompleteRequest autoCompleteRequest) throws ServiceException, IOException {
    String soapUrl = URLUtil.getSoapURL(authenticatedAccount.getServer(), true);

    final Element autocompleteRequestElement = JaxbUtil.jaxbToElement(autoCompleteRequest);
    return JaxbUtil.elementToJaxb(new SoapHttpTransport(zAuthToken, soapUrl).invoke(
        autocompleteRequestElement),  AutoCompleteResponse.class);
  }
}
