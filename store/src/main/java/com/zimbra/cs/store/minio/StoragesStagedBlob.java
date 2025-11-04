package com.zimbra.cs.store.minio;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.StagedBlob;

public class StoragesStagedBlob extends StagedBlob {

	private final StoragesBlob minioBlob;

	public StoragesStagedBlob(Mailbox mailbox, StoragesBlob minioBlob) {
		super(mailbox, "", minioBlob.getSize());
		this.minioBlob = minioBlob;
	}

	public StoragesBlob getMinIoBlob() {
		return minioBlob;
	}

	@Override
	public String getLocator() {
		return "";
	}
}
