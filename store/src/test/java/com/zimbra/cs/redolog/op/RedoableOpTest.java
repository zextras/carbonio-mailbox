// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.MailboxSetupHelper;
import com.zextras.mailbox.util.MailboxTestData;
import com.zextras.mailbox.util.MailboxTestExtension;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogManager;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.redolog.TransactionId;
import java.io.InputStream;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RedoableOpTest extends EasyMockSupport {

	private static final String DEFAULT_DOMAIN_NAME = "test.com";
	private static final String DEFAULT_DOMAIN_ID = "f4806430-b434-4e93-9357-a02d9dd796b8";
	private static final String SERVER_NAME = "localhost";
	private static final MailboxTestData mailboxTestData = new MailboxTestData(SERVER_NAME,
			DEFAULT_DOMAIN_NAME,
			DEFAULT_DOMAIN_ID);

	@RegisterExtension
	private static final MailboxTestExtension mailboxTestExtension = new MailboxTestExtension(
			mailboxTestData,
			MailboxSetupHelper.create());

	private RedoLogManager mgr;
	private RedoableOp op;

	@BeforeEach
	public void setUp() {
		mgr = createStrictMock(RedoLogManager.class);
		op = createMockBuilder(RedoableOp.class)
				.withConstructor(MailboxOperation.CopyItem, mgr)
				.addMockedMethod("toString")
				.createMock();
	}

	@Test
	void constructor() {
		assertNull(op.getTransactionId());
		assertEquals(RedoableOp.UNKNOWN_ID, op.getMailboxId());
		assertTrue(op.isStartMarker());
		assertFalse(op.isEndMarker());
		assertFalse(op.isDeleteOp());
	}

	@Test
	void startLogCommit() {
		final TransactionId txnId = new TransactionId(1, 2);
		EasyMock.expect(mgr.getNewTxnId()).andReturn(txnId);
		mgr.log(op, false);
		mgr.commit(op);
		replayAll();

		op.start(7);
		assertEquals(txnId, op.getTransactionId());
		assertEquals(7, op.getTimestamp());
		op.log(false);
		op.setSerializedByteArray(new byte[3]);
		op.commit();
		assertNull(op.mSerializedByteArrayVector,
				"Commit clears byte array.");
		verifyAll();
	}

	@Test
	void inactiveOp() {
		replayAll();
		op.commit();
		op.abort();
		// no calls to mock expected, op is not active until log()
		verifyAll();
	}

	@Test
	void serialize() throws Exception {
		op = createMockBuilder(RedoableOp.class)
				.withConstructor(MailboxOperation.CopyItem, mgr)
				.addMockedMethod("getTransactionId")
				.createMock();

		EasyMock.expect(op.getTransactionId())
				.andReturn(new TransactionId(1, 2));
		op.serializeData(EasyMock.anyObject(RedoLogOutput.class));
		replayAll();
		InputStream out = op.getInputStream();
		assertNotNull(op.mSerializedByteArrayVector,
				"getInputStream sets up internal vector.");
		assertEquals(46, out.available(), "available bytes != 46");
		byte[] bytes = new byte[RedoableOp.REDO_MAGIC.length()];
		out.read(bytes);
		assertEquals(RedoableOp.REDO_MAGIC, new String(bytes), "REDO_MAGIC missing in serialize.");
		verifyAll();
	}

	@Test
	void chainedCommit() {
		mgr.log(op, false);
		mgr.commit(op);

		RedoableOp subOp = createMock(RedoableOp.class);
		subOp.commit();
		replayAll();
		op.addChainedOp(subOp);

		op.log(false);
		op.commit();
		verifyAll();
	}

	@Test
	void chainedAbort() {
		mgr.log(op, false);
		mgr.abort(op);

		RedoableOp subOp = createMock(RedoableOp.class);
		subOp.abort();
		replayAll();
		op.addChainedOp(subOp);

		op.log(false);
		op.abort();
		verifyAll();
	}

	@Test
	void checkSubclasses() throws Exception {
		assertTrue(RedoableOp.checkSubclasses(),
				"Some RedoableOp subclasses are incomplete.  "
						+ "Hint: Make sure the subclass defines a default"
						+ " constructor.");
	}
}
