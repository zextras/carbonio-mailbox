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
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    final var authenticatedAccount = zsc.getAuthToken().getAccount();
    final int contactAutoCompleteMaxResultsLimit = authenticatedAccount.getContactAutoCompleteMaxResults();

    try {
      final var autoCompleteRequest = fullAutocompleteRequest.getAutoCompleteRequest();
      final var zAuthToken = zsc.getAuthToken().toZAuthToken();
      final var parsedAccountIds = parsePreferredAccountsFrom(
          fullAutocompleteRequest.getOrderedAccountIds());
      final var preferredAccountId = parsedAccountIds._1();
      final var otherPreferredAccountIds = parsedAccountIds._2();

      // process matches from "preferred account"
      doAutoCompleteOnAccount(authenticatedAccount, zAuthToken, preferredAccountId, autoCompleteRequest).getMatches()
          .stream()
          .takeWhile(autoCompleteMatch -> contactAutoCompleteMaxResultsLimit <= 0
              || fullAutoCompleteMatches.size() < contactAutoCompleteMaxResultsLimit)
          .forEachOrdered(fullAutoCompleteMatches::add);

      // process matches from "other preferred accounts"
      otherPreferredAccountIds.stream()
          .map(otherAccountId -> doAutoCompleteOnAccount(authenticatedAccount, zAuthToken, otherAccountId,
              autoCompleteRequest))
          .forEachOrdered(otherAccountAutoCompleteResponse -> otherAccountAutoCompleteResponse.getMatches().stream()
              .filter(autoCompleteMatch -> fullAutoCompleteMatches.stream()
                  .noneMatch(m -> m.getEmail().equalsIgnoreCase(autoCompleteMatch.getEmail())))
              .forEachOrdered(otherAutoCompleteMatches::add));

      otherAutoCompleteMatches.sort(Comparator.comparing(AutoCompleteMatch::getRanking).reversed()
          .thenComparing(AutoCompleteMatch::getEmail));
    } catch (ServiceException e) {
      throw ServiceException.FAILURE(e.getMessage());
    }

    otherAutoCompleteMatches.stream()
        .filter(autoCompleteMatch -> fullAutoCompleteMatches.stream()
            .noneMatch(m -> m.getEmail().equalsIgnoreCase(autoCompleteMatch.getEmail())))
        .limit(Math.max(contactAutoCompleteMaxResultsLimit - fullAutoCompleteMatches.size(), 0))
        .forEach(fullAutoCompleteMatches::add);

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
   */
  private AutoCompleteResponse doAutoCompleteOnAccount(Account authenticatedAccount, ZAuthToken zAuthToken,
      String requestedAccountId, AutoCompleteRequest autoCompleteRequest) {
    try {
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
    } catch (ServiceException | IOException ignored) {
      // TODO add logging
      return new AutoCompleteResponse();
    }
  }

  /**
   * Parses {@link com.zimbra.common.soap.MailConstants#E_ORDERED_ACCOUNT_IDS} into a {@link Tuple2} object containing
   * the "preferred account" and "other preferred accounts". Duplicates account IDs are omitted.
   *
   * @param preferredAccountsStr A {@link String} containing a comma-separated ordered list of account IDs.
   * @return A tuple containing the preferred account and other preferred accounts.
   */
  Tuple2<String, LinkedHashSet<String>> parsePreferredAccountsFrom(String preferredAccountsStr) {
    if (preferredAccountsStr == null || preferredAccountsStr.isEmpty()) {
      return new Tuple2<>(null, new LinkedHashSet<>());
    }

    final var seenTokens = new HashSet<String>();
    final var otherAccounts = new LinkedHashSet<String>();
    String preferredAccount = null;

    for (var token : preferredAccountsStr.split(",")) {
      var trimmedToken = token.trim();
      if (!trimmedToken.isEmpty() && !seenTokens.contains(trimmedToken)) {
        if (preferredAccount == null) {
          preferredAccount = trimmedToken;
        } else {
          otherAccounts.add(trimmedToken);
        }
        seenTokens.add(trimmedToken);
      }
    }

    return new Tuple2<>(preferredAccount, otherAccounts);
  }
}
