// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

public class ZmApiTags {

  /**
   * Use for a general description of the SOAP Command.  Use in JAXB class for a Request.
   * <br />
   * Note that the first part will be used for a summary of the command, where the first part is considered to
   * be everything upto an html break of some sort - Current pattern (See Command) used for the split is:
   *      "<br|<BR|<p>|<P>|<table|<TABLE|<ul|<UL|<ol|<OL|<pre>|<PRE>";
   */
  public static final String TAG_COMMAND_DESCRIPTION = "@zm-api-command-description";
  /**
   * Use to flag that a command is part of Network Edition. Value doesn't matter, suggest "true" or
   * empty
   */
  public static final String TAG_COMMAND_NETWORK_ONLY = "@zm-api-command-network-edition";
  /**
   * Information on whether standard Zimbra Authorization is required - true|false|maybe If the
   * first token is "maybe", the rest explains why
   */
  public static final String TAG_COMMAND_AUTH_REQUIRED = "@zm-api-command-auth-required";
  /**
   * Information on whether Zimbra Administration Authorization is required - true|false|maybe If
   * the first token is "maybe", the rest explains why
   */
  public static final String TAG_COMMAND_ADMIN_AUTH_REQUIRED =
      "@zm-api-command-admin-auth-required";
  /**
   * Use to flag a command that is or will be deprecated. Set to text explaining the nature of the
   * deprecation
   */
  public static final String TAG_COMMAND_DEPRECATION_INFO = "@zm-api-command-deprecation-info";
  /** Use for description of the SOAP Request. Use in JAXB class for a Request. */
  public static final String TAG_COMMAND_REQUEST_DESCRIPTION = "@zm-api-request-description";
  /** Use for description of the SOAP Response. Use in JAXB class for a Response. */
  public static final String TAG_COMMAND_RESPONSE_DESCRIPTION = "@zm-api-response-description";
  /**
   * Use for description of a field related to an element, attribute or element value. Place near
   * the field (or method where appropriate) containing the JAXB annotation.
   */
  public static final String TAG_FIELD_DESCRIPTION = "@zm-api-field-description";
  /**
   * Use for references in documentation to the value of a field. For instance "@zm-api-field-tag
   * thing-name" would result in : <SetThingNameRequest name="{thing-name}" /> instead of, say
   * <SetThingNameRequest name="String" />
   */
  public static final String TAG_FIELD_TAG = "@zm-api-field-tag";

  private ZmApiTags() {}
}
