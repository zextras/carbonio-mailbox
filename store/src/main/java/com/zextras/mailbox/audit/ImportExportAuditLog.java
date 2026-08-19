/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.audit;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Audit trail for folder import/export operations performed through the UserServlet, so
 * administrators can monitor which users imported or exported which folders.
 */
public class ImportExportAuditLog {

  public enum Outcome {
    SUCCESS,
    ERROR,
    PARTIAL
  }

  private static final Set<FormatType> AUDITED_FORMATS =
      EnumSet.of(
          FormatType.TGZ,
          FormatType.TAR,
          FormatType.ZIP,
          FormatType.CSV,
          FormatType.ICS,
          FormatType.VCF,
          FormatType.LDIF,
          FormatType.NETSCAPELDIF);

  private final Log log;

  public ImportExportAuditLog(Log log) {
    this.log = log;
  }

  public boolean isAuditable(UserServletContext context) {
    return context.target instanceof Folder
        && context.formatter != null
        && AUDITED_FORMATS.contains(context.formatter.getType());
  }

  public void logExport(UserServletContext context, boolean failed, long size) {
    log.info(buildLine("FolderExport", context, failed, size));
  }

  public void logImport(UserServletContext context, boolean failed, long size) {
    log.info(buildLine("FolderImport", context, failed, size));
  }

  private String buildLine(String cmd, UserServletContext context, boolean failed, long size) {
    return ZimbraLog.encodeAttrs(
        new String[] {
          "cmd", cmd,
          "account", targetAccountName(context),
          "authAccount", authAccountName(context),
          "folder", ((Folder) context.target).getPath(),
          "fmt", context.formatter.getType().toString(),
          "outcome", resolveOutcome(context, failed).name().toLowerCase(Locale.ROOT),
          "size", String.valueOf(size)
        });
  }

  private Outcome resolveOutcome(UserServletContext context, boolean failed) {
    if (failed || context.getLoggedError() != null) {
      return Outcome.ERROR;
    }
    if (context.hasPartialResults()) {
      return Outcome.PARTIAL;
    }
    return Outcome.SUCCESS;
  }

  private String targetAccountName(UserServletContext context) {
    return context.targetAccount != null ? context.targetAccount.getName() : context.accountPath;
  }

  private String authAccountName(UserServletContext context) {
    return context.getAuthAccount() != null ? context.getAuthAccount().getName() : "anonymous";
  }
}
