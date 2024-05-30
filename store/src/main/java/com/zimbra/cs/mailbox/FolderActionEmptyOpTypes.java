package com.zimbra.cs.mailbox;

import com.zimbra.cs.mailbox.MailItem.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum representing different types supported by the empty operation in
 * {@link com.zimbra.soap.mail.type.FolderActionSelector}.
 */
public enum FolderActionEmptyOpTypes {
  EMAILS,
  CONTACTS,
  APPOINTMENTS;

  /**
   * Checks if the enum contains a type with the specified name.
   *
   * @param name the name of the type to check
   * @return true if the enum contains the specified type, false otherwise
   */
  public static boolean contains(String name) {
    for (FolderActionEmptyOpTypes type : values()) {
      if (type.name().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the list of included types for a specified folder action type.
   *
   * @param type the folder action type
   * @return a list of included types for the specified folder action type
   */
  public static List<Type> getIncludedTypesFor(FolderActionEmptyOpTypes type) {
    switch (type) {
      case EMAILS:
        return List.of(Type.MESSAGE, Type.CONVERSATION);
      case CONTACTS:
        return List.of(Type.CONTACT);
      case APPOINTMENTS:
        return List.of(Type.APPOINTMENT);
      default:
        return Collections.emptyList();
    }
  }

  /**
   * Converts the enum values to a comma-separated string.
   *
   * @return a string representation of the enum values, separated by commas
   */
  public static String valueToString() {
    return Arrays.stream(
        values()).map(type -> type.name().toLowerCase()).collect(Collectors.joining(", "));
  }
}
