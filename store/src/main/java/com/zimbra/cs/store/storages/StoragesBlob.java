package com.zimbra.cs.store.storages;

import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.storages.StoragesClientAdapter.StorageKey;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StoragesBlob extends Blob {

	private final StorageKey key;
	private final byte[] data;
	private final int size;

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(data);
	}

	@Override
	public byte[] getContent() throws IOException {
		return this.data;
	}

	public StoragesBlob(StorageKey key, byte[] data, int size) {
		super(key.path());
		this.key = key;
		this.data = data;
		this.size = size;
	}

	@Override
	public long getRawSize() throws IOException {
		return size;
	}

	public long getSize() {
		return size;
	}

	@Override
	public String getName() {
		return key.path();
	}

	public StorageKey getKey() {
		return key;
	}
}
