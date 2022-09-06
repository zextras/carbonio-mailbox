// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource.imap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.datasource.DataSourceFolderMapping;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.db.DbDataSource;
import com.zimbra.cs.db.DbDataSource.DataSourceItem;
import com.zimbra.cs.db.DbImapFolder;
import com.zimbra.cs.db.DbImapMessage;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImapFolder extends DataSourceFolderMapping {
  private String localPath;
  private Long uidValidity;
  private static final String METADATA_KEY_LOCAL_PATH = "lp";
  private static final String METADATA_KEY_UID_VALIDITY = "uv";

  public ImapFolder(DataSource ds, DataSourceItem dsi) throws ServiceException {
    super(ds, dsi);
  }

  public ImapFolder(DataSource ds, int itemId) throws ServiceException {
    super(ds, itemId);
  }

  public ImapFolder(DataSource ds, String remoteId) throws ServiceException {
    super(ds, remoteId);
  }

  public ImapFolder(DataSource ds, int itemId, String remoteId, String localPath, Long uidValidity)
      throws ServiceException {
    super(ds, itemId, remoteId);
    setLocalPath(localPath);
    setUidValidity(uidValidity);
  }

  public String getLocalPath() {
    return localPath;
  }

  public long getUidValidity() {
    return uidValidity;
  }

  public void setLocalPath(String localPath) {
    dsi.md.put(METADATA_KEY_LOCAL_PATH, this.localPath = localPath);
  }

  public void setUidValidity(Long uidValidity) {
    dsi.md.put(METADATA_KEY_UID_VALIDITY, this.uidValidity = uidValidity);
  }

  @Override
  protected void parseMetaData() throws ServiceException {
    localPath = dsi.md.get(METADATA_KEY_LOCAL_PATH, "");
    uidValidity = dsi.md.getLong(METADATA_KEY_UID_VALIDITY, -1);
  }

  public ImapMessage getMessage(int itemId) throws ServiceException {
    return new ImapMessage(ds, itemId);
  }

  public ImapMessage getMessage(long uid) throws ServiceException {
    return new ImapMessage(ds, getItemId(), uid);
  }

  public ImapMessageCollection getMessages() throws ServiceException {
    Collection<DataSourceItem> mappings = getMappingsAndFlags(ds, getItemId());
    ImapMessageCollection imc = new ImapMessageCollection();

    for (DataSourceItem mapping : mappings) imc.add(new ImapMessage(ds, mapping));
    return imc;
  }

  public List<Integer> getNewMessageIds() throws ServiceException {
    Collection<DataSourceItem> mappings = getMappingsAndFlags(ds, getItemId());
    Mailbox mbox = DataSourceManager.getInstance().getMailbox(ds);
    List<Integer> allIds =
        mbox.listItemIds(mbox.getOperationContext(), MailItem.Type.MESSAGE, getItemId());
    List<Integer> newIds = new ArrayList<Integer>();

    loop:
    for (Integer id : allIds) {
      for (DataSourceItem mapping : mappings) {
        if (mapping.itemId == id) {
          mappings.remove(mapping);
          continue loop;
        }
      }
      newIds.add(id);
    }
    return newIds;
  }

  public static ImapFolderCollection getFolders(DataSource ds) throws ServiceException {
    Collection<DataSourceItem> mappings = getMappings(ds, ds.getFolderId());
    ImapFolderCollection ifc = new ImapFolderCollection();

    if (mappings.size() == 0) {
      Mailbox mbox = DataSourceManager.getInstance().getMailbox(ds);

      ZimbraLog.datasource.info("Upgrading IMAP data for %s", ds.getName());
      DbDataSource.deleteAllMappings(ds);
      try {
        for (ImapFolder folderTracker : DbImapFolder.getImapFolders(mbox, ds)) {
          folderTracker.add();
          for (ImapMessage msgTracker : DbImapMessage.getImapMessages(mbox, ds, folderTracker)) {
            msgTracker.add();
          }
        }
      } catch (Exception e) {
        DbDataSource.deleteAllMappings(ds);
        throw ServiceException.FAILURE("IMAP data upgrade failed for " + ds.getName(), e);
      }
      mappings = getMappings(ds, ds.getFolderId());
      DbImapFolder.deleteImapData(mbox, ds.getId());
    }
    for (DataSourceItem mapping : mappings) ifc.add(new ImapFolder(ds, mapping));
    return ifc;
  }

  @Override
  public String toString() {
    return String.format(
        "ImapFolder: { itemId=%d, dataSourceId=%s, localPath=%s, remotePath=%s, uidValidity=%d }",
        dsi.itemId, ds.getId(), localPath, dsi.remoteId, uidValidity);
  }
}
