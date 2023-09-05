package com.zextras.mailbox.usecase.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.SearchDirectoryOptions;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.List;

public enum GrantType {
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

  GrantType(
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

  public static Try<GrantType> fromGranteeTypeNumber(byte granteeNumber) {
    return Try.of(
        () ->
            Arrays.stream(GrantType.values())
                .filter(type -> type.granteeNumber == granteeNumber)
                .findFirst()
                .orElseThrow(
                    () ->
                        ServiceException.INVALID_REQUEST(
                            "invalid grantee type for revokeOrphanGrants", null)));
  }

  public static Try<GrantType> fromGranteeTypeName(String granteeName) {
    return Try.of(
        () ->
            Arrays.stream(GrantType.values())
                .filter(type -> type.granteeTypeName.equalsIgnoreCase(granteeName))
                .findFirst()
                .orElseThrow(
                    () ->
                        ServiceException.INVALID_REQUEST(
                            "invalid grantee type for revokeOrphanGrants", null)));
  }
}
