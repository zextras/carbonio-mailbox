package com.zimbra.cs.store.minio;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import java.io.IOException;

public class MinIOMailboxBlob extends MailboxBlob {

	private final MinioBlob blob;

	public MinIOMailboxBlob(Mailbox mbox, int itemId, int revision, String locator, MinioBlob blob) {
		super(mbox, itemId, revision, locator);
		// TODO: maybe use the minioclient to retrieve data?
		this.blob = blob;
	}

	@Override
	public Blob getLocalBlob() throws IOException {
		return blob;
	}
}
