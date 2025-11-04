package com.zimbra.cs.store.storages;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.storages.api.StoragesClient;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.store.StoreManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpResponse;
import qa.unittest.MessageBuilder;
import qa.unittest.TestUtil;

public class StoragesManagerIT extends MailboxTestSuite {
	private static StoragesClient storagesClient;
	private static ClientAndServer storagesServer;

	@BeforeAll
	static void setup() {
		storagesServer = startClientAndServer(20010);
		storagesClient = StoragesClient.atUrl("http://localhost:20010");
		StoreManager.setInstance(new StoragesManager(new StoragesClientAdapter(storagesClient)));
	}

	@AfterAll
	static void teardown() {
		storagesServer.stop();
		StoreManager.setInstance(null);
	}

	@Test
	void shouldStoreMessageInMinIO() throws Exception {
		final Account account = createAccount().create();
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
		final Message message = TestUtil.addMessage(mailbox, "Test minio");

		final Message messageById = mailbox.getMessageById(null, message.getId());

		Assertions.assertEquals(messageById.getSubject(), "Test minio");
	}
	@Test
	void shouldCopyMessageInMinIO() throws Exception {
		final Account account = createAccount().create();
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
		final Message message = TestUtil.addMessage(mailbox, "Test minio");

		final MailItem copied = mailbox.copy(null, message.getId(), Type.MESSAGE,
				Mailbox.ID_FOLDER_DRAFTS);

		Assertions.assertEquals(copied.getSubject(), "Test minio");
	}

	@Test
	void shouldStoreAndRetrieveMailContent() throws Exception {
		final Account account = createAccount().create();
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
		final String body = "This is the body stored on minio";
		final String from = "from-account@test.com";
		final String subject = "Test minio";
		final String mimeMessage = new MessageBuilder().withSubject(subject)
				.withFrom(from)
				.withBody(body).create();
		mockStoragesUpload();
		final Message message = TestUtil.addMimeMessage(mailbox, mimeMessage);

		final Message messageById = mailbox.getMessageById(null, message.getId());

		final String stringBody = new String(messageById.getContent());
		Assertions.assertTrue(stringBody.contains(body));
		Assertions.assertTrue(stringBody.contains("Subject: " + subject));
		Assertions.assertTrue(stringBody.contains("From: " + from));
	}

	private static Expectation[] mockStoragesUpload() {
		return storagesServer
				.when(request().withPath("/upload"))
				.respond(
						HttpResponse.response("")
								.withStatusCode(200));
	}
}
