package com.zimbra.cs.store.storages;

import com.google.common.base.Strings;
import com.zextras.storages.api.StoragesClient;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.MailboxBlob;
import com.zimbra.cs.store.MailboxBlob.MailboxBlobInfo;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.storages.StoragesClientAdapter.StorageKey;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class StoragesManager extends StoreManager<StoragesBlob, StoragesStagedBlob> {

	private final StoragesClientAdapter storagesClientAdapter;

	StoragesManager(StoragesClientAdapter storagesClientAdapter) {
		this.storagesClientAdapter = storagesClientAdapter;
	}

	public StoragesManager() {
		var storagesClient = StoragesClient.atUrl(LC.storages_service_url.value());
		this.storagesClientAdapter = new StoragesClientAdapter(storagesClient);
	}

	@Override
	public void startup() throws IOException, ServiceException {
	}

	@Override
	public void shutdown() {
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
	public BlobBuilder<StoragesBlob> getBlobBuilder() throws IOException, ServiceException {
		final StorageKey key = incomingStorageKey();
		return new StoragesIncomingBlobBuilder(key);
	}

	private static StorageKey incomingStorageKey() {
		return new StorageKey("incoming", UUID.randomUUID().toString());
	}

	@Override
	public StoragesBlob storeIncoming(InputStream data, boolean storeAsIs)
			throws IOException, ServiceException {
		var key = incomingStorageKey();
		final int unknownSize = -1;
		return storagesClientAdapter.upload(data, unknownSize, key);
	}

	@Override
	public StoragesStagedBlob stage(InputStream data, long actualSize, Mailbox mbox)
			throws IOException, ServiceException {
		return stageOnMinIO(mbox, data, actualSize);
	}

	private StoragesStagedBlob stageOnMinIO(Mailbox mailbox, InputStream data, long actualSize)
			throws IOException, ServiceException {
		final StorageKey pathname = new StorageKey(mailbox.getAccountId(), UUID.randomUUID().toString());
		final StoragesBlob minioBlob = storagesClientAdapter.upload(data, actualSize, pathname);
		return new StoragesStagedBlob(mailbox, minioBlob);
	}



	@Override
	public StoragesStagedBlob stage(StoragesBlob blob, Mailbox mbox) throws IOException, ServiceException {
		final long rawSize = blob.getRawSize();
		return stageOnMinIO(mbox, blob.getInputStream(), rawSize);
	}

	@Override
	public MailboxBlob copy(MailboxBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		final String locator = src.getLocator();
		var minIOKey = getKey(destMbox, destMsgId, destRevision, locator);
		final Blob originalBlob = src.getLocalBlob();
		final long rawSize = originalBlob.getRawSize();
		final StoragesBlob minioBlob = storagesClientAdapter.upload(originalBlob.getInputStream(), rawSize, minIOKey);
		return new StoragesMailboxBlob(destMbox, destMsgId, destRevision, locator, minioBlob);
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
	public MailboxBlob link(StoragesStagedBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		var minIOKey = getKey(destMbox, destMsgId, destRevision, "");
		final StoragesBlob originalBlob = src.getMinIoBlob();
		final StoragesBlob minioBlob = storagesClientAdapter.upload(originalBlob.getInputStream(),
				originalBlob.getRawSize(), minIOKey);
		return new StoragesMailboxBlob(destMbox, destMsgId, destRevision, "", minioBlob);
	}

	@Override
	public MailboxBlob renameTo(StoragesStagedBlob src, Mailbox destMbox, int destMsgId, int destRevision)
			throws IOException, ServiceException {
		return null;
	}

	@Override
	public boolean delete(StoragesBlob blob) throws IOException {
		try {
			return storagesClientAdapter.delete(blob.getKey());
		} catch (ServiceException e) {
			// TODO: log?
			return false;
		}
	}

	@Override
	public boolean delete(StoragesStagedBlob staged) throws IOException {
		try {
			return storagesClientAdapter.delete(staged.getMinIoBlob().getKey());
		} catch (ServiceException e) {
			// TODO: log?
			return false;
		}
	}

	@Override
	public boolean delete(MailboxBlob mblob) throws IOException {
		try {
			return storagesClientAdapter.delete(getKey(mblob));
		} catch (ServiceException e) {
			// TODO: log?
			return false;
		}
	}

	private StorageKey getKey(Mailbox mbox, int itemId, int revision, String locator) {
		if (Strings.isNullOrEmpty(locator)) {
			return new StorageKey(mbox.getAccountId(), itemId + "-" + revision);
		}
		return new StorageKey(mbox.getAccountId(), itemId + "-" + revision + "-" + locator);
	}

	private StorageKey getKey(MailboxBlob mailboxBlob) {
		return getKey(mailboxBlob.getMailbox(), mailboxBlob.getItemId(),
				mailboxBlob.getRevision(), mailboxBlob.getLocator());
	}

	@Override
	public MailboxBlob getMailboxBlob(Mailbox mbox, int itemId, int revision, String locator,
			boolean validate) throws ServiceException {
		final StoragesBlob fromMinIo;
		try {
			fromMinIo = storagesClientAdapter.get(getKey(mbox, itemId, revision, locator));
		} catch (IOException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
		return new StoragesMailboxBlob(mbox, itemId, revision, locator, fromMinIo);
	}

	@Override
	public InputStream getContent(MailboxBlob mboxBlob) throws IOException {
		var minIOKey = getKey(mboxBlob);
		try {
			return storagesClientAdapter.get(minIOKey).getInputStream();
		} catch (ServiceException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public InputStream getContent(StoragesBlob blob) throws IOException {
		return blob.getInputStream();
	}

	@Override
	public boolean deleteStore(Mailbox mbox, Iterable<MailboxBlobInfo> blobs)
			throws IOException, ServiceException {
		blobs.forEach(
				blob -> {
					var blobKey = getKey(mbox, blob.itemId, blob.revision, blob.locator);
					try {
						storagesClientAdapter.delete(blobKey);
					} catch (ServiceException e) {
						// TODO: log
					}
				}
		);

		return true;
	}

}
