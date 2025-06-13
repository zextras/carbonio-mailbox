package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.UUID;
import org.apache.commons.net.imap.IMAPSClient;
import org.junit.jupiter.api.*;

// test it with containers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled()
class RemoteImapServerTest {

	private final String HOST = "localhost";
	private final int PORT = 7143; // 993
	private final String USERNAME = "test@demo.zextras.io";
	private final String PASSWORD = "password";
	private IMAPSClient imapClient;
	private static final String remoteFolder = "SharedFolder";

	@BeforeAll
	void connect() throws IOException {
		imapClient = new IMAPSClient(false);
		imapClient.connect(HOST, PORT);
		String reply = imapClient.getReplyString();
		assertTrue(reply.startsWith("* OK"), "Connection failed: " + reply);
	}

	@AfterAll
	void disconnect() throws IOException {
		if (imapClient.isConnected()) {
			imapClient.logout();
			imapClient.disconnect();
		}
	}

	@Test
	void testLogin() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		assertTrue(imapClient.getReplyString().contains("LOGIN completed"));
	}

	@Test
	void testListMailboxes() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		final String reply = listFolders();
		assertTrue(reply.contains("INBOX"), "INBOX not found in mailbox list");
	}

	private String listFolders() throws IOException {
		imapClient.list("", "*");
		String reply = imapClient.getReplyString();
		System.out.println("LIST:\n" + reply);
		return reply;
	}

	@Test
	void testSelectInbox() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		imapClient.select("INBOX");
		String reply = imapClient.getReplyString();
		assertTrue(reply.contains("OK"), "INBOX not selected properly: " + reply);
	}

	@Test
	void testSelectRemoteFolder() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		imapClient.select(remoteFolder);
		String reply = imapClient.getReplyString();
		assertTrue(
				reply.contains("OK"),
				"Remote folder \"" + remoteFolder + "\" not selected properly: " + reply);
	}

	@Test
	void testFetchHeaders() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		imapClient.select("INBOX");
		imapClient.fetch("1", "(BODY[HEADER.FIELDS (SUBJECT FROM DATE)])");
		String reply = imapClient.getReplyString();
		System.out.println("Fetch Headers:\n" + reply);
		assertTrue(
				reply.contains("Subject") || reply.contains("BODY[HEADER.FIELDS"), "No header found");
	}

	@Test
	void testFetchBodyText() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		imapClient.select("INBOX");
		imapClient.fetch("1", "(BODY[TEXT])");
		String reply = imapClient.getReplyString();
		System.out.println("Fetch Body:\n" + reply);
		assertTrue(
				reply.contains("BODY[TEXT]") || reply.contains("OK"), "No body found or fetch failed");
	}

	@Test
	void testSearchBySubject() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		imapClient.select("INBOX");
		imapClient.search("SUBJECT \"Test\"");
		String[] replies = imapClient.getReplyStrings();
		System.out.println("Search:\n" + String.join("\n", replies));
		assertTrue(
				replies[0].startsWith("* SEARCH") || replies[1].contains("OK"),
				"Search failed or returned nothing");
	}

	@Test
	void testCreateAndDeleteFolder() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		String folderName = "TempTestFolder";

		createFolder(folderName);

		deleteFolder(folderName);
	}

	private void deleteFolder(String folderName) throws IOException {
		imapClient.delete(folderName);
		String deleteReply = imapClient.getReplyString();
		System.out.println("Delete Folder:\n" + deleteReply);
		assertTrue(deleteReply.contains("OK"), "Folder deletion failed");
	}

	private void createFolder(String folderName) throws IOException {
		imapClient.create(folderName);
		String createReply = imapClient.getReplyString();
		System.out.println("Create Folder:\n" + createReply);
		assertTrue(createReply.contains("OK"), "Folder creation failed");
	}

	@Test
	void concurrencyTest_SelectFolder() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		final int threadCount = 10;
		runConcurrentTasks(threadCount, () -> {
			try {
				imapClient.select("INBOX");
				String reply = imapClient.getReplyString();
				assertTrue(reply.contains("OK"), "INBOX not selected properly: " + reply);
			} catch (IOException e) {
				Assertions.fail("Exception in thread: " + e.getMessage());
			}
		});
	}

	@Test
	void concurrencyTest_SelectRemoteFolder() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		final int threadCount = 1;
		runConcurrentTasks(threadCount, () -> {
			try {
				listFolders();
				imapClient.select("SharedFolder");
				String reply = imapClient.getReplyString();
				final boolean ok = reply.contains("OK");
				System.out.println("reply was = " + reply);
				assertTrue(
						ok,
						"Remote folder \"" + remoteFolder + "\" not selected properly: " + reply);
			} catch (IOException e) {
				Assertions.fail("Exception in thread: " + e.getMessage());
			}
		});
	}

	@Test
	void concurrencyTest_CreateDeleteFolder() throws IOException {
		imapClient.login(USERNAME, PASSWORD);
		final int threadCount = 10;
		runConcurrentTasks(threadCount, () -> {
			try {
				final String randomFolderName = UUID.randomUUID().toString();
				createFolder(randomFolderName);
				deleteFolder(randomFolderName);
			} catch (IOException e) {
				Assertions.fail("Exception in thread: " + e.getMessage());
			}
		});
	}

	private void runConcurrentTasks(int threadCount, Runnable task) {
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(task);
			threads[i].start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Assertions.fail("Thread interrupted: " + e.getMessage());
			}
		}
	}
}