package com.zextras.mailbox.usecase.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.SearchDirectoryOptions;
import io.vavr.control.Try;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GranteeTypeTest {

  @ParameterizedTest
  @MethodSource("getGranteeTypesWithLDAPTypes")
  void shouldReturnProperLdapObjectTypes(
      GranteeType type, List<SearchDirectoryOptions.ObjectType> expectedTypes) {
    assertEquals(type.getObjectTypes(), expectedTypes);
  }

  @ParameterizedTest
  @MethodSource("getGranteeTypesWithNumbers")
  void shouldReturnProperGrantTypeFromByte(byte granteeNumber, GranteeType expectedType) {
    assertEquals(expectedType, GranteeType.fromGranteeTypeNumber(granteeNumber).get());
  }

  @ParameterizedTest
  @MethodSource("getGranteeTypesWithNames")
  void shouldReturnProperGrantTypeFromByte(String granteeName, GranteeType expectedType) {
    assertEquals(expectedType, GranteeType.fromGranteeTypeName(granteeName).get());
  }

  private static Stream<Arguments> getGranteeTypesWithLDAPTypes() {
    return Stream.of(
        Arguments.of(
            GranteeType.GRANTEE_USER,
            List.of(
                SearchDirectoryOptions.ObjectType.accounts,
                SearchDirectoryOptions.ObjectType.resources)),
        Arguments.of(
            GranteeType.GRANTEE_GROUP,
            List.of(SearchDirectoryOptions.ObjectType.distributionlists)),
        Arguments.of(GranteeType.GRANTEE_COS, List.of(SearchDirectoryOptions.ObjectType.coses)),
        Arguments.of(
            GranteeType.GRANTEE_DOMAIN, List.of(SearchDirectoryOptions.ObjectType.domains)));
  }

  @Test
  void shouldFailOnWrongGrantTypeNumber() {
    Try<GranteeType> result = GranteeType.fromGranteeTypeNumber((byte) 0xa);
    assertTrue(result.isFailure());
  }

  @Test
  void shouldFailOnWrongGrantTypeName() {
    Try<GranteeType> result = GranteeType.fromGranteeTypeName("wrongType");
    assertTrue(result.isFailure());
  }

  private static Stream<Arguments> getGranteeTypesWithNumbers() {
    return Stream.of(
        Arguments.of((byte) 1, GranteeType.GRANTEE_USER),
        Arguments.of((byte) 2, GranteeType.GRANTEE_GROUP),
        Arguments.of((byte) 5, GranteeType.GRANTEE_COS),
        Arguments.of((byte) 4, GranteeType.GRANTEE_DOMAIN));
  }

  private static Stream<Arguments> getGranteeTypesWithNames() {
    return Stream.of(
        Arguments.of("usr", GranteeType.GRANTEE_USER),
        Arguments.of("grp", GranteeType.GRANTEE_GROUP),
        Arguments.of("cos", GranteeType.GRANTEE_COS),
        Arguments.of("dom", GranteeType.GRANTEE_DOMAIN));
  }
}
