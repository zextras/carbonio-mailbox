package com.zimbra.cs.mailbox;

public enum FolderActionEmptyOpTypes {
  EMAILS("emails"),
  CONTACTS("contacts"),
  APPOINTMENTS("appointments");

  private final String name;

  FolderActionEmptyOpTypes(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static boolean contains(String name) {
    for (FolderActionEmptyOpTypes type : FolderActionEmptyOpTypes.values()) {
      if (type.getName().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }
}
