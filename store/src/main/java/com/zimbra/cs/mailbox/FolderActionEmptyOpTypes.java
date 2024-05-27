package com.zimbra.cs.mailbox;

import com.zimbra.cs.mailbox.MailItem.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//Todo add java docs
public enum FolderActionEmptyOpTypes {
  EMAILS,
  CONTACTS,
  APPOINTMENTS;

  //Todo add java docs
  public static boolean contains(String name) {
    for (FolderActionEmptyOpTypes type : values()) {
      if (type.name().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  //Todo add java docs
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
   * @return String of comma separated list of types
   */
  public static String valueToString() {
    return Arrays.stream(
        values()).map(type -> type.name().toLowerCase()).collect(Collectors.joining(", "));
  }
}
