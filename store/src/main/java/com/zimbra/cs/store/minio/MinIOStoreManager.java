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
import com.zimbra.cs.store.minio.MinIOStoreManager.MinioBlob;
import com.zimbra.cs.store.minio.MinIOStoreManager.MinioStagedBlob;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MinIOStoreManager extends StoreManager<MinioBlob, MinioStagedBlob> {

	private final MinioClient minioClient;
	private final String bucketName;

	MinIOStoreManager(MinioClient minioClient, String bucketName) {
		this.minioClient = minioClient;
		this.bucketName = bucketName;
	}

	public MinIOStoreManager() {
		this.minioClient = MinioClient.builder()
				.endpoint(LC.minio_store_url.value())
				.credentials(LC.minio_store_user.value(), LC.minio_store_password.value())
				.build();
		this.bucketName = "mailbox-blobs";
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
		return uploadOnMinIo(data, unknownSize, key);
	}

	@Override
	public MinioStagedBlob stage(InputStream data, long actualSize, Mailbox mbox)
			throws IOException, ServiceException {
		return stageOnMinIO(mbox, data, actualSize);
	}

	private MinioStagedBlob stageOnMinIO(Mailbox mailbox, InputStream data, long actualSize)
			throws IOException, ServiceException {
		final String pathname = mailbox.getAccountId() + "/" + UUID.randomUUID();
		final MinioBlob minioBlob = uploadOnMinIo(data, actualSize, pathname);
		return new MinioStagedBlob(mailbox, minioBlob);
	}

	private MinioBlob uploadOnMinIo(InputStream data, long actualSize, String key)
			throws IOException, ServiceException {
		try {
			minioClient.putObject(PutObjectArgs.builder()
					.bucket(bucketName)
					.stream(data, actualSize, 10 * 1024 * 1024)
					.object(key)
					.build()
			);
			return getFromMinIo(key);
		} catch (ErrorResponseException | ServerException | NoSuchAlgorithmException |
						 InvalidResponseException | InvalidKeyException | InternalException |
						 InsufficientDataException | XmlParserException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	private MinioBlob getFromMinIo(String key)
			throws IOException, ServiceException {
		try {
			final GetObjectResponse minIOResponse = minioClient.getObject(GetObjectArgs.builder()
					.bucket(bucketName)
					.object(key)
					.build()
			);
			final byte[] bytes = minIOResponse.readAllBytes();
			return new MinioBlob(key, bytes, bytes.length);
		} catch (ErrorResponseException | ServerException | NoSuchAlgorithmException |
						 InvalidResponseException | InvalidKeyException | InternalException |
						 InsufficientDataException | XmlParserException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	@Override
	public MinioStagedBlob stage(MinioBlob blob, Mailbox mbox) throws IOException, ServiceException {
		final long rawSize = blob.getRawSize();
		return stageOnMinIO(mbox, blob.getInputStream(), rawSize);
	}

	@Override
	public MailboxBlob copy(MailboxBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		return null;
	}

	/**
	 * This method actually links the message in the same mailbox also.
	 * The message is stored in incoming, then staged, then linked to a path with the fully qualified item id
	 * @param src
	 * @param destMbox
	 * @param destMsgId    mail_item.id value for message in destMbox
	 * @param destRevision mail_item.mod_content value for message in destMbox
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Override
	public MailboxBlob link(MinioStagedBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		final String minIOKey = getMinIOKey(destMbox, destMsgId, destRevision, "");
		final MinioBlob originalBlob = src.getMinIoBlob();
		final MinioBlob minioBlob = uploadOnMinIo(originalBlob.getInputStream(),
				originalBlob.getRawSize(), minIOKey);
		return new MinIOMailboxBlob(destMbox, destMsgId, destRevision, "", minioBlob);
	}

	@Override
	public MailboxBlob renameTo(MinioStagedBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		return null;
	}

	@Override
	public boolean delete(MinioBlob blob) throws IOException {
		return false;
	}

	@Override
	public boolean delete(MinioStagedBlob staged) throws IOException {
		return false;
	}

	@Override
	public boolean delete(MailboxBlob mblob) throws IOException {
		return false;
	}

	private String getMinIOKey(Mailbox mbox, int itemId, int revision, String locator) {
		return mbox.getAccountId() + "/" + itemId + "/" + revision;
	}
	@Override
	public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator,
			boolean validate) throws ServiceException {
		final MinioBlob fromMinIo;
		try {
			fromMinIo = getFromMinIo(getMinIOKey(mbox, itemId, revision, locator));
		} catch (IOException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
		return new MinIOMailboxBlob(mbox, itemId, revision, locator, fromMinIo);
	}

	@Override
	public InputStream getContent(MailboxBlob mboxBlob) throws IOException {
		final String minIOKey = getMinIOKey(mboxBlob.getMailbox(), mboxBlob.getItemId(),
				mboxBlob.getRevision(), mboxBlob.getLocator());
		try {
			return getFromMinIo(minIOKey).getInputStream();
		} catch (ServiceException e) {
			throw new IOException(e.getMessage(), e);
		}
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
		private byte[] data;
		private int size;

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		@Override
		public byte[] getContent() throws IOException {
			return this.data;
		}

		public MinioBlob(String key, byte[] data, int size) {
			super(key);
			this.key = key;
			this.data = data;
			this.size = size;
		}
		public MinioBlob(String key) {
			super(key);
			this.key = key;
		}

		@Override
		public long getRawSize() throws IOException {
			return size;
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

		private final MinioBlob minioBlob;

		public MinioStagedBlob(Mailbox mailbox, MinioBlob minioBlob) {
			super(mailbox, "", minioBlob.size);
			this.minioBlob = minioBlob;
		}

		public MinioBlob getMinIoBlob() {
			return minioBlob;
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
