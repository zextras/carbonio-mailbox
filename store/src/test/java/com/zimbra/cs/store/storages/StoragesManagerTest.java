package com.zimbra.cs.store.storages;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.storages.api.StoragesClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.store.StoreManager;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoragesManagerTest extends MailboxTestSuite {

	private static StoragesServerMock storagesServerMock;
	private static StoragesManager storagesManager;

	@BeforeAll
	static void setup() {
		storagesServerMock = new StoragesServerMock();
		StoragesClient storagesClient = StoragesClient.atUrl(storagesServerMock.getUrl());
		storagesManager = new StoragesManager(new StoragesClientAdapter(storagesClient));
		StoreManager.setInstance(new StoragesManager(new StoragesClientAdapter(storagesClient)));
	}

	@AfterAll
	static void teardown() {
		storagesServerMock.stop();
		StoreManager.setInstance(null);
	}
	@BeforeEach
	void setUp() throws Exception {
		storagesServerMock.reset();
	}


	@Test
	void shouldStoreDataUsingInputStream() throws Exception {
		final Mailbox mailbox = createMailbox();
		final StoragesManager minIOStoreManager = getStoragesStoreManager();
		final String testData = "test data";

		storagesServerMock.mockStoragesUpload(testData);
		final StoragesStagedBlob stagedBlob = minIOStoreManager.stage(
				generateByData(testData), mailbox);
		// TODO: verify it cas been called with data

//		final GetObjectResponse object = getObjectFromMinIO(stagedBlob);
//		final byte[] contentBytes = object.readAllBytes();
//		Assertions.assertEquals(testData, new String(contentBytes));
	}

	@Test
	void shouldStoreIncoming() throws Exception {
		final StoragesManager minIOStoreManager = getStoragesStoreManager();
		final String testData = "test data";

		storagesServerMock.mockStoragesUpload(testData);
		final StoragesBlob blob = minIOStoreManager.storeIncoming(
				generateByData(testData), true);

//		final GetObjectResponse object = getObjectFromMinIO(blob.getKey());
//		final byte[] contentBytes = object.readAllBytes();
//		Assertions.assertTrue(object.object().contains("incoming"));
//		Assertions.assertEquals(testData, new String(contentBytes));
	}

	@Test
	void shouldDeleteBlob() throws Exception {
		final StoragesManager minIOStoreManager = getStoragesStoreManager();
		final String testData = "test data";
		storagesServerMock.mockStoragesUpload(testData);
		final StoragesBlob blob = minIOStoreManager.storeIncoming(
				generateByData(testData), true);
//		Mockito.verify(storagesClient).delete(Mockito.any());
	}

	private static ByteArrayInputStream generateByData(String testData) {
		return new ByteArrayInputStream(
				testData.getBytes());
	}

	private static StoragesManager getStoragesStoreManager() {
		return storagesManager;
	}

	private static Mailbox createMailbox() throws ServiceException {
		final Account account = createAccount().create();
		return MailboxManager.getInstance().getMailboxByAccount(account);
	}
}