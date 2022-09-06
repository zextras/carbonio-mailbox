// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.redolog.op.CommitTxn;

/**
 * CommitId consists of redolog sequence number and TransactionId of a redo transaction. It helps
 * locate an exact point in redo history, by going to the redolog of the given sequence and scanning
 * its content until finding the commit record (not the log record) matching the TransactionId.
 */
public class CommitId {

  private long mRedoSeq; // sequence of redo log at transaction commit
  private long mTxnTstamp; // timestamp that goes with mTxnId
  // this is used to distinguish between multiple
  // commit records with the same txn id, which
  // can happen after replayed ops are logged
  private TransactionId mTxnId;

  private CommitId(long seq, long txnTstamp, TransactionId txnId) {
    mRedoSeq = seq;
    mTxnTstamp = txnTstamp;
    mTxnId = txnId;
  }

  public CommitId(long seq, CommitTxn txn) {
    this(seq, txn.getTimestamp(), txn.getTransactionId());
  }

  public boolean matches(CommitTxn txn) {
    return mTxnId.equals(txn.getTransactionId()) && mTxnTstamp == txn.getTimestamp();
  }

  public long getRedoSeq() {
    return mRedoSeq;
  }

  public String encodeToString() {
    int time = mTxnId.getTime();
    int counter = mTxnId.getCounter();
    StringBuilder sb = new StringBuilder();
    sb.append(mRedoSeq).append('-');
    sb.append(mTxnTstamp).append('-');
    sb.append(mTxnId.encodeToString());
    return sb.toString();
  }

  public static CommitId decodeFromString(String str) throws ServiceException {
    Throwable cause = null;
    if (str != null) {
      String[] fields = str.split("-", 3);
      if (fields != null && fields.length == 3) {
        try {
          long seq = Long.parseLong(fields[0]);
          long txnTstamp = Long.parseLong(fields[1]);
          TransactionId txnId = TransactionId.decodeFromString(fields[2]);
          return new CommitId(seq, txnTstamp, txnId);
        } catch (NumberFormatException e) {
          cause = e;
        } catch (ServiceException e) {
          cause = e;
        }
      }
    }
    throw ServiceException.PARSE_ERROR("Invalid CommitId " + str, cause);
  }

  public String toString() {
    return encodeToString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CommitId) {
      CommitId id = (CommitId) o;
      return id.mRedoSeq == mRedoSeq && id.mTxnTstamp == mTxnTstamp && id.mTxnId.equals(mTxnId);
    }
    return false;
  }
}
