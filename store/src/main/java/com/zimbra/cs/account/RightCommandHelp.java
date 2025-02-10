package com.zimbra.cs.account;

class RightCommandHelp implements CommandHelp {
  boolean printRights;
  boolean secretPossible;
  boolean modifierPossible;

  RightCommandHelp(boolean printRights, boolean secretPossible, boolean modifierPossible) {
    this.printRights = printRights;
    this.secretPossible = secretPossible;
    this.modifierPossible = modifierPossible;
  }

  @Override
  public String getExtraHelp() {
    return ProvUtil.helpRIGHTCommand(secretPossible, modifierPossible);
  }
}
