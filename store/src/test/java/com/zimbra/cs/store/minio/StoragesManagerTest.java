package com.zimbra.cs.store.minio;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.storages.api.StoragesClient;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StoragesManagerTest extends MailboxTestSuite {

	private static StoragesClient storagesClient;

	@BeforeAll
	static void setupContainer() {
		storagesClient = Mockito.mock(StoragesClient.class);
	}

	@Test
	void shouldStoreDataUsingInputStream() throws Exception {
		final Mailbox mailbox = createMailbox();
		final StoragesManager minIOStoreManager = getMinIOStoreManager();
		final String testData = "test data";

		final StoragesStagedBlob stagedBlob = minIOStoreManager.stage(
				generateByData(testData), mailbox);

//		final GetObjectResponse object = getObjectFromMinIO(stagedBlob);
//		final byte[] contentBytes = object.readAllBytes();
//		Assertions.assertEquals(testData, new String(contentBytes));
	}

	@Test
	void shouldStoreIncoming() throws Exception {
		final StoragesManager minIOStoreManager = getMinIOStoreManager();
		final String testData = "test data";

		final StoragesBlob blob = minIOStoreManager.storeIncoming(
				generateByData(testData), true);

//		final GetObjectResponse object = getObjectFromMinIO(blob.getKey());
//		final byte[] contentBytes = object.readAllBytes();
//		Assertions.assertTrue(object.object().contains("incoming"));
//		Assertions.assertEquals(testData, new String(contentBytes));
	}

	@Test
	void shouldDeleteBlob() throws Exception {
		final StoragesManager minIOStoreManager = getMinIOStoreManager();
		final String testData = "test data";
		final StoragesBlob blob = minIOStoreManager.storeIncoming(
				generateByData(testData), true);
		Mockito.verify(storagesClient).delete(Mockito.any());
	}

	private static ByteArrayInputStream generateByData(String testData) {
		return new ByteArrayInputStream(
				testData.getBytes());
	}

	private static StoragesManager getMinIOStoreManager() {
		return new StoragesManager(new StoragesClientAdapter(storagesClient));
	}

	private static Mailbox createMailbox() throws ServiceException {
		final Account account = createAccount().create();
		return MailboxManager.getInstance().getMailboxByAccount(account);
	}
}