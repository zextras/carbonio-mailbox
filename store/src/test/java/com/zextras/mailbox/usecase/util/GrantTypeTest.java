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

class GrantTypeTest {

  @ParameterizedTest
  @MethodSource("getGranteeTypesWithLDAPTypes")
  void shouldReturnProperLdapObjectTypes(
      GrantType type, List<SearchDirectoryOptions.ObjectType> expectedTypes) {
    assertEquals(type.getObjectTypes(), expectedTypes);
  }

  @ParameterizedTest
  @MethodSource("getGranteeTypesWithNumbers")
  void shouldReturnProperGrantTypeFromByte(byte granteeNumber, GrantType expectedType) {
    assertEquals(expectedType, GrantType.fromGranteeTypeNumber(granteeNumber).get());
  }

  @ParameterizedTest
  @MethodSource("getGranteeTypesWithNames")
  void shouldReturnProperGrantTypeFromByte(String granteeName, GrantType expectedType) {
    assertEquals(expectedType, GrantType.fromGranteeTypeName(granteeName).get());
  }

  private static Stream<Arguments> getGranteeTypesWithLDAPTypes() {
    return Stream.of(
        Arguments.of(
            GrantType.GRANTEE_USER,
            List.of(
                SearchDirectoryOptions.ObjectType.accounts,
                SearchDirectoryOptions.ObjectType.resources)),
        Arguments.of(
            GrantType.GRANTEE_GROUP, List.of(SearchDirectoryOptions.ObjectType.distributionlists)),
        Arguments.of(GrantType.GRANTEE_COS, List.of(SearchDirectoryOptions.ObjectType.coses)),
        Arguments.of(GrantType.GRANTEE_DOMAIN, List.of(SearchDirectoryOptions.ObjectType.domains)));
  }

  @Test
  void shouldFailOnWrongGrantTypeNumber() {
    Try<GrantType> result = GrantType.fromGranteeTypeNumber((byte) 0xa);
    assertTrue(result.isFailure());
  }

  @Test
  void shouldFailOnWrongGrantTypeName() {
    Try<GrantType> result = GrantType.fromGranteeTypeName("wrongType");
    assertTrue(result.isFailure());
  }

  private static Stream<Arguments> getGranteeTypesWithNumbers() {
    return Stream.of(
        Arguments.of((byte) 1, GrantType.GRANTEE_USER),
        Arguments.of((byte) 2, GrantType.GRANTEE_GROUP),
        Arguments.of((byte) 5, GrantType.GRANTEE_COS),
        Arguments.of((byte) 4, GrantType.GRANTEE_DOMAIN));
  }

  private static Stream<Arguments> getGranteeTypesWithNames() {
    return Stream.of(
        Arguments.of("usr", GrantType.GRANTEE_USER),
        Arguments.of("grp", GrantType.GRANTEE_GROUP),
        Arguments.of("cos", GrantType.GRANTEE_COS),
        Arguments.of("dom", GrantType.GRANTEE_DOMAIN));
  }
}
