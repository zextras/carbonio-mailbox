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
 * A variant of {@link AutoComplete} that returns {@link AutoCompleteMatch}es from multiple sources ({@link Account}s)
 * based on the order provided by the {@link com.zimbra.common.soap.MailConstants#E_ORDERED_ACCOUNT_IDS}
 */
public class FullAutoComplete extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var fullAutocompleteRequest = getFullAutocompleteRequestFrom(request);
    final var fullAutoCompleteMatches = new ArrayList<AutoCompleteMatch>();
    final var otherAutoCompleteMatches = new ArrayList<AutoCompleteMatch>();

    try {
      final var autoCompleteRequest = fullAutocompleteRequest.getAutoCompleteRequest();
      final var authenticatedAccount = zsc.getAuthToken().getAccount();
      final var zAuthToken = zsc.getAuthToken().toZAuthToken();
      final int contactAutoCompleteMaxResultsLimit = authenticatedAccount.getContactAutoCompleteMaxResults();
      final var parsedAccountIds = parsePreferredAccountsFrom(
          fullAutocompleteRequest.getOrderedAccountIds());
      final var preferredAccountId = parsedAccountIds._1();
      final var otherPreferredAccountIds = parsedAccountIds._2();

      // process matches from "preferred account"
      final var preferredAccountAutoCompleteResponse = doAutoCompleteOnAccount(authenticatedAccount,
          zAuthToken, preferredAccountId, autoCompleteRequest);
      for (var autoCompleteMatch : preferredAccountAutoCompleteResponse.getMatches()) {
        if (contactAutoCompleteMaxResultsLimit > 0
            && fullAutoCompleteMatches.size() >= contactAutoCompleteMaxResultsLimit) {
          break;
        }
        fullAutoCompleteMatches.add(autoCompleteMatch);
      }

      // process matches from "other preferred accounts"
      for (var otherAccountId : otherPreferredAccountIds) {
        final var otherAccountAutoCompleteResponse = doAutoCompleteOnAccount(authenticatedAccount,
            zAuthToken, otherAccountId, autoCompleteRequest);
        for (var autoCompleteMatch : otherAccountAutoCompleteResponse.getMatches()) {
          if (contactAutoCompleteMaxResultsLimit > 0
              && fullAutoCompleteMatches.size() + otherAutoCompleteMatches.size()
              >= contactAutoCompleteMaxResultsLimit) {
            break;
          }
          if (!fullAutoCompleteMatches.contains(autoCompleteMatch)) {
            otherAutoCompleteMatches.add(autoCompleteMatch);
          }
        }
      }
      otherAutoCompleteMatches.sort(Comparator.comparing(AutoCompleteMatch::getRanking).reversed()
          .thenComparing(AutoCompleteMatch::getEmail));
    } catch (ServiceException | IOException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }
    fullAutoCompleteMatches.addAll(otherAutoCompleteMatches);

    return fullAutoCompleteResponseFor(fullAutoCompleteMatches, false);
  }

  /**
   * Retrieves a {@link FullAutocompleteRequest} object from the provided raw XML {@link Element}.
   *
   * @param request The raw XML {@link Element} containing the {@link FullAutocompleteRequest}.
   * @return The {@link FullAutocompleteRequest} object parsed from the raw XML {@link Element}.
   * @throws ServiceException If the raw XML {@link Element} does not contain a valid {@link FullAutocompleteRequest}
   *                          element.
   */
  private FullAutocompleteRequest getFullAutocompleteRequestFrom(Element request) throws ServiceException {
    final FullAutocompleteRequest fullAutocompleteRequest = JaxbUtil.elementToJaxb(request);

    if (fullAutocompleteRequest == null) {
      throw ServiceException.FAILURE("Invalid Request");
    }
    return fullAutocompleteRequest;
  }

  /**
   * Generates an XML {@link Element} representing a {@link FullAutocompleteResponse} based on the provided list of
   * autocomplete matches ({@link AutoCompleteMatch}) and cache flag.
   *
   * @param fullAutoCompleteMatches The list of autocomplete({@link AutoCompleteMatch}) matches.
   * @param canBeCached             A boolean flag indicating whether the response can be cached.
   * @return The XML {@link  Element} representing the {@link FullAutocompleteResponse}.
   * @throws ServiceException If an error occurs during the generation of the {@link FullAutocompleteResponse}.
   */
  @SuppressWarnings("SameParameterValue")
  private Element fullAutoCompleteResponseFor(List<AutoCompleteMatch> fullAutoCompleteMatches, boolean canBeCached)
      throws ServiceException {
    final var autoCompleteResponse = new FullAutocompleteResponse();
    autoCompleteResponse.setMatches(fullAutoCompleteMatches);
    autoCompleteResponse.setCanBeCached(canBeCached);
    return JaxbUtil.jaxbToElement(autoCompleteResponse);
  }

  /**
   * @param authenticatedAccount The Authenticated account
   * @param zAuthToken           The {@link ZAuthToken} that will be used to perform SOAP calls
   * @param requestedAccountId   The account ID for which the {@link AutoComplete} matches will be returned
   * @param autoCompleteRequest  The original {@link AutoCompleteRequest} element that will be used to perform {@link
   *                             AutoComplete} SOAP call
   * @return {@link AutoCompleteResponse}
   * @throws ServiceException If something goes wrong.
   * @throws IOException      If an I/O error occurs during the request transport.
   */
  private AutoCompleteResponse doAutoCompleteOnAccount(Account authenticatedAccount, ZAuthToken zAuthToken,
      String requestedAccountId, AutoCompleteRequest autoCompleteRequest)
      throws ServiceException, IOException {
    final var soapUrl = URLUtil.getSoapURL(authenticatedAccount.getServer(), true);
    final var autocompleteRequestElement = JaxbUtil.jaxbToElement(autoCompleteRequest);

    final AutoCompleteResponse autoCompleteResponse;
    if (authenticatedAccount.getId().equalsIgnoreCase(requestedAccountId)) {
      autoCompleteResponse = JaxbUtil.elementToJaxb(new SoapHttpTransport(zAuthToken, soapUrl).invoke(
          autocompleteRequestElement), AutoCompleteResponse.class);
    } else {
      autoCompleteResponse = JaxbUtil.elementToJaxb(new SoapHttpTransport(zAuthToken, soapUrl).invoke(
          autocompleteRequestElement, requestedAccountId));
    }
    return autoCompleteResponse;
  }

  /**
   * Parses {@link com.zimbra.common.soap.MailConstants#E_ORDERED_ACCOUNT_IDS} into a {@link Tuple2} object containing
   * first object as "Preferred Account" and second object as "Other Preferred Account"
   *
   * @param preferredAccountsStr {@link String} containing comma seperated ordered list of accounts IDs
   * @return a tuple containing first object as "Preferred Account" and second object as "Other Preferred Account".
   */
  Tuple2<String, List<String>> parsePreferredAccountsFrom(String preferredAccountsStr) {
    String preferredAccount = null;
    List<String> otherAccounts = new ArrayList<>();

    if (preferredAccountsStr != null && !preferredAccountsStr.isEmpty()) {
      String[] tokens = preferredAccountsStr.split(",");
      preferredAccount = tokens[0].trim();
      for (int i = 1; i < tokens.length; i++) {
        String trimmedToken = tokens[i].trim();
        if (!trimmedToken.isEmpty()) {
          otherAccounts.add(trimmedToken);
        }
      }
    }
    return new Tuple2<>(preferredAccount, otherAccounts);
  }
}
