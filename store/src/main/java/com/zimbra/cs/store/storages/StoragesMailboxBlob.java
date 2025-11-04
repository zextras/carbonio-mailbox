package com.zimbra.cs.store.storages;

import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.MailboxBlob;
import java.io.IOException;

public class StoragesMailboxBlob extends MailboxBlob {

	private final StoragesBlob blob;

	public StoragesMailboxBlob(Mailbox mbox, int itemId, int revision, String locator, StoragesBlob blob) {
		super(mbox, itemId, revision, locator);
		// TODO: maybe use the client to retrieve data?
		this.blob = blob;
	}

	@Override
	public Blob getLocalBlob() throws IOException {
		return blob;
	}
}
