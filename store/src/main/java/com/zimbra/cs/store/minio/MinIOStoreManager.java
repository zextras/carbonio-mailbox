package com.zimbra.cs.store.minio;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.StagedBlob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.minio.MinIOStoreManager.MinioBlob;
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
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.activation.DataSource;

public class MinIOStoreManager extends StoreManager<MinioBlob> {

	private final MinioClient minioClient;
	private final String bucketName;

	MinIOStoreManager(MinioClient minioClient, String bucketName) {
		this.minioClient = minioClient;
		this.bucketName = bucketName;
	}

	@Override
	public void startup() throws IOException, ServiceException {
	}

	@Override
	public void shutdown() {
		try {
			minioClient.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
	public BlobBuilder<MinioBlob> getBlobBuilder() throws IOException, ServiceException {
		final String key = "/incoming/" + UUID.randomUUID();
		return new MinIOIncomingBlobBuilder(new MinioBlob(key));
	}

	@Override
	public MinioBlob storeIncoming(InputStream data, boolean storeAsIs)
			throws IOException, ServiceException {
		final String key = "/incoming/" + UUID.randomUUID();
		final int unknownSize = -1;
		uploadOnMinIo(data, unknownSize, key);
		return new MinioBlob(key);
	}

	@Override
	public StagedBlob stage(InputStream data, long actualSize, Mailbox mbox)
			throws IOException, ServiceException {
		return stageOnMinIO(mbox, data, actualSize);
	}

	private MinioStagedBlob stageOnMinIO(Mailbox mailbox, InputStream data, long actualSize)
			throws IOException, ServiceException {
		final String pathname = mailbox.getAccountId() + "/" + UUID.randomUUID();
		final MinioStagedBlob minioStagedBlob = new MinioStagedBlob(pathname, mailbox, "", actualSize);
		uploadOnMinIo(data, actualSize, minioStagedBlob.getKey());
		return minioStagedBlob;
	}

	private void uploadOnMinIo(InputStream data, long actualSize, String key)
			throws IOException, ServiceException {
		try {
			minioClient.putObject(PutObjectArgs.builder()
					.bucket(bucketName)
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
	public StagedBlob stage(MinioBlob blob, Mailbox mbox) throws IOException, ServiceException {
		final long rawSize = blob.getRawSize();
		return stageOnMinIO(mbox, blob.getInputStream(), rawSize);
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
	public boolean delete(MinioBlob blob) throws IOException {
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
	public InputStream getContent(MinioBlob blob) throws IOException {
		return null;
	}

	@Override
	public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlobInfo> blobs)
			throws IOException, ServiceException {
		return false;
	}

	public static class MinioBlob extends Blob {

		private final String key;

		@Override
		public InputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public byte[] getContent() throws IOException {
			return new byte[0];
		}

		public MinioBlob(String key) {
			super(key);
			this.key = key;
		}

		@Override
		public long getRawSize() throws IOException {
			return 0;
		}

		@Override
		public String getName() {
			return key;
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

	public static class MinIOIncomingBlobBuilder implements BlobBuilder<MinioBlob> {

		private final MinioBlob blob;

		public MinIOIncomingBlobBuilder(MinioBlob blob) {
			this.blob = blob;
		}

		public BlobBuilder<MinioBlob> init() {
			return this;
		}

		@Override
		public long getSizeHint() {
			return 0;
		}

		@Override
		public long getTotalBytes() {
			return 0;
		}

		@Override
		public BlobBuilder<MinioBlob> disableCompression(boolean disable) {
			return null;
		}

		@Override
		public int getCompressionThreshold() {
			return 0;
		}

		@Override
		public BlobBuilder<MinioBlob> disableDigest(boolean disable) {
			return null;
		}

		@Override
		public BlobBuilder<MinioBlob> append(InputStream in) throws IOException {
			return null;
		}

		@Override
		public BlobBuilder<MinioBlob> append(byte[] b) throws IOException {
			return null;
		}

		@Override
		public BlobBuilder<MinioBlob> append(byte[] b, int off, int len) throws IOException {
			return null;
		}

		@Override
		public BlobBuilder<MinioBlob> append(ByteBuffer bb) throws IOException {
			return null;
		}

		@Override
		public MinioBlob finish() throws IOException, ServiceException {
			return null;
		}

		@Override
		public MinioBlob getBlob() {
			return null;
		}

		@Override
		public boolean isFinished() {
			return false;
		}

		@Override
		public void dispose() {

		}
	}
}
