// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import com.zimbra.common.service.ServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents incoming blob, i.e. blob that may not be yet completely received. Note that
 * IncomingBlob is not thread-safe; two threads MUST NOT write to the same IncomingBlob
 * concurrently.
 */
public abstract class IncomingBlob {
  /**
   * Returns the incoming blob id.
   *
   * @return the id
   */
  public abstract String getId();

  /**
   * Returns the current size of the incoming blob.
   *
   * @return the size
   * @throws ServiceException
   * @throws IOException
   */
  public abstract long getCurrentSize() throws IOException, ServiceException;

  /**
   * Allows to check if expected size has been set.
   *
   * @return True if expected size was set, false otherwise.
   */
  public abstract boolean hasExpectedSize();

  /**
   * Gets the expected size, if set.
   *
   * @pre hasExpectedSize() returned true
   * @return the expected size
   */
  public abstract long getExpectedSize();

  /**
   * Sets the expected size.
   *
   * @pre Must have not been set yet
   * @param value The expected size.
   */
  public abstract void setExpectedSize(long value);

  /**
   * Gets the output stream for the incoming blob. The stream is used to write to the end of the
   * incoming blob.
   *
   * <p>Note that neither IncomingBlob nor the returned OutputStream is thread-safe.
   *
   * @return the output stream
   */
  public abstract OutputStream getAppendingOutputStream() throws IOException;

  /**
   * Gets the input stream. The return stream can be used to read the already written data.
   *
   * <p>The stream must not return data that was written to the incoming blob after the InputStream
   * instance was obtained via this call. EOF should be reported upon attempt to read byte past the
   * current size of the blob at the time of this call.
   *
   * @return the input stream
   */
  public abstract InputStream getInputStream() throws IOException;

  /**
   * Returns the user settable context data. Must not be used or otherwise interpreted by the
   * implementations.
   *
   * <p>Future note: should instances of IncomingBlob be serialized, the Object stored here must be
   * serializable.
   *
   * @return the context
   */
  public abstract Object getContext();

  /**
   * Returns the previously set user context data.
   *
   * @param value the new context
   */
  public abstract void setContext(Object value);

  /**
   * Checks if the incoming blob is complete, i.e. the current size matches expected size
   *
   * @return True if complete, false otherwise
   * @throws ServiceException
   * @throws IOException
   */
  public boolean isComplete() throws IOException, ServiceException {
    return hasExpectedSize() && getExpectedSize() == getCurrentSize();
  }

  /**
   * Finalize the data and create a local Blob object. No further writes are permitted after calling
   * getBlob()
   *
   * @return Blob which holds the local data
   * @throws IOException
   * @throws ServiceException
   */
  public abstract Blob getBlob() throws IOException, ServiceException;

  /** Cancel an upload */
  public abstract void cancel();

  /**
   * @return time (in ms) of last access
   */
  public abstract long getLastAccessTime();
}
