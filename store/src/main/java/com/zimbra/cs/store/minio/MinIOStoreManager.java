package com.zimbra.cs.store.minio;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MinIOStoreManager extends StoreManager {

	private static MinioClient minioClient;

	@Override
	public void startup() throws IOException, ServiceException {
		final String url = LC.minio_store_url.value();
		final String user = LC.minio_store_user.value();
		final String password = LC.minio_store_password.value();
		minioClient =
				MinioClient.builder()
						.endpoint(url)
						.credentials(user, password)
						.build();
	}

	@Override
	public void shutdown() {
		try {
			minioClient.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getBucketName() {
		return "";
	}
	private String getIncomingBucket() {
		final String incoming = "incoming";
		createBucketIfNotExists(incoming);
		return incoming;
	}

	private boolean createBucketIfNotExists(String bucketName) {

		// Check if the bucket exists
		try {
			boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
					.bucket(bucketName)
					.build());
			if (!found) {
				minioClient.makeBucket(MakeBucketArgs.builder()
						.bucket(bucketName)
						.build());
				return true;
			}
		} catch (Exception e) {
			// ignore
		}
		return false;
	}

	@Override
	public boolean supports(StoreFeature feature) {
		switch (feature) {
			case BULK_DELETE, CENTRALIZED -> {
				return true;
			}
			default -> {
				return false;
			}
		}
	}

	@Override
	public boolean supports(StoreFeature feature, String locator) {
		return false;
	}

	@Override
	public BlobBuilder getBlobBuilder() throws IOException, ServiceException {
		return new MinIOIncomingBlobBuilder(new MinioBlob("mailbox-blobs/" + UUID.randomUUID()));
	}

	@Override
	public Blob storeIncoming(InputStream data, boolean storeAsIs)
			throws IOException, ServiceException {
		BlobBuilder builder = getBlobBuilder();
		// if the blob is already compressed, *don't* calculate a digest/size from what we write
		builder.disableCompression(storeAsIs).disableDigest(storeAsIs);
		return builder.init().append(data).finish();
	}

	@Override
	public StagedBlob stage(InputStream data, long actualSize, Mailbox mbox)
			throws IOException, ServiceException {
		final MinioBlob minioBlob = new MinioBlob(
				"mailbox-blobs/" + mbox.getAccountId() + "/" + UUID.randomUUID());

		store(minioBlob.getKey(), data, actualSize);
		return new MinioStagedBlob(minioBlob.getKey(), mbox,"", actualSize);
	}

	private static void store(String key, InputStream data, long actualSize)
			throws IOException, ServiceException {
		try {
			minioClient.putObject(PutObjectArgs.builder()
					.bucket("mailbox-blobs")
					.stream(data, actualSize, 10 * 1024 * 1024)
					.object(key)
					.build()
			);
		} catch (ErrorResponseException | ServerException | NoSuchAlgorithmException |
						 InvalidResponseException | InvalidKeyException | InternalException |
						 InsufficientDataException | XmlParserException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	@Override
	public StagedBlob stage(Blob blob, Mailbox mbox) throws IOException, ServiceException {
		MinioBlob minioBlob = (MinioBlob) blob;
		final long rawSize = minioBlob.getRawSize();
		final String key = minioBlob.getKey();
		store(key, minioBlob.getInputStream(), rawSize);
		return new MinioStagedBlob(key, mbox, "", rawSize);
	}

	@Override
	public MailboxBlob copy(MailboxBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		return null;
	}

	@Override
	public MailboxBlob link(StagedBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		return null;
	}

	@Override
	public MailboxBlob renameTo(StagedBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		return null;
	}

	@Override
	public boolean delete(Blob blob) throws IOException {
		return false;
	}

	@Override
	public boolean delete(StagedBlob staged) throws IOException {
		return false;
	}

	@Override
	public boolean delete(MailboxBlob mblob) throws IOException {
		return false;
	}

	@Override
	public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator,
			boolean validate) throws ServiceException {
		return null;
	}

	@Override
	public InputStream getContent(MailboxBlob mboxBlob) throws IOException {
		return null;
	}

	@Override
	public InputStream getContent(Blob blob) throws IOException {
		return null;
	}

	@Override
	public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlobInfo> blobs)
			throws IOException, ServiceException {
		return false;
	}

	public static class MinioBlob extends Blob {
		private final String key;

		public MinioBlob(String key) {
			super(null); // no file
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}
	public static class MinioStagedBlob extends StagedBlob {
		private final String key;

		public MinioStagedBlob(String key, Mailbox mbox, String digest, long size) {
			super(mbox, digest, size);
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		@Override
		public String getLocator() {
			return "";
		}
	}

	public static class MinIOIncomingBlobBuilder extends BlobBuilder {
		private final Blob blob;

		public MinIOIncomingBlobBuilder(Blob blob) {
			super(blob);
			this.blob = blob;
		}
	}
}
