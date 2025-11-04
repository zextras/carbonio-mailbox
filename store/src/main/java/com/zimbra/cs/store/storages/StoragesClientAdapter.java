package com.zimbra.cs.store.storages;

import com.zextras.filestore.model.NodeIdentifier;
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
			var identifier = fromKey(key);
			storagesClient.uploadPut(identifier, data, actualSize);
			return get(key);
		} catch (Exception e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	private static NodeIdentifier fromKey(StorageKey key) {
		return new NodeIdentifier(key.path());
	}

	public StoragesBlob get(StorageKey key)
			throws IOException, ServiceException {
		try {
			var identifier = fromKey(key);
			final InputStream download = storagesClient.download(identifier);
			final byte[] bytes = download.readAllBytes();
			return new StoragesBlob(key, bytes, bytes.length);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public boolean delete(StorageKey key) throws ServiceException {
		try {
			var identifier = fromKey(key);
			storagesClient.delete(identifier);
			return true;
		} catch (Exception e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	public record StorageKey(String path){
	}
}
