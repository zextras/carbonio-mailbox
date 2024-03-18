// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jan 20, 2005
 *
 */
package com.zimbra.cs.convert;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zimbra.common.util.ZimbraLog;

/**
 * Represents an attachment to a message or a piece of content within an archive-type
 * attachment. 
 * 
 * @author kchen
 */
public class AttachmentInfo {
    private InputStream mInstream;
    private String mContentType;
    private String mPart;
    private List<String> mSeqInArchive;
    private String mDigest;
    private String mFilename;
    private long mLength;
    private Charset charset;
    
    public AttachmentInfo(InputStream in, String digest, String ct, String p, String filename, long length, List<String> seq, String cset) {
        mInstream = in;
        mDigest = digest;
        mContentType = ct;
        mPart = p;
        mFilename = filename;
        mLength = length;
        mSeqInArchive = new ArrayList<>(seq.size());
        mSeqInArchive.addAll(seq);
        try {
            charset = Charset.forName(cset);
        } catch (Exception e) {
            charset = Charset.forName("UTF-8");
            ZimbraLog.mailbox.error("Invalid character set - %s", cset);;
        }
    }
    
    public AttachmentInfo(InputStream in, String digest, String ct, String p, String filename, long length, List<String> seq) {
        this(in, digest, ct, p, filename, length, seq, "UTF-8");
    }
    
    public AttachmentInfo(InputStream in, String digest, String ct, String p, String filename, long length, String[] seq) {
        this(in, digest, ct, p, filename, length, Arrays.asList(seq), "UTF-8");
    }
    
    public AttachmentInfo(InputStream in, String digest, String ct, String p, String filename, long length, String[] seq, String cset) {
        this(in, digest, ct, p, filename, length, Arrays.asList(seq), cset);
    }
   
    public AttachmentInfo(InputStream in, String digest, String ct, String p, String filename, long length) {
        this(in, digest, ct, p, filename, length, new String[0]);
    }

    
    /**
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * @return Returns the Seq ID of a piece of content in the Archive.
     */
    public List<String> getSeqInArchive() {
        return new ArrayList<>(mSeqInArchive);
    }

    /**
     * @return Returns the Part.
     */
    public String getPart() {
        return mPart;
    }
    
    /**
     * @return Returns the ContentType.
     */
    public String getContentType() {
        return mContentType;
    }
    
    /**
     * @return Returns the Instream.
     */
    public InputStream getInputStream() {
        return mInstream;
    }
    
    /**
     * 
     * @return the digest of the containing message. This is the digest
     * computed over the body of the message plus all its attachments.
     */
    public String getDigest() {
        return mDigest;
    }
    
    /**
     * @return the relative file path to the converted main HTML file.
     * Its general construct is: <msg digest>/<attachment part id>[/<seq id1>/<seq id2>/.../<seq idn>].html
     * where <seq idn> refers to the sequence ID for content piece within an archive type attachment.
     * They are not applicable if the attachment is not archive type.
     */
    public String getFilePath() {
        StringBuffer buf = new StringBuffer();
        buf.append(mDigest).append(File.separator);
        buf.append(mPart);
      for (String s : mSeqInArchive) {
        buf.append(File.separator);
        buf.append(s);
      }
        buf.append(".html");
        return buf.toString();
    }
    
    public String getPartId() {
        return mDigest + "_" + mPart;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("AttachmentInfo: input stream=").append(mInstream.getClass());
        buf.append(", content type=").append(mContentType);
        return buf.toString();
    }

    /**
     * 
     */
    public void resetSequences() {
        mSeqInArchive.clear();
    }

    /**
     * @return
     */
    public String getFilename() {
        return mFilename;
    }
    
    public long getLength() {
        return mLength;
    }
}
