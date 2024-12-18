package com.zimbra.cs.account;

public enum Category {
  ACCOUNT("help on account-related commands"),
  CALENDAR("help on calendar resource-related commands"),
  COMMANDS("help on all commands"),
  CONFIG("help on config-related commands"),
  COS("help on COS-related commands"),
  DOMAIN("help on domain-related commands"),
  FREEBUSY("help on free/busy-related commands"),
  LIST("help on distribution list-related commands"),
  LOG("help on logging commands"),
  MISC("help on misc commands"),
  MAILBOX("help on mailbox-related commands"),
  REVERSEPROXY("help on reverse proxy related commands"),
  RIGHT("help on right-related commands"),
  SEARCH("help on search-related commands"),
  SERVER("help on server-related commands"),
  SHARE("help on share related commands"),
  HAB("help on HAB commands");

  private final String description;

  public String getDescription() {
    return description;
  }

  Category(String desc) {
    description = desc;
  }
}
