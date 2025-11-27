package com.zimbra.cs.service.mail;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Folder.FolderOptions;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CalendarGroupCodecTest extends MailboxTestSuite {


	@Test
	void shouldDecodeCalendarGroupWithoutCalendarIds() throws Exception {
		var account = createAccount().create();
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
		final FolderOptions fopt = new FolderOptions();
		final Folder folder = mailbox.createFolder(null, "/myFolder", fopt);
		Assertions.assertThrows(Exception.class, () ->  CalendarGroupCodec.decodeCalendarIds(folder));
	}
}