/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.authz.service;

import com.zextras.mailbox.api.rest.service.AccountService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.accesscontrol.TargetTypeLookup;
import com.zimbra.soap.type.TargetBy;
import io.vavr.control.Try;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RightsService {

  private final Supplier<Provisioning> provisioningSupplier;
  private final Supplier<AccessManager> accessManagerSupplier;
  private final Supplier<RightManager> rightManagerSupplier;
  private final AccountService accountService;

  public RightsService(
      Supplier<Provisioning> provisioningSupplier,
      Supplier<AccessManager> accessManagerSupplier,
      Supplier<RightManager> rightManagerSupplier,
      AccountService accountService) {
    this.provisioningSupplier = provisioningSupplier;
    this.accessManagerSupplier = accessManagerSupplier;
    this.rightManagerSupplier = rightManagerSupplier;
    this.accountService = accountService;
  }

  public Try<Boolean> tokenHasRight(
      String encodedToken, String right, String targetType, String targetId) {
    return rightCheck(right, targetType, targetId)
        .flatMap(check -> accountService.getAuthToken(encodedToken)
            .mapTry(authToken -> hasRight(
                authToken.getAccountId(), grantee -> AuthToken.isAnyAdmin(authToken), check)));
  }

  public Try<Boolean> accountHasRight(
      String accountId, String right, String targetType, String targetId) {
    return rightCheck(right, targetType, targetId)
        .mapTry(check -> hasRight(
            accountId, grantee -> accessManagerSupplier.get().isAdequateAdminAccount(grantee), check));
  }

  public Try<List<Right>> rightsOn(String targetTypeName) {
    return Try.of(() -> {
      final TargetType targetType = TargetType.fromCode(targetTypeName);
      final RightManager rightManager = rightManagerSupplier.get();
      return Stream.<Right>concat(
              rightManager.getAllUserRights().values().stream(),
              rightManager.getAllAdminRights().values().stream())
          .filter(right -> !right.isComboRight() && right.executableOnTargetType(targetType))
          .sorted(Comparator.comparing(Right::getName))
          .toList();
    });
  }

  private Try<RightCheck> rightCheck(String rightName, String targetTypeName, String targetId) {
    return Try.of(() -> {
      final TargetType targetType = TargetType.fromCode(targetTypeName);
      if (targetType.needsTargetIdentity() && (targetId == null || targetId.isEmpty())) {
        throw ServiceException.INVALID_REQUEST("Missing target id for " + targetType, null);
      }
      final Right right = right(rightName);
      if (!right.executableOnTargetType(targetType)) {
        throw ServiceException.INVALID_REQUEST(
            "Right " + rightName + " cannot be checked on " + targetType, null);
      }
      return new RightCheck(right, targetType, targetId);
    });
  }

  private Right right(String rightName) throws ServiceException {
    try {
      return rightManagerSupplier.get().getRight(rightName);
    } catch (ServiceException e) {
      throw ServiceException.INVALID_REQUEST("Unknown right: " + rightName, e);
    }
  }

  private boolean hasRight(String granteeId, Predicate<Account> asAdminFor, RightCheck check)
      throws ServiceException {
    final Provisioning provisioning = provisioningSupplier.get();
    final Account grantee = provisioning.getAccountById(granteeId);
    final Entry target = TargetTypeLookup.lookupTarget(
        provisioning, check.targetType(), TargetBy.id, check.targetId(), false);
    if (grantee == null || target == null) {
      return false;
    }
    final boolean asAdmin = asAdminFor.test(grantee);
    if (asAdmin && isDomainInaccessible(provisioning, target)) {
      return false;
    }
    try {
      return accessManagerSupplier.get()
          .canPerform(grantee, target, check.right(), false, null, asAdmin, null);
    } catch (ServiceException e) {
      if (ServiceException.PERM_DENIED.equals(e.getCode())) {
        return false;
      }
      throw e;
    }
  }

  private static boolean isDomainInaccessible(Provisioning provisioning, Entry target)
      throws ServiceException {
    final Domain domain = target instanceof Domain targetDomain
        ? targetDomain
        : TargetTypeLookup.getTargetDomain(provisioning, target);
    return domain != null && (domain.isSuspended() || domain.isShutdown());
  }

  private record RightCheck(Right right, TargetType targetType, String targetId) {

  }
}
