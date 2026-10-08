/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.rights;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.TargetType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuthzRightGeneratorTest {

  @TempDir
  static Path outputDir;

  private static RightManager rightManager;
  private static String authzRight;
  private static String authzTargetType;

  @BeforeAll
  static void generate() throws Exception {
    AuthzRightGenerator.main(new String[] {outputDir.toString(), "com.example.authz"});
    final Path packageDir = outputDir.resolve("com/example/authz");
    authzRight = Files.readString(packageDir.resolve("AuthzRight.java"));
    authzTargetType = Files.readString(packageDir.resolve("AuthzTargetType.java"));
    rightManager = RightManager.fromResources(AttributeManager.getInstance());
  }

  @Test
  void generatesEveryCheckableRightOfTheCatalogue() {
    assertAll(allRights()
        .filter(right -> !right.isComboRight())
        .map(right -> () -> assertTrue(
            authzRight.contains("(\"%s\", %s)".formatted(right.getName(), right.isUserRight())),
            right.getName())));
  }

  @Test
  void skipsComboRights() {
    assertAll(allRights()
        .filter(Right::isComboRight)
        .map(right -> () -> assertFalse(
            authzRight.contains("(\"%s\",".formatted(right.getName())), right.getName())));
  }

  @Test
  void generatesEveryTargetType() {
    assertAll(Arrays.stream(TargetType.values())
        .map(type -> () -> assertTrue(
            authzTargetType.contains("(\"%s\")".formatted(type.getCode())), type.getCode())));
  }

  @Test
  void generatesIntoTheGivenPackage() {
    assertTrue(authzRight.startsWith("package com.example.authz;"));
    assertTrue(authzTargetType.startsWith("package com.example.authz;"));
  }

  @Test
  void turnsCamelCaseRightNamesIntoConstants() {
    assertEquals("CONFIGURE_QUOTA", AuthzRightGenerator.constantName("configureQuota"));
    assertEquals("SET_DL_ATTR2", AuthzRightGenerator.constantName("setDlAttr2"));
  }

  private static Stream<Right> allRights() {
    return Stream.of(rightManager.getAllUserRights().values(),
            rightManager.getAllAdminRights().values())
        .flatMap(Collection::stream);
  }
}
