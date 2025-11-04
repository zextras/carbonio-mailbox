package com.zimbra.cs.store.storages;

import com.zextras.filestore.model.MailboxItemIdentifier;
import com.zextras.storages.api.StoragesClient;
import com.zimbra.common.service.ServiceException;
import java.io.IOException;
import java.io.InputStream;

public class StoragesClientAdapter {
		private final StoragesClient storagesClient;

	public StoragesClientAdapter(StoragesClient storagesClient) {
		this.storagesClient = storagesClient;
	}

	public StoragesBlob upload(InputStream data, long actualSize, StorageKey key)
			throws IOException, ServiceException {
		try {
			final MailboxItemIdentifier mailboxItemIdentifier = fromKey(key);
			storagesClient.uploadPut(mailboxItemIdentifier, data, actualSize);
			return get(key);
		} catch (Exception e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	private MailboxItemIdentifier fromKey(StorageKey key) {
		return new MailboxItemIdentifier(key.itemId(),
				key.revision(), key.accountId());
	}

	public StoragesBlob get(StorageKey key)
			throws IOException, ServiceException {
		try {
			final MailboxItemIdentifier mailboxItemIdentifier = fromKey(key);
			final InputStream download = storagesClient.download(mailboxItemIdentifier);
			final byte[] bytes = download.readAllBytes();
			return new StoragesBlob(key, bytes, bytes.length);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public boolean delete(StorageKey key) throws ServiceException {
		try {
			final MailboxItemIdentifier mailboxItemIdentifier = fromKey(key);
			storagesClient.delete(mailboxItemIdentifier);
			return true;
		} catch (Exception e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	public record StorageKey(String accountId, int revision, int itemId, String locator){

		public String toPath() {
			return accountId + "/" + revision + "/" + itemId;
		}
	}
}
