// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 7. 22.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.redolog.logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import com.zimbra.cs.redolog.op.RedoableOp;

/**
 * @author jhahm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface LogWriter {

	/**
	 * Opens the log.
	 * @throws IOException
	 */
  void open() throws IOException;

	/**
	 * Closes the log.
	 * @throws IOException
	 */
  void close() throws IOException;

	/**
	 * Logs an entry.
     * @param op entry being logged
     * @param data the data stream; must not be null;
     *             while it is possible to compute data from op, only what
     *             is passed in as data gets logged
	 * @param synchronous if true, method doesn't return until log entry
	 *                    has been written to disk safely, or has been
	 *                    securely stored in an equivalent manner depending
	 *                    on the logger implementation
	 * @throws IOException
	 */
  void log(RedoableOp op, InputStream data, boolean synchronous) throws IOException;
    
    /**
     * Make sure all writes are committed to disk, or whatever the log
     * destination medium is.  This is mainly useful only when we need to
     * make sure the commit record is on disk, because fsync of commit record
     * is deferred until the logging of the next redo record for performance
     * reasons.
     * @throws IOException
     */
    void flush() throws IOException;

	/**
	 * Returns the current size of the log.  Used for rollover tracking.
	 * @return
	 */
  long getSize();

	/**
	 * Returns the time of the log creation.
	 * @return
	 */
  long getCreateTime();

	/**
     * Returns the time of the last entry logged.
     * @return
     */
  long getLastLogTime();

	/**
	 * Whether the current log is empty, i.e. has no entries logged.
	 * @return
	 * @throws IOException
	 */
  boolean isEmpty() throws IOException;

	/**
	 * Whether the underlying logfile exists.
	 * @return
	 */
  boolean exists();

	/**
	 * Returns the absolute pathname for the underlying logfile.
	 * @return
	 */
  String getAbsolutePath();

	/**
	 * Renames the underlying logfile.
	 * @param dest
	 * @return true if and only if the renaming succeeded; false otherwise
	 */
  boolean renameTo(File dest);

	/**
	 * Deletes the underlying logfile.  The logger should be closed first
	 * if open.
	 * @return true if and only if the deletion succeeded; false otherwise
	 */
  boolean delete();

    /**
     * Performs log rollover.
     * @param activeOps map of pending transactions; these should be logged
     *                  at the beginning of new log file
     * @return java.io.File object for rolled over logfile
     * @throws IOException
     */
    File rollover(LinkedHashMap /*<TxnId, RedoableOp>*/ activeOps)
    throws IOException;

    /**
     * Returns the sequence number of redolog.  Only file-based log writers
     * will return a meaningful number.  Others return 0.
     * @return
     * @throws IOException
     */
    long getSequence();
}
