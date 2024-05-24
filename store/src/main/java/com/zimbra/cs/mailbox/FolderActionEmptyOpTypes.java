package com.zimbra.cs.mailbox;

public enum FolderActionEmptyOpTypes {
  EMAILS,
  CONTACTS,
  APPOINTMENTS;

  public static boolean contains(String name) {
    for (FolderActionEmptyOpTypes type : FolderActionEmptyOpTypes.values()) {
      if (type.name().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }
}
