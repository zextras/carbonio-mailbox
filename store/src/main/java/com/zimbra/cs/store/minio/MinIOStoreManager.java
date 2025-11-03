package com.zimbra.cs.store.minio;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.StoreManager;
import io.minio.MinioClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MinIOStoreManager extends StoreManager<MinioBlob, MinioStagedBlob> {

	private final MinIOBlobClientAdapter minIOBlobAdapter;
	private final String bucketName;

	MinIOStoreManager(MinioClient minioClient, String bucketName) {
		this.minIOBlobAdapter = new MinIOBlobClientAdapter(minioClient, bucketName);
		this.bucketName = bucketName;
	}

	public MinIOStoreManager() {
		var minioClient = MinioClient.builder()
				.endpoint(LC.minio_store_url.value())
				.credentials(LC.minio_store_user.value(), LC.minio_store_password.value())
				.build();
		this.bucketName = "mailbox-blobs";
		this.minIOBlobAdapter = new MinIOBlobClientAdapter(minioClient, this.bucketName);
	}

	@Override
	public void startup() throws IOException, ServiceException {
	}

	@Override
	public void shutdown() {
		try {
			minIOBlobAdapter.close();
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
		return minIOBlobAdapter.uploadOnMinIo(data, unknownSize, key);
	}

	@Override
	public MinioStagedBlob stage(InputStream data, long actualSize, Mailbox mbox)
			throws IOException, ServiceException {
		return stageOnMinIO(mbox, data, actualSize);
	}

	private MinioStagedBlob stageOnMinIO(Mailbox mailbox, InputStream data, long actualSize)
			throws IOException, ServiceException {
		final String pathname = mailbox.getAccountId() + "/" + UUID.randomUUID();
		final MinioBlob minioBlob = minIOBlobAdapter.uploadOnMinIo(data, actualSize, pathname);
		return new MinioStagedBlob(mailbox, minioBlob);
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
		final MinioBlob minioBlob = minIOBlobAdapter.uploadOnMinIo(originalBlob.getInputStream(),
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
		try {
			return minIOBlobAdapter.deleteFromMinIo(blob.getKey());
		} catch (ServiceException e) {
			// TODO: log?
			return false;
		}
	}

	@Override
	public boolean delete(MinioStagedBlob staged) throws IOException {
		try {
			return minIOBlobAdapter.deleteFromMinIo(staged.getMinIoBlob().getKey());
		} catch (ServiceException e) {
			// TODO: log?
			return false;
		}
	}

	@Override
	public boolean delete(MailboxBlob mblob) throws IOException {
		try {
			return minIOBlobAdapter.deleteFromMinIo(getMinIOKey(mblob));
		} catch (ServiceException e) {
			// TODO: log?
			return false;
		}
	}

	private String getMinIOKey(Mailbox mbox, int itemId, int revision, String locator) {
		return mbox.getAccountId() + "/" + itemId + "/" + revision;
	}
	private String getMinIOKey(MailboxBlob mailboxBlob) {
		return getMinIOKey(mailboxBlob.getMailbox(), mailboxBlob.getItemId(),
				mailboxBlob.getRevision(), mailboxBlob.getLocator());
	}

	@Override
	public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator,
			boolean validate) throws ServiceException {
		final MinioBlob fromMinIo;
		try {
			fromMinIo = minIOBlobAdapter.getFromMinIo(getMinIOKey(mbox, itemId, revision, locator));
		} catch (IOException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
		return new MinIOMailboxBlob(mbox, itemId, revision, locator, fromMinIo);
	}

	@Override
	public InputStream getContent(MailboxBlob mboxBlob) throws IOException {
		final String minIOKey = getMinIOKey(mboxBlob);
		try {
			return minIOBlobAdapter.getFromMinIo(minIOKey).getInputStream();
		} catch (ServiceException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public InputStream getContent(MinioBlob blob) throws IOException {
		return blob.getInputStream();
	}

	@Override
	public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlobInfo> blobs)
			throws IOException, ServiceException {
		return false;
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
