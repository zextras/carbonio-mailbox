// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

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
import io.vavr.Tuple2;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A variant of {@link AutoComplete} that returns all contacts of authenticated account + contacts of all accounts
 * shared with it. It doesn't support delegated requests.
 */
public class FullAutoComplete extends MailDocumentHandler {


  private Tuple2<String, List<String>> parsePreferredAccounts(String input) {
    String preferredAccount = null;
    List<String> otherAccounts = new ArrayList<>();

    if (input != null && !input.isEmpty()) {
      String[] tokens = input.split(",");
      if (tokens.length > 0) {
        preferredAccount = tokens[0].trim();
        for (int i = 1; i < tokens.length; i++) {
          otherAccounts.add(tokens[i].trim());
        }
      }
    }
    return new Tuple2<>(preferredAccount, otherAccounts);
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final FullAutocompleteRequest fullAutocompleteRequest = JaxbUtil.elementToJaxb(request);
    if (fullAutocompleteRequest == null) {
      throw ServiceException.FAILURE("Invalid Request");
    }
    final ZAuthToken zAuthToken = zsc.getAuthToken().toZAuthToken();

    final List<AutoCompleteMatch> fullAutoCompleteMatches = new ArrayList<>();
    final List<AutoCompleteMatch> otherAutoCompleteMatches = new ArrayList<>();

    final Account authenticatedAccount = zsc.getAuthToken().getAccount();
    final int contactAutoCompleteMaxResultsLimit = authenticatedAccount.getContactAutoCompleteMaxResults();
    final Tuple2<String, List<String>> parsedAccountIds = parsePreferredAccounts(
        fullAutocompleteRequest.getOrderedAccountIds());
    final String preferredAccountId = parsedAccountIds._1();
    final List<String> otherPreferredAccountIds = parsedAccountIds._2();
    final AutoCompleteRequest autoCompleteRequest = fullAutocompleteRequest.getAutoCompleteRequest();
    try {
      // process matches from "preferred account"
      final AutoCompleteResponse preferredAccountAutoCompleteResponse = doAutoCompleteOnAccount(authenticatedAccount,
          zAuthToken, preferredAccountId, autoCompleteRequest);
      for (AutoCompleteMatch match : preferredAccountAutoCompleteResponse.getMatches()) {
        if (contactAutoCompleteMaxResultsLimit > 0
            && fullAutoCompleteMatches.size() >= contactAutoCompleteMaxResultsLimit) {
          break;
        }
        fullAutoCompleteMatches.add(match);
      }

      // process matches from "other preferred accounts"
      for (String otherAccountId : otherPreferredAccountIds) {
        final AutoCompleteResponse otherAccountAutoCompleteResponse = doAutoCompleteOnAccount(authenticatedAccount,
            zAuthToken, otherAccountId, autoCompleteRequest);
        for (AutoCompleteMatch match : otherAccountAutoCompleteResponse.getMatches()) {
          if (contactAutoCompleteMaxResultsLimit > 0
              && fullAutoCompleteMatches.size() + otherAutoCompleteMatches.size()
              >= contactAutoCompleteMaxResultsLimit) {
            break;
          }
          if (!fullAutoCompleteMatches.contains(match)) {
            otherAutoCompleteMatches.add(match);
          }
        }
      }
      otherAutoCompleteMatches.sort(Comparator.comparing(AutoCompleteMatch::getRanking).reversed());
    } catch (ServiceException | IOException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }

    fullAutoCompleteMatches.addAll(otherAutoCompleteMatches);

    final AutoCompleteResponse autoCompleteResponse = new FullAutocompleteResponse();
    autoCompleteResponse.setMatches(fullAutoCompleteMatches);
    autoCompleteResponse.setCanBeCached(false);
    return JaxbUtil.jaxbToElement(autoCompleteResponse);
  }

  private AutoCompleteResponse doAutoCompleteOnAccount(Account authenticatedAccount, ZAuthToken zAuthToken,
      String requestedAccountId, AutoCompleteRequest autoCompleteRequest)
      throws ServiceException, IOException {
    String soapUrl = URLUtil.getSoapURL(authenticatedAccount.getServer(), true);
    final Element autocompleteRequestElement = JaxbUtil.jaxbToElement(autoCompleteRequest);

    AutoCompleteResponse autoCompleteResponse;
    if (authenticatedAccount.getId().equalsIgnoreCase(requestedAccountId)) {
      autoCompleteResponse = JaxbUtil.elementToJaxb(new SoapHttpTransport(zAuthToken, soapUrl).invoke(
          autocompleteRequestElement), AutoCompleteResponse.class);
    } else {
      autoCompleteResponse = JaxbUtil.elementToJaxb(new SoapHttpTransport(zAuthToken, soapUrl).invoke(
          autocompleteRequestElement, requestedAccountId));
    }
    return autoCompleteResponse;
  }
}
