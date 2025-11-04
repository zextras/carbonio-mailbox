package com.zimbra.cs.store.minio;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.BlobBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

// TODO: double check build logic, it is used in few places
public class MinIOIncomingBlobBuilder implements BlobBuilder<MinioBlob> {

	private byte[] dataToSet;
	private final String key;
	private int size;
	private boolean isFinished = false;

	public MinIOIncomingBlobBuilder(String key) {
		this.key = key;
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
		if (Objects.isNull(dataToSet)) {
			return 0;
		}
		return this.dataToSet.length;
	}

	@Override
	public BlobBuilder<MinioBlob> disableCompression(boolean disable) {
		return this;
	}

	@Override
	public int getCompressionThreshold() {
		// TODO: do we support compression on minio?
		return 0;
	}

	@Override
	public BlobBuilder<MinioBlob> disableDigest(boolean disable) {
		// TODO: digest?
		return this;
	}

	@Override
	public BlobBuilder<MinioBlob> append(InputStream in) throws IOException {
		this.dataToSet = in.readAllBytes();
		this.size = dataToSet.length;
		return this;
	}

	@Override
	public BlobBuilder<MinioBlob> append(byte[] b) throws IOException {
		this.dataToSet = b;
		this.size = b.length;
		return this;
	}

	@Override
	public BlobBuilder<MinioBlob> append(byte[] b, int off, int len) throws IOException {
		// TODO: append to data or overwrite existing?
		this.dataToSet = b;
		this.size = len;
		return this;
	}

	@Override
	public BlobBuilder<MinioBlob> append(ByteBuffer bb) throws IOException {
		this.dataToSet = bb.array();
		return this;
	}

	@Override
	public MinioBlob finish() throws IOException, ServiceException {
		isFinished = true;
		return new MinioBlob(key, dataToSet, size);
	}

	@Override
	public boolean isFinished() {
		return isFinished;
	}

	@Override
	public void dispose() {
		this.dataToSet = null;
		this.isFinished = false;
		this.size = 0;
	}
}
