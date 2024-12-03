package com.zimbra.cs.account;

class ReindexCommandHelp implements CommandHelp {

  @Override
  public String getExtraHelp() {
    /*
     * copied from soap-admin.txt Not exactly match all types in MailboxIndex TODO: cleanup
     */
    return System.lineSeparator()
        + "Valid types:"
        + System.lineSeparator()
        + "    appointment"
        + System.lineSeparator()
        + "    contact"
        + System.lineSeparator()
        + "    conversation"
        + System.lineSeparator()
        + "    document"
        + System.lineSeparator()
        + "    message"
        + System.lineSeparator()
        + "    note"
        + System.lineSeparator()
        + "    task"
        + System.lineSeparator();
  }
}
