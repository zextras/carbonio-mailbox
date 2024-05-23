package com.zimbra.cs.mailbox;

public enum FolderActionEmptyOpTypes {
  EMAILS("emails"),
  CONTACTS("contacts"),
  APPOINTMENTS("appointments");

  private final String name;

  FolderActionEmptyOpTypes(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
