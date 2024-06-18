// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ContactAutoComplete.ContactEntryType;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.AutoCompleteRequest;
import com.zimbra.soap.mail.message.AutoCompleteResponse;
import com.zimbra.soap.mail.message.FullAutocompleteRequest;
import com.zimbra.soap.mail.message.FullAutocompleteResponse;
import com.zimbra.soap.mail.type.AutoCompleteMatch;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    final var contactAutoCompleteMaxResultsLimit = authenticatedAccount.getContactAutoCompleteMaxResults();

    final var autoCompleteRequest = fullAutocompleteRequest.getAutoCompleteRequest();
    final var parsedAccountIds = parsePreferredAccountsFrom(
        fullAutocompleteRequest.getOrderedAccountIds());
    final var preferredAccountId = parsedAccountIds._1();
    final var otherPreferredAccountIds = parsedAccountIds._2();

    doAutoCompleteOnAccount(authenticatedAccount, preferredAccountId, autoCompleteRequest, context)
        .getMatches()
        .stream()
        .limit(Math.max(contactAutoCompleteMaxResultsLimit, 0))
        .forEachOrdered(fullAutoCompleteMatches::add);

    otherPreferredAccountIds.stream()
        .map(otherAccountId -> doAutoCompleteOnAccount(authenticatedAccount, otherAccountId,
            autoCompleteRequest, context))
        .forEachOrdered(otherAccountAutoCompleteResponse -> otherAccountAutoCompleteResponse.getMatches().stream()
            .filter(autoCompleteMatch -> autoCompleteMatch.getEmail() == null || fullAutoCompleteMatches.stream()
                .noneMatch(match -> autoCompleteMatch.getEmail().equalsIgnoreCase(match.getEmail())))
            .forEachOrdered(otherAutoCompleteMatches::add));

    otherAutoCompleteMatches.stream()
        .sorted(Comparator.comparing(AutoCompleteMatch::getRanking).reversed()
            .thenComparing(match -> match.getEmail() == null ? "<" + match.getDisplayName() + ">" : match.getEmail(),
                Comparator.nullsLast(String::compareToIgnoreCase)))
        .filter(autoCompleteMatch -> fullAutoCompleteMatches.stream()
            .noneMatch(match ->
                match.getEmail() != null && match.getEmail().equalsIgnoreCase(autoCompleteMatch.getEmail())))
        .limit(Math.max(contactAutoCompleteMaxResultsLimit - fullAutoCompleteMatches.size(), 0))
        .forEachOrdered(fullAutoCompleteMatches::add);

    return fullAutoCompleteResponseFor(fullAutoCompleteMatches, false, zsc);
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
   */
  @SuppressWarnings("SameParameterValue")
  private Element fullAutoCompleteResponseFor(List<AutoCompleteMatch> fullAutoCompleteMatches, boolean canBeCached,
      ZimbraSoapContext zsc) {
    final var response = zsc.createElement(MailConstants.FULL_AUTO_COMPLETE_RESPONSE);
    response.addAttribute(MailConstants.A_CANBECACHED, canBeCached);
    getElementsForMatches(fullAutoCompleteMatches, zsc).forEach(response::addNonUniqueElement);
    return response;
  }

  /**
   * @param fullAutoCompleteMatches List of full {@link AutoCompleteMatch}es
   * @return List of match {@link Element}s
   */
  private ArrayList<Element> getElementsForMatches(List<AutoCompleteMatch> fullAutoCompleteMatches,
      ZimbraSoapContext zsc) {

    return fullAutoCompleteMatches.stream().map(match -> {
      var matchElement = zsc.createElement(MailConstants.E_MATCH);
      matchElement.addAttribute(MailConstants.A_RANKING, Integer.toString(match.getRanking()));
      matchElement.addAttribute(MailConstants.A_MATCH_TYPE, match.getMatchType());
      matchElement.addAttribute(MailConstants.A_IS_GROUP, match.getGroup());

      // for contact group, emails of members will be expanded separately on user request
      if (Boolean.FALSE.equals(match.getGroup())) {
        matchElement.addAttribute(MailConstants.A_EMAIL, match.getEmail());
      }

      if (match.getGroup() && match.getCanExpandGroupMembers()) {
        matchElement.addAttribute(MailConstants.A_EXP, true);
      }

      final String id = match.getId();
      if (id != null) {
        matchElement.addAttribute(MailConstants.A_ID, id);
      }

      final String folder = match.getFolder();
      if (folder != null) {
        matchElement.addAttribute(MailConstants.A_FOLDER, folder);
      }

      if (Boolean.TRUE.equals(match.getGroup()) || !Objects.equals(match.getMatchType(),
          ContactEntryType.GAL.getName())) {
        matchElement.addAttribute(MailConstants.A_DISPLAYNAME, match.getDisplayName());
      }

      final String firstName = match.getFirstName();
      if (firstName != null) {
        matchElement.addAttribute(MailConstants.A_FIRSTNAME, firstName);
      }

      final String middleName = match.getMiddleName();
      if (middleName != null) {
        matchElement.addAttribute(MailConstants.A_MIDDLENAME, middleName);
      }

      final String lastName = match.getLastName();
      if (lastName != null) {
        matchElement.addAttribute(MailConstants.A_LASTNAME, lastName);
      }

      final String fullName = match.getFullName();
      if (fullName != null) {
        matchElement.addAttribute(MailConstants.A_FULLNAME, fullName);
      }

      final String nickname = match.getNickname();
      if (nickname != null) {
        matchElement.addAttribute(MailConstants.A_NICKNAME, nickname);
      }

      final String company = match.getCompany();
      if (company != null) {
        matchElement.addAttribute(MailConstants.A_COMPANY, company);
      }

      final String fileAs = match.getFileAs();
      if (fileAs != null) {
        matchElement.addAttribute(MailConstants.A_FILEAS, fileAs);
      }
      return matchElement;
    }).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Gets AutoComplete matches for passed account.
   *  <p>If the account is local execute the handler otherwise makes http call to the remote server to retrieve the response
   *  for SOAP AutoComplete.</p>
   *
   * @param authenticatedAccount The Authenticated account
   * @param requestedAccountId   The account ID for which the {@link AutoComplete} matches will be returned
   * @param autoCompleteRequest  The original {@link AutoCompleteRequest} element that will be used to perform {@link
   *                             AutoComplete}
   * @return {@link AutoCompleteResponse}
   */
  private AutoCompleteResponse doAutoCompleteOnAccount(Account authenticatedAccount,
      String requestedAccountId, AutoCompleteRequest autoCompleteRequest, Map<String, Object> context) {
    try {
      if (requestedAccountId == null || authenticatedAccount.getId().equalsIgnoreCase(requestedAccountId)) {
        return JaxbUtil.elementToJaxb(
            new AutoComplete().handle(JaxbUtil.jaxbToElement(autoCompleteRequest), context));
      } else {
        var requestedAccount = Provisioning.getInstance().getAccountById(requestedAccountId);
        if (requestedAccount.getServer().isLocalServer()) {
          var zimbraSoapContext = getZimbraSoapContext(context);
          var operationContext = getOperationContext(zimbraSoapContext, context);
          operationContext.setmRequestedAccountId(requestedAccountId);
          return JaxbUtil.elementToJaxb(
              new AutoComplete().handle(JaxbUtil.jaxbToElement(autoCompleteRequest), requestedAccount,
                  operationContext, zimbraSoapContext));
        }
      }
      return proxyRequestInternal(requestedAccountId, autoCompleteRequest, context);
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
