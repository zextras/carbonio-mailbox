// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Mountpoint;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetFolderTest {
  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> attrs = Maps.newHashMap();
    prov.createAccount("test@zimbra.com", "secret", attrs);

    attrs = Maps.newHashMap();
    attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("test2@zimbra.com", "secret", attrs);
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

 @Test
 void depth() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  mbox.createFolder(
    null,
    "Inbox/foo/bar/baz",
    new Folder.FolderOptions().setDefaultView(Type.FOLDER));

  // first, test the default setup (full tree)
  Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
  Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  Element folder = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(folder, "top-level folder is listed");

  folder = getSubfolder(folder, "Inbox");
  assertNotNull(folder, "Inbox is listed");

  folder = getSubfolder(folder, "foo");
  assertNotNull(folder, "foo is listed");

  folder = getSubfolder(folder, "bar");
  assertNotNull(folder, "bar is listed");

  folder = getSubfolder(folder, "baz");
  assertNotNull(folder, "baz is listed");

  assertTrue(folder.listElements(MailConstants.E_FOLDER).isEmpty(), "no more subfolders");

  // next, test constraining to a single level of subfolders
  request.addAttribute(MailConstants.A_FOLDER_DEPTH, 1);
  response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  folder = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(folder, "top-level folder is listed");

  folder = getSubfolder(folder, "Inbox");
  assertNotNull(folder, "Inbox is listed");

  folder = getSubfolder(folder, "foo");
  assertNull(folder, "foo is listed");
 }

 @Test
 void view() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

  Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(Type.FOLDER);
  mbox.createFolder(null, "foo", fopt);
  mbox.createFolder(null, "bar/baz", fopt);
  mbox.createFolder(null, "Inbox/woot", fopt);

  Element request =
    new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST)
      .addAttribute(MailConstants.A_DEFAULT_VIEW, Type.FOLDER.toString());
  Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  Element root = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(root, "top-level folder is listed");
  assertTrue(isStubbed(root), "top level folder is stubbed");

  Set<String> subfolderNames = ImmutableSet.of("Trash", "foo", "bar", "Inbox");
  Set<String> stubbedSubfolderNames = ImmutableSet.of("bar", "Inbox");
  List<Element> subfolders = root.listElements(MailConstants.E_FOLDER);
  assertEquals(subfolderNames.size(), subfolders.size(), "number of listed subfolders");
  for (Element subfolder : subfolders) {
   String subfolderName = subfolder.getAttribute(MailConstants.A_NAME);
   assertTrue(subfolderNames.contains(subfolderName), "");
   boolean expectStubbed = stubbedSubfolderNames.contains(subfolderName);
   assertNotNull(
     isStubbed(subfolder),
     subfolderName + " folder is " + (expectStubbed ? "" : " not") + " stubbed");
  }

  Element leaf = getSubfolder(getSubfolder(root, "bar"), "baz");
  assertNotNull(leaf, "leaf node present");
  assertFalse(isStubbed(leaf), "leaf not stubbed");
 }

 @Test
 void mount() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Account acct2 = Provisioning.getInstance().get(Key.AccountBy.name, "test2@zimbra.com");

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
  Mailbox mbox2 = MailboxManager.getInstance().getMailboxByAccount(acct2);

  Folder.FolderOptions fopt = new Folder.FolderOptions().setDefaultView(Type.FOLDER);
  int folderId = mbox2.createFolder(null, "foo", fopt).getId();
  Folder folder = mbox2.getFolderById(null, folderId);
  mbox2.createFolder(null, "bar", folderId, fopt);
  mbox2.grantAccess(
    null,
    folderId,
    acct.getId(),
    ACL.GRANTEE_USER,
    (short) (ACL.RIGHT_READ | ACL.RIGHT_WRITE),
    null);

  Mountpoint mpt =
    mbox.createMountpoint(
      null,
      Mailbox.ID_FOLDER_USER_ROOT,
      "remote",
      acct2.getId(),
      folderId,
      folder.getUuid(),
      Type.COMMENT,
      0,
      (byte) 2,
      false);

  // fetch the mountpoint directly
  Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
  request.addElement(MailConstants.E_FOLDER).addAttribute(MailConstants.A_FOLDER, mpt.getId());
  Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  Element root = response.getOptionalElement(MailConstants.E_MOUNT);
  assertNotNull(root, "top-level mountpoint is listed");
  assertFalse(isStubbed(root), "top level mountpont is not stubbed");

  Element leaf = getSubfolder(root, "bar");
  assertNotNull(leaf, "leaf node present");
  assertFalse(isStubbed(leaf), "leaf not stubbed");

  // fetch the entire tree (does not recurse through mountpoint)
  request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
  response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  root = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(root, "top-level folder is listed");

  Element mount = getSubfolder(root, "remote");
  assertNotNull(mount, "mountpoint present");
  assertTrue(isStubbed(mount), "mountpoint stubbed");

  leaf = getSubfolder(mount, "bar");
  assertNull(leaf, "leaf node not present");

  // fetch the entire tree, traversing mountpoints
  request.addAttribute(MailConstants.A_TRAVERSE, true);
  response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  root = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(root, "top-level folder is listed");

  mount = getSubfolder(root, "remote");
  assertNotNull(mount, "mountpoint present");
  assertFalse(isStubbed(mount), "mountpoint not stubbed");

  leaf = getSubfolder(mount, "bar");
  assertNotNull(leaf, "leaf node present");
  assertFalse(isStubbed(leaf), "leaf node not stubbed");

  // fetch the tree to a depth of 1, traversing mountpoints
  request
    .addAttribute(MailConstants.A_TRAVERSE, true)
    .addAttribute(MailConstants.A_FOLDER_DEPTH, 1);
  response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  root = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(root, "top-level folder is listed");

  mount = getSubfolder(root, "remote");
  assertNotNull(mount, "mountpoint present");
  assertFalse(isStubbed(mount), "mountpoint not stubbed");

  leaf = getSubfolder(mount, "bar");
  assertNull(leaf, "leaf node not present");

  // broken link
  mbox2.delete(null, folderId, MailItem.Type.FOLDER);

  response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

  root = response.getOptionalElement(MailConstants.E_FOLDER);
  assertNotNull(root, "top-level folder is listed");

  mount = getSubfolder(root, "remote");
  assertNotNull(mount, "mountpoint present");
  assertTrue(isStubbed(mount), "mountpoint is stubbed");
  assertTrue(
    mount.getAttributeBool(MailConstants.A_BROKEN, false), "mountpoint is broken");

  leaf = getSubfolder(mount, "bar");
  assertNull(leaf, "leaf node absent");
 }

  private static final Set<String> FOLDER_TYPES =
      ImmutableSet.of(MailConstants.E_FOLDER, MailConstants.E_MOUNT, MailConstants.E_SEARCH);

  private static Element getSubfolder(Element parent, String foldername) {
    if (parent != null) {
      for (Element elt : parent.listElements()) {
        if (FOLDER_TYPES.contains(elt.getName())
            && foldername.equals(elt.getAttribute(MailConstants.A_NAME, null))) {
          return elt;
        }
      }
    }
    return null;
  }

  private static boolean isStubbed(Element folder) {
    return folder.getAttribute(MailConstants.A_SIZE, null) == null;
  }
}
