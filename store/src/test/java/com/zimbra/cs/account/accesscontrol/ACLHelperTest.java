// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.account.accesscontrol;

import static org.mockito.Mockito.mock;

import com.google.common.base.Supplier;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.util.AccountUtil;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ACLHelperTest {

  @ParameterizedTest
  @ValueSource(longs = {-100L, 420L, 300L, 200L})
  @DisplayName("Policy exception when grantee time ZERO and max lifetime != 0.")
  void shouldThrowNumberFormatExceptionWhenEmptyString(long maxLifeTime) {
    Assertions.assertThrows(
        NumberFormatException.class, () -> new ACLHelper().validateGrantExpiry("", maxLifeTime));
  }

  @Test
  void shouldThrowPolicyConflictWhenExpiryZeroAndMaxLifeTimeNotZero() {
    final String message =
        Assertions.assertThrows(
                ServiceException.class, () -> new ACLHelper().validateGrantExpiry("0", 420L))
            .getMessage();
    Assertions.assertEquals(message, "permission denied: share expiration policy conflict");
  }

  @Test
  @DisplayName("Return ZERO if grant expiry null and maxLifeTime zero")
  void shouldReturnZeroLifetimeIWhenExpiryNullAndMaxLifeTimeZero() throws Exception {
    Assertions.assertEquals(0L, new ACLHelper().validateGrantExpiry(null, 0L));
  }

  @ParameterizedTest
  @ValueSource(longs = {420L, 1L, 3L, 27000L})
  @DisplayName("Return new expiry time when expiry is null and max life time > zero.")
  void shouldReturnCurrentTimeMillisPlusExpiryTimeWhenExpiryNull(long maxLifeTime)
      throws Exception {
    long now = System.currentTimeMillis();
    Supplier<Long> timeNowSupplier = () -> now;
    Assertions.assertEquals(
        now + maxLifeTime,
        new ACLHelper(mock(AccountUtil.class), timeNowSupplier)
            .validateGrantExpiry(null, maxLifeTime));
  }

  private static Stream<Arguments> expiryNotNull() {
    return Stream.of(
        Arguments.of("1", 420L),
        Arguments.of("2", 30L),
        Arguments.of("199", 200L),
        Arguments.of("200", 200L),
        Arguments.of("250", 200L),
        Arguments.of("250", 0L),
        Arguments.of("0", 0L),
        Arguments.of("10", -100L),
        Arguments.of("-100", 100L),
        Arguments.of("-100", -100L));
  }

  @ParameterizedTest
  @MethodSource("expiryNotNull")
  @DisplayName("Return expiry time when not null.")
  void shouldReturnRemainingExpiryIfNotNull(String expiryTime, long maxLifeTime) throws Exception {
    Assertions.assertEquals(
        Long.parseLong(expiryTime), new ACLHelper().validateGrantExpiry(expiryTime, maxLifeTime));
  }

  @Test
  @DisplayName("Throws if granted expiry time is in the future.")
  void shouldThrowWhenGranteeTimeGreaterThanComputedTime() throws Exception {
    final long now = System.currentTimeMillis();
    Supplier<Long> timeNowSupplier = () -> now;
    final long maxLifeTime = 10L;
    final long granteeTime = now + 100L;
    final String message =
        Assertions.assertThrows(
                ServiceException.class,
                () ->
                    new ACLHelper(mock(AccountUtil.class), timeNowSupplier)
                        .validateGrantExpiry(String.valueOf(granteeTime), maxLifeTime))
            .getMessage();
    Assertions.assertEquals(message, "permission denied: share expiration policy conflict");
  }

  @Test
  @DisplayName("if grantee time == now + maxLifetime, returns same number.")
  void shouldReturnValueIfGranteeTimeEqualToComputedTime() throws Exception {
    final long now = System.currentTimeMillis();
    Supplier<Long> timeNowSupplier = () -> now;
    final long maxLifeTime = 10L;
    final long granteeTime = now + maxLifeTime;
    Assertions.assertEquals(
        granteeTime,
        new ACLHelper(mock(AccountUtil.class), timeNowSupplier)
            .validateGrantExpiry(String.valueOf(granteeTime), maxLifeTime));
  }
}
