package com.zimbra.cs.store.minio;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StagedBlob;

public class MinioStagedBlob extends StagedBlob {

	private final MinioBlob minioBlob;

	public MinioStagedBlob(Mailbox mailbox, MinioBlob minioBlob) {
		super(mailbox, "", minioBlob.getSize());
		this.minioBlob = minioBlob;
	}

	public MinioBlob getMinIoBlob() {
		return minioBlob;
	}

	@Override
	public String getLocator() {
		return "";
	}
}
