package com.zimbra.cs.store.minio;

import com.zimbra.cs.store.Blob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MinioBlob extends Blob {

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

	public long getSize() {
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
