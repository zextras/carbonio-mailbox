package com.zimbra.cs.service.mail;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Folder.FolderOptions;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CalendarGroupCodecTest extends MailboxTestSuite {


	@Test
	void shouldReturnEmptyCalendarIdsList_IfFolderDoesNotHaveCalendarIds() throws Exception {
		var account = createAccount().create();
		final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
		final Folder folder = mailbox.createFolder(null, "/myFolder", new FolderOptions());
		final List<String> strings = CalendarGroupCodec.decodeCalendarIds(folder);
		Assertions.assertEquals(List.of(), strings);
	}
}