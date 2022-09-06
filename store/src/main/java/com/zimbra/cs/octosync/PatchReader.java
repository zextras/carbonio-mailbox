// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for reading octopus patches
 *
 * <p>This is to be used by code that processes Octopus patches as produced and uploaded by the
 * client. Interface is provided to abstract out the actual implementation of the patch format.
 *
 * @author grzes
 */
public interface PatchReader extends Closeable {
  /** Indicates the next record on the patch reader "stream" */
  public enum RecordType {
    /** Data record */
    DATA,
    /** Reference record */
    REF
  };

  /** Helper class to return information about records in the patch. */
  public class RecordInfo {
    /** type of record */
    public RecordType type;
    /** length, valid for DATA only */
    public long length;
  }
  ;

  /**
   * Returns the size of the resulting file (i.e file created by applying this patch). Can be used
   * to preallocate output buffer/reserve space in the filesystem.
   *
   * @return Size of the resulting file.
   */
  public long getFileSize();

  /**
   * Checks if the patch has more records.
   *
   * @return True, if there are more records to process, false if end-of-patch
   * @throws IOException Signals that an I/O error occurred
   * @throws BadPatchFormatException Indicates malformed patch format
   */
  public boolean hasMoreRecordInfos() throws IOException, BadPatchFormatException;

  /**
   * Returns the information about the next record in the patch.
   *
   * <p>Called again without a call to PopData() or PopRef() should return the same information.
   *
   * @precondition hasMoreRecordInfos() == true
   * @return Instance of RecordInfo
   * @throws IOException Signals that an I/O error occurred
   * @throws BadPatchFormatException Indicates malformed patch format
   */
  public RecordInfo getNextRecordInfo() throws IOException, BadPatchFormatException;

  /**
   * Pops the data record from the patch stream.
   *
   * @precondition Last to call to GetNextRecordInfo() must have indicated DATA
   * @precondition Must be called only once after a valid call to GetNextRecordInfo()
   * @todo better API that directly writes to a stream
   * @param buffer Pointer to the buffer where the data will be copied to
   * @param bufferSize Size of the buffer; must be no less than the size indicated in RecordInfo.
   * @throws IOException Signals that an I/O error occurred
   */
  public InputStream popData() throws IOException, BadPatchFormatException;

  /**
   * Pops the reference record from the patch stream.
   *
   * @return Instacne of PatchRef
   * @throws IOException Signals that an I/O error occurred
   * @throws BadPatchFormatException Indicates malformed patch format
   * @throws InvalidPatchReferenceException Invalid reference in the patch (e.g. zero length)
   * @precondition Last to call to GetNextRecordInfo() must have indicated REF
   * @precondition Must be called only once after a valid call to GetNextRecordInfo()
   */
  public PatchRef popRef()
      throws IOException, BadPatchFormatException, InvalidPatchReferenceException;

  /**
   * Closes the underlying input stream if applicable.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void close() throws IOException;
}
