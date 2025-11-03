package com.zimbra.cs.store.minio;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

class MinIOStoreManagerTest extends MailboxTestSuite {

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
	}

	@Test
	void shouldStoreDataUsingInputStream() throws Exception {
		final Mailbox mailbox = createMailbox();
		final MinIOStoreManager minIOStoreManager = getMinIOStoreManager();
		final String testData = "test data";

		final MinioStagedBlob stagedBlob = (MinioStagedBlob) minIOStoreManager.stage(
				generateByData(testData), mailbox);

		final GetObjectResponse object = getObjectFromMinIO(stagedBlob);
		final byte[] contentBytes = object.readAllBytes();
		Assertions.assertEquals(testData, new String(contentBytes));
	}

	@Test
	void shouldStoreIncoming() throws Exception {
		final MinIOStoreManager minIOStoreManager = getMinIOStoreManager();
		final String testData = "test data";

		final MinioBlob blob = minIOStoreManager.storeIncoming(
				generateByData(testData), true);

		final GetObjectResponse object = getObjectFromMinIO(blob.getKey());
		final byte[] contentBytes = object.readAllBytes();
		Assertions.assertTrue(object.object().contains("incoming"));
		Assertions.assertEquals(testData, new String(contentBytes));
	}

	@Test
	void shouldDeleteBlob() throws Exception {
		final MinIOStoreManager minIOStoreManager = getMinIOStoreManager();
		final String testData = "test data";
		final MinioBlob blob = minIOStoreManager.storeIncoming(
				generateByData(testData), true);
		final GetObjectResponse object = getObjectFromMinIO(blob.getKey());
		Assertions.assertNotNull(object);

		minIOStoreManager.delete(blob);
		final Exception exception = Assertions.assertThrows(Exception.class,
				() -> getObjectFromMinIO(blob.getKey()));
		Assertions.assertTrue(exception.getMessage().contains("The specified key does not exist."));
	}

	private static ByteArrayInputStream generateByData(String testData) {
		return new ByteArrayInputStream(
				testData.getBytes());
	}

	private static GetObjectResponse getObjectFromMinIO(MinioStagedBlob stagedBlob)
			throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
		return getObjectFromMinIO(stagedBlob.getMinIoBlob().getKey());
	}

	private static GetObjectResponse getObjectFromMinIO(String key)
			throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
		return minioClient.getObject(GetObjectArgs.builder().
				bucket(BUCKET_NAME).
				object(key).
				build());
	}

	private static MinIOStoreManager getMinIOStoreManager() {
		return new MinIOStoreManager(minioClient, BUCKET_NAME);
	}

	private static Mailbox createMailbox() throws ServiceException {
		final Account account = createAccount().create();
		return MailboxManager.getInstance().getMailboxByAccount(account);
	}
}