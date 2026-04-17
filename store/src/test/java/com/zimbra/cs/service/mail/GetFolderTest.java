// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableSet;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.datasource.CalDavDataImport;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.CustomMetadata;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.soap.admin.type.DataSourceType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GetFolderTest extends MailboxTestSuite {

	/**
	 * Verifies that a folder subscribed to an external iCal/ICS URL:
	 * <ul>
	 *   <li>returns {@code perm="r"} to signal it is effectively read-only (content is
	 *       synced one-way from the external source and cannot be written back)</li>
	 *   <li>includes the {@code 'y'} (SYNCFOLDER) flag in its {@code f} attribute to
	 *       indicate it is a sync folder backed by an external data source</li>
	 * </ul>
	 */
	@Test
	void externalICalUrlFolderReturnsReadOnlyPermAndSyncFolderFlag() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		// Create a calendar folder with an external iCal URL.
		Folder.FolderOptions folderOptions = new Folder.FolderOptions()
				.setDefaultView(Type.APPOINTMENT)
				.setUrl("https://calendar.example.com/calendar.ics");
		Folder calFolder = mbox.createFolder(null, "ExternalCal", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		// Fetch the folder via GetFolder
		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		// perm="r" — clients should not allow creating/editing items in this folder
		assertEquals(
				String.valueOf(ACL.ABBR_READ),
				folderElem.getAttribute(MailConstants.A_RIGHTS, null),
				"external iCal URL folder should report perm='r' (read-only)");

		// flags should contain 'y' (SYNCFOLDER) — folder has an external data source
		String flags = folderElem.getAttribute(MailConstants.A_FLAGS, "");
		String syncFolderFlagChar = com.zimbra.cs.mailbox.Flag.toString(
				com.zimbra.cs.mailbox.Flag.BITMASK_SYNCFOLDER);
		assertTrue(
				flags.contains(syncFolderFlagChar),
				"external iCal URL folder should have SYNCFOLDER flag 'y', got flags='" + flags + "'");
	}

	@Test
	void externalICalUrlFolderReturnsLastSyncDate() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		Folder.FolderOptions folderOptions =
				new Folder.FolderOptions()
						.setDefaultView(Type.APPOINTMENT)
						.setUrl("https://calendar.example.com/calendar.ics");
		Folder calFolder =
				mbox.createFolder(null, "ExternalCalWithSyncDate", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		long expectedSyncEpochMillis = 1735744115000L;
		mbox.setSubscriptionData(null, calFolder.getId(), expectedSyncEpochMillis, "test-guid");

		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request
				.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));
		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		long expectedSyncEpochSeconds = expectedSyncEpochMillis / 1000;
		assertEquals(
				expectedSyncEpochSeconds,
				folderElem.getAttributeLong(MailConstants.A_LAST_SYNC_DATE, 0),
				"external iCal URL folder should return last successful sync date in epoch seconds");
	}

	/**
	 * Verifies that GetFolder response includes dsId and dsType attributes on the folder
	 * when it is the root folder of a CalDAV DataSource, allowing clients to identify
	 * datasource-synced folders without needing to cross-reference GetDataSources.
	 */
	@Test
	void caldavDataSourceRootFolderIncludesDsIdAndDsType() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		// Create a calendar folder that will be the root of the CalDAV datasource
		Folder.FolderOptions folderOptions = new Folder.FolderOptions()
				.setDefaultView(Type.APPOINTMENT);
		Folder calFolder = mbox.createFolder(null, "CalDAV Root", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		// Create a CalDAV datasource pointing to this folder
		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(calFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");

		DataSource caldavDs = Provisioning.getInstance().createDataSource(
				acct, DataSourceType.caldav, "My CalDAV", dsAttrs);

		// Fetch the folder via GetFolder
		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		// Verify dsId is present and matches the datasource ID
		String dsId = folderElem.getAttribute(MailConstants.A_DATASOURCE_ID, null);
		assertNotNull(dsId, "datasource root folder should have dsId attribute");
		assertEquals(caldavDs.getId(), dsId, "dsId should match the datasource ID");

		// Verify dsType is present and set to 'caldav'
		String dsType = folderElem.getAttribute(MailConstants.A_DATASOURCE_TYPE, null);
		assertNotNull(dsType, "datasource root folder should have dsType attribute");
		assertEquals(DataSourceType.caldav.toString(), dsType, "dsType should be 'caldav'");
	}

	@Test
	void caldavDataSourceRootFolderWithImportOnlyIsMarkedReadOnly() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		Folder.FolderOptions folderOptions = new Folder.FolderOptions().setDefaultView(Type.APPOINTMENT);
		Folder calFolder = mbox.createFolder(null, "CalDAV Root Import-Only", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(calFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");
		dsAttrs.put(Provisioning.A_zimbraDataSourceImportOnly, "TRUE");

		Provisioning.getInstance().createDataSource(acct, DataSourceType.caldav, "My CalDAV Import-Only", dsAttrs);

		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		String rights = folderElem.getAttribute(MailConstants.A_RIGHTS, null);
		assertEquals(
				String.valueOf(ACL.ABBR_READ),
				rights,
				"import-only caldav datasource root folder should have read-only permissions");
	}

	@Test
	void caldavDataSourceRootFolderReturnsLastSyncDate() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		Folder.FolderOptions folderOptions = new Folder.FolderOptions()
				.setDefaultView(Type.APPOINTMENT);
		Folder calFolder = mbox.createFolder(null, "CalDAV Root With Sync Date", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(calFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");

		Provisioning.getInstance().createDataSource(acct, DataSourceType.caldav, "My CalDAV", dsAttrs);

		long expectedSyncEpochMillis = 1735744115000L;
		mbox.setSyncDate(null, calFolder.getId(), expectedSyncEpochMillis);

		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request
				.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));
		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		long expectedSyncEpochSeconds = expectedSyncEpochMillis / 1000;
		assertEquals(
				expectedSyncEpochSeconds,
				folderElem.getAttributeLong(MailConstants.A_LAST_SYNC_DATE, 0),
				"caldav datasource root folder should return last successful sync date in epoch seconds");
	}

	@Test
	void caldavDataSourceRootFolderUsesCustomLastSuccessfulSyncDateInsteadOfSyncToken() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		Folder.FolderOptions folderOptions = new Folder.FolderOptions().setDefaultView(Type.APPOINTMENT);
		Folder calFolder = mbox.createFolder(null, "CalDAV Root Sync Token", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(calFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");
		Provisioning.getInstance().createDataSource(acct, DataSourceType.caldav, "My CalDAV", dsAttrs);

		mbox.setSyncDate(null, calFolder.getId(), 19000L);

		long expectedSyncEpochMillis = 1735744115000L;
		CustomMetadata custom = new CustomMetadata(CalDavDataImport.CUSTOM_METADATA_SECTION);
		custom.put(
				CalDavDataImport.CUSTOM_METADATA_KEY_LAST_SUCCESSFUL_SYNC_MS,
				String.valueOf(expectedSyncEpochMillis));
		mbox.setCustomData(null, calFolder.getId(), MailItem.Type.FOLDER, custom);

		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request
				.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));
		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		assertEquals(
				expectedSyncEpochMillis / 1000,
				folderElem.getAttributeLong(MailConstants.A_LAST_SYNC_DATE, 0),
				"caldav datasource root folder should expose real last successful sync timestamp, not sync token");
	}

	@Test
	void caldavDataSourceRootFolderMetadataNotificationIncludesLastSyncDate() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		Folder calFolder = mbox.createFolder(
				null,
				"CalDAV Root Notify Metadata",
				Mailbox.ID_FOLDER_USER_ROOT,
				new Folder.FolderOptions().setDefaultView(Type.APPOINTMENT));

		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(calFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");
		Provisioning.getInstance().createDataSource(acct, DataSourceType.caldav, "My CalDAV", dsAttrs);

		long expectedSyncEpochMillis = 1735744115000L;
		CustomMetadata custom = new CustomMetadata(CalDavDataImport.CUSTOM_METADATA_SECTION);
		custom.put(
				CalDavDataImport.CUSTOM_METADATA_KEY_LAST_SUCCESSFUL_SYNC_MS,
				String.valueOf(expectedSyncEpochMillis));
		mbox.setCustomData(null, calFolder.getId(), MailItem.Type.FOLDER, custom);

		Element notify = new Element.XMLElement("notify");
		Element folderElem = ToXML.encodeFolder(
				notify,
				new ItemIdFormatter(),
				new OperationContext(mbox),
				calFolder,
				Change.METADATA);

		assertEquals(
				expectedSyncEpochMillis / 1000,
				folderElem.getAttributeLong(MailConstants.A_LAST_SYNC_DATE, 0),
				"metadata-only CalDAV folder notifications should include lsd");
	}

	@Test
	void regularFolderMetadataNotificationDoesNotIncludeLastSyncDate() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		Folder regularFolder = mbox.createFolder(
				null,
				"Regular Calendar Notify Metadata",
				Mailbox.ID_FOLDER_USER_ROOT,
				new Folder.FolderOptions().setDefaultView(Type.APPOINTMENT));

		Element notify = new Element.XMLElement("notify");
		Element folderElem = ToXML.encodeFolder(
				notify,
				new ItemIdFormatter(),
				new OperationContext(mbox),
				regularFolder,
				Change.METADATA);

		assertNull(
				folderElem.getAttribute(MailConstants.A_LAST_SYNC_DATE, null),
				"metadata-only notifications on non-CalDAV folders should not include lsd");
	}

	/**
	 * Verifies that non-datasource folders do NOT include dsId and dsType attributes.
	 */
	@Test
	void regularFolderDoesNotIncludeDsIdAndDsType() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		// Create a regular calendar folder (not a datasource root)
		Folder.FolderOptions folderOptions = new Folder.FolderOptions()
				.setDefaultView(Type.APPOINTMENT);
		Folder calFolder = mbox.createFolder(null, "Regular Calendar", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		// Fetch the folder via GetFolder
		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(calFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "folder element should be present in response");

		// Verify dsId and dsType are NOT present
		String dsId = folderElem.getAttribute(MailConstants.A_DATASOURCE_ID, null);
		assertNull(dsId, "regular folder should not have dsId attribute");

		String dsType = folderElem.getAttribute(MailConstants.A_DATASOURCE_TYPE, null);
		assertNull(dsType, "regular folder should not have dsType attribute");
	}

	/**
	 * Verifies that sub-folders created by CalDAV sync under a datasource root folder
	 * do NOT include dsId/dsType attributes (only the root folder should have them).
	 */
	@Test
	void caldavSubFolderDoesNotIncludeDsIdAndDsType() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		// Create a CalDAV datasource root folder
		Folder.FolderOptions folderOptions = new Folder.FolderOptions()
				.setDefaultView(Type.APPOINTMENT);
		Folder rootFolder = mbox.createFolder(null, "CalDAV Root", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		// Create a CalDAV datasource
		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(rootFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");

		Provisioning.getInstance().createDataSource(
				acct, DataSourceType.caldav, "My CalDAV", dsAttrs);

		// Create a sub-folder (simulating what CalDAV import would create)
		Folder subFolder = mbox.createFolder(null, "Work Calendar", rootFolder.getId(), folderOptions);

		// Fetch the sub-folder via GetFolder
		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(subFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "subfolder element should be present in response");

		// Verify dsId and dsType are NOT on the sub-folder
		String dsId = folderElem.getAttribute(MailConstants.A_DATASOURCE_ID, null);
		assertNull(dsId, "datasource sub-folder should not have dsId attribute (only root should)");

		String dsType = folderElem.getAttribute(MailConstants.A_DATASOURCE_TYPE, null);
		assertNull(dsType, "datasource sub-folder should not have dsType attribute (only root should)");
	}

	@Test
	void caldavSubFolderUnderImportOnlyIsMarkedReadOnly() throws Exception {
		var acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);

		// Create a CalDAV datasource root folder with importOnly=true
		Folder.FolderOptions folderOptions = new Folder.FolderOptions()
				.setDefaultView(Type.APPOINTMENT);
		Folder rootFolder = mbox.createFolder(null, "CalDAV Import-Only Root", Mailbox.ID_FOLDER_USER_ROOT, folderOptions);

		Map<String, Object> dsAttrs = new HashMap<>();
		dsAttrs.put(Provisioning.A_zimbraDataSourceFolderId, String.valueOf(rootFolder.getId()));
		dsAttrs.put(Provisioning.A_zimbraDataSourceHost, "caldav.example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePort, "443");
		dsAttrs.put(Provisioning.A_zimbraDataSourceUsername, "user@example.com");
		dsAttrs.put(Provisioning.A_zimbraDataSourcePassword, "password");
		dsAttrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
		dsAttrs.put(Provisioning.A_zimbraDataSourceEnabled, "TRUE");
		dsAttrs.put(Provisioning.A_zimbraDataSourceImportOnly, "TRUE");

		Provisioning.getInstance().createDataSource(
				acct, DataSourceType.caldav, "My CalDAV Import-Only", dsAttrs);

		// Create a sub-folder under the import-only datasource root
		Folder subFolder = mbox.createFolder(null, "Work Calendar", rootFolder.getId(), folderOptions);

		// Fetch the sub-folder via GetFolder
		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER)
				.addAttribute(MailConstants.A_FOLDER, String.valueOf(subFolder.getId()));

		Element response = new GetFolder().handle(request, ServiceTestUtil.getRequestContext(acct));

		Element folderElem = response.getOptionalElement(MailConstants.E_FOLDER);
		assertNotNull(folderElem, "subfolder element should be present in response");

		String rights = folderElem.getAttribute(MailConstants.A_RIGHTS, null);
		assertEquals(
				String.valueOf(ACL.ABBR_READ),
				rights,
				"child folder under import-only caldav datasource should have read-only permissions");
	}

	@Disabled
	@Test
	void depth() throws Exception {
		var acct = createAccount().create();
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

	@Disabled
	@Test
	void view() throws Exception {
		var acct = createAccount().create();
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

	@Disabled
	@Test
	void mount() throws Exception {
		var acct = createAccount().create();
		var acct2 = createAccount().create();
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
						Type.CONVERSATION,
						0,
						(byte) 2,
						false);

		// fetch the mountpoint directly
		Element request = new Element.XMLElement(MailConstants.GET_FOLDER_REQUEST);
		request.addUniqueElement(MailConstants.E_FOLDER).addAttribute(MailConstants.A_FOLDER, mpt.getId());
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
