// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.redolog.op;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogManager;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.redolog.TransactionId;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;

public class RedoableOpTest extends MailboxTestSuite {

	private RedoLogManager mgr;
	private RedoableOp op;

	@BeforeEach
	public void setUp() {
		mgr = Mockito.mock(RedoLogManager.class, Mockito.withSettings().strictness(Strictness.STRICT_STUBS));

		op = Mockito.mock(RedoableOp.class,
				Mockito.withSettings()
						.useConstructor(MailboxOperation.CopyItem, mgr)
						.defaultAnswer(Mockito.CALLS_REAL_METHODS));

		Mockito.doReturn("mocked-toString").when(op).toString();
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
		Mockito.when(mgr.getNewTxnId()).thenReturn(txnId);
		mgr.log(op, false);
		mgr.commit(op);
		

		op.start(7);
		assertEquals(txnId, op.getTransactionId());
		assertEquals(7, op.getTimestamp());
		op.log(false);
		op.setSerializedByteArray(new byte[3]);
		op.commit();
		assertNull(op.mSerializedByteArrayVector,
				"Commit clears byte array.");
		
	}

	@Test
	void inactiveOp() {
		
		op.commit();
		op.abort();
		// no calls to mock expected, op is not active until log()
		
	}

	@Test
	void serialize() throws Exception {
		op = Mockito.mock(RedoableOp.class,
				Mockito.withSettings()
						.useConstructor(MailboxOperation.CopyItem, mgr)
						.defaultAnswer(Mockito.CALLS_REAL_METHODS));

		Mockito.when(op.getTransactionId())
				.thenReturn(new TransactionId(1, 2));
		op.serializeData(Mockito.any(RedoLogOutput.class));
		
		InputStream out = op.getInputStream();
		assertNotNull(op.mSerializedByteArrayVector,
				"getInputStream sets up internal vector.");
		assertEquals(46, out.available(), "available bytes != 46");
		byte[] bytes = new byte[RedoableOp.REDO_MAGIC.length()];
		out.read(bytes);
		assertEquals(RedoableOp.REDO_MAGIC, new String(bytes), "REDO_MAGIC missing in serialize.");
		
	}

	@Test
	void chainedCommit() {
		mgr.log(op, false);
		mgr.commit(op);

		RedoableOp subOp = Mockito.mock(RedoableOp.class);
		subOp.commit();
		
		op.addChainedOp(subOp);

		op.log(false);
		op.commit();
		
	}

	@Test
	void chainedAbort() {
		mgr.log(op, false);
		mgr.abort(op);

		RedoableOp subOp = Mockito.mock(RedoableOp.class);
		subOp.abort();
		
		op.addChainedOp(subOp);

		op.log(false);
		op.abort();
		
	}

	@Test
	void checkSubclasses() throws Exception {
		assertTrue(RedoableOp.checkSubclasses(),
				"Some RedoableOp subclasses are incomplete.  "
						+ "Hint: Make sure the subclass defines a default"
						+ " constructor.");
	}
}
