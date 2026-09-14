package com.zextras.mailbox.quota;

public final class QuotaUsageMessages {

  /**
   * Qualifies every reported mailbox usage figure: the value covers mails, calendars and contacts
   * only, and excludes storage accounted for by other services. It is printed on its own line,
   * above single line outputs and both above and below multi line ones.
   */
  public static final String USAGE_SCOPE_NOTE =
      "WARNING: This size refeers to Mails/Calendars/Contacts only";

  private QuotaUsageMessages() {
    throw new UnsupportedOperationException("Utility class and cannot be instantiated");
  }
}
