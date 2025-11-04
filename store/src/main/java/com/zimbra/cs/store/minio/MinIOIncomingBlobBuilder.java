package com.zimbra.cs.store.minio;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.BlobBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

// TODO: implement build logic, it is used in few places
public class MinIOIncomingBlobBuilder implements BlobBuilder<MinioBlob> {

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
