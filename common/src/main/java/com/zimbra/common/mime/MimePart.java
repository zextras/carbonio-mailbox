// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataSource;

import com.zimbra.common.util.ByteUtil;

public abstract class MimePart implements Cloneable {
    /** The property specifying the default charset to use for both parsing
     *  and encoding 8-bit headers and message content. */
    public static final String PROP_CHARSET_DEFAULT = "charset.default";

    public interface InputStreamSource {
        public InputStream newStream(long start, long end);
        public long getSize();
    }

    enum Dirty {
        NONE, HEADERS, CTE, CONTENT;

        Dirty combine(Dirty other) {
            if (other == CONTENT || this == CONTENT) {
                return CONTENT;
            } else if (other == CTE || this == CTE) {
                return CTE;
            } else if (other == HEADERS || this == HEADERS) {
                return HEADERS;
            } else {
                return NONE;
            }
        }
    }

    private Dirty mDirty;
    private MimePart mParent;
    private MimeHeaderBlock mMimeHeaders;
    private ContentType mContentType;
    private long mStartOffset = -1, mBodyOffset = -1, mEndOffset = -1;
    private long mSize = -1;
    private int mLineCount = -1;
    private PartSource mPartSource;

    MimePart(ContentType ctype) {
        mDirty = Dirty.CONTENT;
        setMimeHeader("Content-Type", new ContentType(ctype));
    }

    MimePart(ContentType ctype, MimePart parent, long start, long body, MimeHeaderBlock headers) {
        mDirty       = Dirty.NONE;
        mParent      = parent;
        mContentType = ctype;
        mMimeHeaders = headers == null ? null : headers.setParent(this);
        mStartOffset = start;
        mBodyOffset  = body;
    }

    MimePart(MimePart mp) {
        mDirty       = mp.mDirty;
        mMimeHeaders = mp.mMimeHeaders == null ? null : new MimeHeaderBlock(mp.mMimeHeaders).setParent(this);
        mContentType = new ContentType(mp.mContentType);
        mStartOffset = mp.mStartOffset;
        mBodyOffset  = mp.mBodyOffset;
        mEndOffset   = mp.mEndOffset;
        mSize        = mp.mSize;
        mLineCount   = mp.mLineCount;
        mPartSource  = mp.mPartSource;
    }


    @Override protected abstract MimePart clone();

    MimePart getParent() {
        return mParent;
    }

    MimePart setParent(MimePart mp) {
        if (mParent != mp) {
            detach();
            mParent = mp;
        }
        return this;
    }

    public MimePart detach() {
        mPartSource = getPartSource();
        if (mParent != null) {
            mParent.removeChild(this);
        }
        return this;
    }

    abstract void removeChild(MimePart mp);

    /** Fetches a subpart of this part, specified via the IMAP part naming
     *  convention (see {@link http://tools.ietf.org/html/rfc3501#page-56}).
     *  If the part name is invalid or the requested part does not exist,
     *  returns <tt>null</tt>. */
    public MimePart getSubpart(String part) {
        return part == null || part.equals("") ? this : null;
    }

    Map<String, MimePart> listMimeParts(Map<String, MimePart> parts, String parentName) {
        return parts;
    }

    Properties getProperties() {
        return mParent == null ? null : mParent.getProperties();
    }

    public String getDefaultCharset() {
        Properties props = getProperties();
        return props == null ? null : props.getProperty(PROP_CHARSET_DEFAULT);
    }


    /** Returns the decoded value of the last MIME header matching the given
     *  {@code name}, or {@code null} if none match.  If {@code name} is
     *  {@code null}, returns the value of the last header in the part's
     *  header block. */
    public String getMimeHeader(String name) {
        return mMimeHeaders == null ? null : mMimeHeaders.getValue(name, getDefaultCharset());
    }

    /** Returns the value of the last MIME header matching the given {@code
     *  name}, or {@code null} if none match.  No decoding is performed other
     *  than removing the trailing CRLF.  If {@code name} is {@code null},
     *  returns the value of the last header in the part's header block. */
    public String getEncodedMimeHeader(String name) {
        return mMimeHeaders == null ? null : mMimeHeaders.getEncodedValue(name, getDefaultCharset());
    }

    /** Returns the raw (byte array) content of the last MIME header matching
     *  the given {@code name}, or {@code null} if none match.  If {@code name}
     *  is {@code null}, returns the last header in the part's header block. */
    public byte[] getRawMimeHeader(String name) {
        return mMimeHeaders == null ? null : mMimeHeaders.getRawHeader(name);
    }

    public MimePart setMimeHeader(String name, String value) {
        return setMimeHeader(name, value, null);
    }

    public MimePart setMimeHeader(String name, String value, String charset) {
        return setMimeHeader(name, value == null ? null : new MimeHeader(name, value, charset));
    }

    public MimePart setMimeHeader(String name, MimeHeader header) {
        getMimeHeaderBlock().setHeader(name, header);
        return this;
    }

    public MimePart addMimeHeader(MimeHeader header) {
        getMimeHeaderBlock().addHeader(header);
        return this;
    }

    public MimeHeaderBlock getMimeHeaderBlock() {
        if (mMimeHeaders == null) {
            mMimeHeaders = new MimeHeaderBlock(false, this);
        }
        return mMimeHeaders;
    }

    /** Returns the effective Content-Type for this part.  Note that this should
     *  <u>never</u> be <tt>null</tt>; in the event that a <tt>Content-Type</tt>
     *  header was not specified for the part, a {@link ContentType} object will
     *  be returned representing the effective Content-Type for the part. */
    public ContentType getContentType() {
        return new ContentType(mContentType);
    }

    public MimePart setContentType(ContentType ctype) {
        setMimeHeader("Content-Type", ctype);
        return this;
    }

    ContentType updateContentType(ContentType ctype) {
        return mContentType = ctype;
    }

    public ContentDisposition getContentDisposition() {
        return new ContentDisposition(getEncodedMimeHeader("Content-Disposition"));
    }

    public String getFilename() {
        String filename = getContentDisposition().getParameter("filename");
        if (filename == null || filename.isEmpty()) {
            filename = mContentType.getParameter("name");
        }
        return filename;
    }

    public MimePart setFilename(String name) {
        String filename = getFilename();
        if (filename != null || name != null) {
            setContentType(getContentType().setParameter("name", name));
            setMimeHeader("Content-Disposition", getContentDisposition().setParameter("filename", name));
        }
        return this;
    }

    @Override
    public String toString() {
        return mContentType == null ? null : mContentType.getContentType();
    }


    protected MimePart recordEndpoint(long position, int lineCount) {
        mEndOffset = position;
        mSize      = position - mBodyOffset;
        mLineCount = lineCount;
        mDirty     = Dirty.NONE;
        return this;
    }

    protected long recordSize(long size) {
        mSize = size;
        return size;
    }

    long getStartOffset() {
        return mDirty == Dirty.NONE ? mStartOffset : -1;
    }

    long getBodyOffset() {
        return mDirty != Dirty.CONTENT ? mBodyOffset : -1;
    }

    long getEndOffset() {
        return mDirty != Dirty.CONTENT ? mEndOffset : -1;
    }

    @SuppressWarnings("unused")
    public long getSize() throws IOException {
        return mSize;
    }

    public int getLineCount() {
        return mLineCount;
    }

    private PartSource getPartSource() {
        return mPartSource != null || mParent == null ? mPartSource : mParent.getPartSource();
    }

    /** Returns an {@code InputStream} whose content is the <u>entire</u>
     *  part, MIME headers and all.  If you only want the part body, try
     *  {@link #getContentStream()}. */
    public InputStream getInputStream() throws IOException {
        if (!isDirty() && getStartOffset() != -1) {
            return getRawContentStream(getStartOffset(), getEndOffset());
        } else {
            byte[] header = mMimeHeaders != null ? mMimeHeaders.toByteArray() : new byte[] { '\r', '\n' };
            return new VectorInputStream(header, getRawContentStream());
        }
    }

    /** Returns an {@code InputStream} whose content is the raw, undecoded
     *  body of the part.  If you want the body with the content transfer
     *  encoding removed, try {@link #getContentStream()}. */
    public InputStream getRawContentStream() throws IOException {
        return getRawContentStream(getBodyOffset(), getEndOffset());
    }

    private InputStream getRawContentStream(long start, long end) throws IOException {
        PartSource source = getPartSource();
        return source == null ? null : source.getContentStream(start, end);
    }

    /** Returns a {@code byte[]} array whose content is the raw, undecoded
     *  body of the part.  If you want the body with the content transfer
     *  encoding removed, try {@link #getContent()}. */
    public byte[] getRawContent() throws IOException {
        if (!isDirty()) {
            return getPartSource().getContent(getBodyOffset(), getEndOffset());
        } else {
            InputStream is = getRawContentStream();
            int length = getEndOffset() == -1 ? -1 : (int) (getEndOffset() - getBodyOffset());
            return is == null ? null : ByteUtil.getContent(is, length);
        }
    }

    /** Returns an {@code InputStream} whose content is the body of the part
     *  after decoding the content transfer encoding (if any).  If you want the
     *  raw part body with encoding intact, try {@link #getRawContentStream()}. */
    public InputStream getContentStream() throws IOException {
        return getRawContentStream();
    }

    /** Returns a {@code byte[]} array whose content is the body of the part
     *  after any content transfer has been decoded.  If you want the raw
     *  part body with encoding intact, try {@link #getRawContent()}. */
    public byte[] getContent() throws IOException {
        return getRawContent();
    }


    MimePart attachSource(PartSource psource) {
        if (mBodyOffset != -1) {
            mPartSource = psource;
        }
        return this;
    }

    MimePart setContent(PartSource psource) {
        // switch to "headers dirty" and propagate upwards, regardless of previous dirty state
        mDirty = Dirty.NONE;
        markDirty(Dirty.HEADERS);

        mPartSource  = psource;
        mStartOffset = -1;
        mBodyOffset  = psource == null ? -1 : 0;
        mEndOffset   = psource == null ? -1 : psource.getLength();
        mSize        = mEndOffset;
        return this;
    }

    /** Marks the item as "dirty" so that we regenerate the part when
     *  serializing. */
    void markDirty(Dirty dirty) {
        if (dirty != Dirty.NONE) {
            mDirty = mDirty.combine(dirty);
            // changing anything in the part effectively changes the body of the parent
            if (mParent != null) {
                mParent.markDirty(Dirty.CONTENT);
            }
            if (dirty == Dirty.CONTENT || dirty == Dirty.CTE) {
                // don't reset the offsets to -1, as recordEndpoint clears dirty state
                mSize = mLineCount = -1;
            }
        }
    }

    boolean isDirty() {
        return mDirty != Dirty.NONE || getPartSource() == null;
    }


    static class PartSource {
        private final byte[] mBodyContent;
        private final File mBodyFile;
        private final DataSource mBodySource;
        private final InputStreamSource mBodyStream;
        private final long mLength;

        PartSource(byte[] content) {
            mBodyContent = content;
            mBodyFile    = null;
            mBodySource  = null;
            mBodyStream  = null;
            mLength      = mBodyContent.length;
        }

        PartSource(File file) {
            mBodyContent = null;
            mBodyFile    = file;
            mBodySource  = null;
            mBodyStream  = null;
            mLength      = mBodyFile.length();
        }

        PartSource(DataSource ds) {
            mBodyContent = null;
            mBodyFile    = null;
            mBodySource  = ds;
            mBodyStream  = null;
            mLength      = -1;
        }

        PartSource(InputStreamSource iss) {
            mBodyContent = null;
            mBodyFile    = null;
            mBodySource  = null;
            mBodyStream  = iss;
            mLength      = iss.getSize();
        }

        long getLength() {
            return mLength;
        }

        InputStream getContentStream(long start, long end) throws IOException {
            long sbound = mLength == -1 ? Long.MAX_VALUE : mLength;
            long sstart = Math.max(0, Math.min(start, sbound));
            long send = end < 0 ? mLength : Math.max(sstart, Math.min(end, sbound));

            if (sstart == send) {
                return new ByteArrayInputStream(new byte[0]);
            } else if (mBodyContent != null) {
                return new ByteArrayInputStream(mBodyContent, (int) sstart, (int) (send - sstart));
            } else if (mBodyFile != null || mBodySource != null) {
                InputStream is = mBodyFile != null ? new FileInputStream(mBodyFile) : mBodySource.getInputStream();
                try {
                    if (send == -1 || send == mLength) {
                        is.skip(sstart);
                        return is;
                    } else {
                        return ByteUtil.SegmentInputStream.create(is, sstart, send);
                    }
                } catch (IOException ioe) {
                    ByteUtil.closeStream(is);
                    throw ioe;
                }
            } else if (mBodyStream != null) {
                return mBodyStream.newStream(sstart, send);
            } else {
                return null;
            }
        }

        byte[] getContent(long start, long end) throws IOException {
            long sstart = Math.max(0, Math.min(start, mLength));
            long send = end < 0 ? mLength : Math.max(sstart, Math.min(end, mLength));

            int size = (int) (send - sstart);
            if (mBodyContent != null && size == mBodyContent.length) {
                return mBodyContent;
            }
            byte[] content = new byte[size];

            if (size == 0) {
                return content;
            } else if (mBodyContent != null) {
                System.arraycopy(mBodyContent, (int) sstart, content, 0, size);
                return content;
            } else if (mBodyFile != null) {
                try {
                    RandomAccessFile raf = new RandomAccessFile(mBodyFile, "r");
                    raf.seek(sstart);
                    raf.readFully(content);
                    return content;
                } catch (FileNotFoundException fnfe) {
                    return null;
                }
            } else if (mBodySource != null || mBodyStream != null) {
                InputStream is = getContentStream(sstart, send);
                try {
                    int remaining = size;
                    while (remaining > 0) {
                        int read = is.read(content, size - remaining, remaining);
                        if (read < 0) {
                            break;
                        }
                        remaining -= read;
                    }
                    return content;
                } finally {
                    ByteUtil.closeStream(is);
                }
            } else {
                return null;
            }
        }
    }


    public static class VectorInputStream extends InputStream {
        private final List<Object> mItems;
        private int mNextIndex;
        private InputStream mCurrentStream;

        public VectorInputStream(List<? extends Object> items) throws IOException {
            mItems = new ArrayList<Object>(items);
            while (mItems.remove(null))
                ;
            getNextStream();
        }

        public VectorInputStream(Object... items) throws IOException {
            mItems = new ArrayList<Object>(items.length);
            for (Object item : items) {
                if (item != null) {
                    mItems.add(item);
                }
            }
            getNextStream();
        }

        @Override public int read() throws IOException {
            int c = mCurrentStream == null ? -1 : mCurrentStream.read();
            while (c == -1 && mCurrentStream != null) {
                c = getNextStream() == null ? -1 : mCurrentStream.read();
            }
            return c;
        }

        @Override public int read(byte[] b, int off, int len) throws IOException {
            int num = mCurrentStream == null ? -1 : mCurrentStream.read(b, off, len);
            while (num == -1 && mCurrentStream != null) {
                num = getNextStream() == null ? -1 : mCurrentStream.read(b, off, len);
            }
            return num;
        }

        @Override public long skip(long n) throws IOException {
            long remaining = n - (mCurrentStream == null ? 0 : mCurrentStream.skip(n));
            while (remaining > 0 && mCurrentStream != null) {
                remaining -= getNextStream() == null ? 0 : mCurrentStream.skip(remaining);
            }
            return n - remaining;
        }

        @Override public void close() {
            InputStream current = mCurrentStream;
            mCurrentStream = null;
            ByteUtil.closeStream(current);

            while (mNextIndex < mItems.size()) {
                Object next = mItems.get(mNextIndex++);
                if (next instanceof InputStream) {
                    ByteUtil.closeStream((InputStream) next);
                }
            }
        }

        private InputStream getNextStream() throws IOException {
            ByteUtil.closeStream(mCurrentStream);
            Object next = mNextIndex >= mItems.size() ? null : mItems.get(mNextIndex);
            if (next == null) {
                mCurrentStream = null;
            } else if (next instanceof byte[]) {
                mCurrentStream = new ByteArrayInputStream((byte[]) next);
            } else if (next instanceof InputStream) {
                mCurrentStream = (InputStream) next;
            } else if (next instanceof MimePart) {
                mCurrentStream = ((MimePart) next).getInputStream();
            } else {
                mCurrentStream = new ByteArrayInputStream(next.toString().getBytes());
            }
            mNextIndex++;
            return mCurrentStream;
        }
    }
}
