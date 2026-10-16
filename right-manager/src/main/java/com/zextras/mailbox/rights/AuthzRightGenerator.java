/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.rights;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AttributeManagerException;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.TargetType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Generates the carbonio-authz enums from the rights catalogue: {@code <output dir> <package>}. */
public class AuthzRightGenerator {

  public static void main(String[] args)
      throws ServiceException, AttributeManagerException, IOException {
    final Path packageDir = Path.of(args[0], args[1].split("\\."));
    final RightManager rightManager = RightManager.fromResources(AttributeManager.getInstance());
    Files.createDirectories(packageDir);
    Files.writeString(packageDir.resolve("AuthzRight.java"), authzRight(args[1], checkableRights(rightManager)));
    Files.writeString(packageDir.resolve("AuthzTargetType.java"), authzTargetType(args[1]));
  }

  private static List<Right> checkableRights(RightManager rightManager) {
    return Stream.concat(
            rightManager.getAllUserRights().values().stream(),
            rightManager.getAllAdminRights().values().stream())
        .filter(right -> !right.isComboRight())
        .sorted(Comparator.comparing(Right::getName))
        .toList();
  }

  private static String authzRight(String packageName, List<Right> rights) {
    final String constants = rights.stream()
        .map(right -> "  %s(\"%s\", %s)".formatted(
            constantName(right.getName()), right.getName(), right.isUserRight()))
        .collect(Collectors.joining(",\n", "", ";"));
    return """
        package %s;

        public enum AuthzRight {
        %s

          private final String rightName;
          private final boolean userRight;

          AuthzRight(String rightName, boolean userRight) {
            this.rightName = rightName;
            this.userRight = userRight;
          }

          public String rightName() {
            return rightName;
          }

          public boolean isUserRight() {
            return userRight;
          }
        }
        """.formatted(packageName, constants);
  }

  private static String authzTargetType(String packageName) {
    final String constants = Arrays.stream(TargetType.values())
        .map(type -> "  %s(\"%s\")".formatted(type.getCode().toUpperCase(), type.getCode()))
        .collect(Collectors.joining(",\n", "", ";"));
    return """
        package %s;

        public enum AuthzTargetType {
        %s

          private final String code;

          AuthzTargetType(String code) {
            this.code = code;
          }

          public String code() {
            return code;
          }
        }
        """.formatted(packageName, constants);
  }

  static String constantName(String rightName) {
    return rightName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
  }
}
