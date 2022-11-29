// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.server;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.datasource.CalDavDataImport;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import qa.unittest.TestUtil;
import com.zimbra.soap.admin.type.DataSourceType;

public class TestCalDavImportServer extends TestCase {
    private static final String NAME_PREFIX = TestCalDavImportServer.class.getSimpleName();
    private static final String USER_NAME = NAME_PREFIX + "user1";
    private static final String DATA_SOURCE_NAME = NAME_PREFIX;
    private static final String TEMP_USER_NAME = NAME_PREFIX + "Temp";
    private Folder rootFolder = null;
    private Account account = null;
    private Mailbox mbox = null;

    @Override
    public void setUp() throws Exception {
        cleanUp();
        Provisioning prov = Provisioning.getInstance();
        // create user1 account
        Account user1Account = prov.createAccount(TestUtil.getAddress(USER_NAME), "test123", null);
        // load the mailbox
        Mailbox user1Mbox = MailboxManager.getInstance().getMailboxByAccount(user1Account);
        // Create temp account and mailbox
        account = prov.createAccount(TestUtil.getAddress(TEMP_USER_NAME), "test123", null);
        mbox = MailboxManager.getInstance().getMailboxByAccount(account);
        rootFolder = mbox.createFolder(null, USER_NAME, Mailbox.ID_FOLDER_ROOT, new Folder.FolderOptions().setDefaultView(MailItem.Type.APPOINTMENT));
        createDataSource();
    }

    public void testRootFolderSyncToken() throws Exception {
        assertTrue(rootFolder.getLastSyncDate() == 0);
        // sync data source
        CalDavDataImport davDataImport = new CalDavDataImport(getDataSource());
        davDataImport.importData(null, true);
        // make sure sync token is updated on root folder
        assertTrue(rootFolder.getLastSyncDate() > 0);
    }

    @Override
    public void tearDown() throws Exception {
        cleanUp();
    }

    private void createDataSource() throws Exception {
        Provisioning prov = Provisioning.getInstance();
        int port = Integer.parseInt(TestUtil.getServerAttr(Provisioning.A_zimbraMailSSLPort));
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraDataSourceEnabled, ProvisioningConstants.TRUE);
        attrs.put(Provisioning.A_zimbraDataSourceHost, TestUtil.getServerAttr(Provisioning.A_zimbraServiceHostname));
        attrs.put(Provisioning.A_zimbraDataSourcePort, Integer.toString(port));
        attrs.put(Provisioning.A_zimbraDataSourceUsername, USER_NAME);
        attrs.put(Provisioning.A_zimbraDataSourcePassword, "test123");
        attrs.put(Provisioning.A_zimbraDataSourceFolderId, Integer.toString(rootFolder.getId()));
        attrs.put(Provisioning.A_zimbraDataSourceConnectionType, "ssl");
        attrs.put(Provisioning.A_zimbraDataSourceAttribute, "p:/principals/users/_USERNAME_");
        prov.createDataSource(account, DataSourceType.caldav, DATA_SOURCE_NAME, attrs);
    }

    private DataSource getDataSource() throws Exception {
        Provisioning prov = Provisioning.getInstance();
        return prov.get(account, Key.DataSourceBy.name, DATA_SOURCE_NAME);
    }

    private void cleanUp() throws Exception {
        // Delete data source
        Account account = TestUtil.getAccount(TEMP_USER_NAME);
        if (account != null) {
            Provisioning prov = Provisioning.getInstance();
            DataSource ds = prov.get(account, Key.DataSourceBy.name, DATA_SOURCE_NAME);
            if (ds != null) {
                prov.deleteDataSource(account, ds.getId());
            }
            TestUtil.deleteTestData(TEMP_USER_NAME, NAME_PREFIX);
            TestUtil.deleteAccount(TEMP_USER_NAME);
        }
        account = TestUtil.getAccount(USER_NAME);
        if (account != null) {
            TestUtil.deleteTestData(USER_NAME, NAME_PREFIX);
            TestUtil.deleteAccount(USER_NAME);
        }
    }
}
