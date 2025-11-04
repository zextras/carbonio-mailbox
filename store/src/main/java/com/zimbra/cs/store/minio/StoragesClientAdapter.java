package com.zimbra.cs.store.minio;

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

	public StoragesBlob upload(InputStream data, long actualSize, String key)
			throws IOException, ServiceException {
		try {
			final MailboxItemIdentifier mailboxItemIdentifier = new MailboxItemIdentifier(key);
			storagesClient.uploadPut(mailboxItemIdentifier, data);
			return get(key);
		} catch (Exception e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	public StoragesBlob get(String key)
			throws IOException, ServiceException {
		try {
			final MailboxItemIdentifier mailboxItemIdentifier = new MailboxItemIdentifier(key);
			final InputStream download = storagesClient.download(mailboxItemIdentifier);
			final byte[] bytes = download.readAllBytes();
			return new StoragesBlob(key, bytes, bytes.length);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public boolean delete(String key) throws ServiceException {
		try {
			final MailboxItemIdentifier mailboxItemIdentifier = new MailboxItemIdentifier(key);
			storagesClient.delete(mailboxItemIdentifier);
			return true;
		} catch (Exception e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}
}
