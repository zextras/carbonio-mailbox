package com.zimbra.cs.store.file;

import com.zimbra.common.util.FileUtil;
import com.zimbra.common.zmime.ZSharedFileInputStream;
import com.zimbra.cs.store.Blob;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

public class FileBlob extends Blob {

	private final File file;
	private final String path;
	private Boolean compressed = null;

	protected FileBlob(final File file) {
		super(file.getAbsolutePath());

		this.file = file;
		this.path = file.getAbsolutePath();
	}

	protected FileBlob(File file, long rawSize, String digest) {
		super(file.getAbsolutePath(), rawSize, digest);
		this.file = file;
		this.path = file.getAbsolutePath();
	}

	public long getRawSize() throws IOException {
		if (rawSize == null) {
			if (!isCompressed()) {
				this.rawSize = file.length();
			} else {
				initializeSizeAndDigest();
			}
		}
		return rawSize;
	}

	@Override
	public String getName() {
		return file.getName();
	}


	public File getFile() {
		return file;
	}

	public String getPath() {
		return path;
	}

	public InputStream getInputStream() throws IOException {
		InputStream in = new ZSharedFileInputStream(file);
		if (isCompressed()) {
			in = new GZIPInputStream(in);
		}
		return in;
	}

	@Override
	public byte[] getContent() throws IOException {
		return Files.readAllBytes(file.toPath());
	}


	public boolean isCompressed() throws IOException {
		if (compressed == null) {
			if (rawSize != null && rawSize == file.length()) {
				this.compressed = Boolean.FALSE;
			} else {
				this.compressed = FileUtil.isGzipped(file);
			}
		}
		return compressed;
	}
}
