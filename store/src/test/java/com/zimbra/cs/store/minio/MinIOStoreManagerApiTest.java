package com.zimbra.cs.store.minio;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.store.StoreManager;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import qa.unittest.TestUtil;

public class MinIOStoreManagerApiTest extends MailboxTestSuite {
	private static GenericContainer<?> minioContainer;
	private static MinioClient minioClient;
	private static final String BUCKET_NAME = "mailbox-blobs";

	@BeforeAll
	static void setupContainer() throws Exception {
		minioContainer = new GenericContainer<>("minio/minio:latest")
				.withExposedPorts(9000)
				.withEnv("MINIO_ROOT_USER", "minioadmin")
				.withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
				.withCommand("server /data");
		minioContainer.start();

		String endpoint =
				"http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000);

		minioClient = MinioClient.builder()
				.endpoint(endpoint)
				.credentials("minioadmin", "minioadmin")
				.build();

		// Ensure the bucket exists
		if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
		}
		LC.zimbra_class_store.setDefault(MinIOStoreManager.class.getName());
		LC.minio_store_user.setDefault("minioadmin");
		LC.minio_store_password.setDefault("minioadmin");
		LC.minio_store_url.setDefault(endpoint);
		StoreManager.setInstance(new MinIOStoreManager());
	}

	@Test
	void shouldStoreMessageInMinIO() throws Exception {
		final Account account = createAccount().create();
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
		TestUtil.addMessage(mailbox, "Test minio");

	}
}
