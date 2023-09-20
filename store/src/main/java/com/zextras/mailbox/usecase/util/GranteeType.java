package com.zextras.mailbox.usecase.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.SearchDirectoryOptions;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to get grantee type.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public enum GranteeType {
  GRANTEE_USER(
      (byte) 1,
      "usr",
      List.of(
          SearchDirectoryOptions.ObjectType.accounts, SearchDirectoryOptions.ObjectType.resources)),
  GRANTEE_GROUP((byte) 2, "grp", List.of(SearchDirectoryOptions.ObjectType.distributionlists)),
  GRANTEE_COS((byte) 5, "cos", List.of(SearchDirectoryOptions.ObjectType.coses)),
  GRANTEE_DOMAIN((byte) 4, "dom", List.of(SearchDirectoryOptions.ObjectType.domains));

  public byte getGranteeNumber() {
    return granteeNumber;
  }

  private final byte granteeNumber;
  private final String granteeTypeName;
  private final List<SearchDirectoryOptions.ObjectType> objectTypes;

  GranteeType(
      byte granteeNum,
      String granteeTypeName,
      List<SearchDirectoryOptions.ObjectType> objectTypes) {
    this.granteeNumber = granteeNum;
    this.granteeTypeName = granteeTypeName;
    this.objectTypes = objectTypes;
  }

  public List<SearchDirectoryOptions.ObjectType> getObjectTypes() {
    return objectTypes;
  }

  /**
   * Gets grantee type by number.
   *
   * @param granteeNumber grantee number
   * @return {@link GranteeType}
   */
  public static Try<GranteeType> fromGranteeTypeNumber(byte granteeNumber) {
    return Try.of(
        () ->
            Arrays.stream(GranteeType.values())
                .filter(type -> type.granteeNumber == granteeNumber)
                .findFirst()
                .orElseThrow(
                    () -> ServiceException.NOT_FOUND("unknown grantee type: " + granteeNumber)));
  }

  /**
   * Gets grantee type by name.
   *
   * @param granteeName grantee name
   * @return {@link GranteeType}
   */
  public static Try<GranteeType> fromGranteeTypeName(String granteeName) {
    return Try.of(
        () ->
            Arrays.stream(GranteeType.values())
                .filter(type -> type.granteeTypeName.equalsIgnoreCase(granteeName))
                .findFirst()
                .orElseThrow(
                    () -> ServiceException.NOT_FOUND("unknown grantee type: " + granteeName)));
  }
}
