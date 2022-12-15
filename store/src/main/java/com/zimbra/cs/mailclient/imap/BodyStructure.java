// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Formatter;

/**
 * IMAP message BODYSTRUCTURE response.
 */
public class BodyStructure {
    private String type;            // Content type
    private String subtype;         // Content subtype
    private Map<String, String> params; // Content parameters
    private String id;              // Content-ID
    private String description;     // Content-Description
    private String encoding;        // Content-Transfer-Encoding
    private long size = -1;         // Content size in bytes
    private long lines = -1;        // Number of content lines
    private Envelope envelope;      // Optional envelope for MESSAGE/RFC822
    private BodyStructure[] parts;  // Multipart message body parts
    private String md5;             // Content MD5 checksum
    private String disposition;     // Body disposition type
    private Map<String, String> dispositionParams; // Disposition parameters
    private String[] language;      // Body language
    private String location;        // Body content URI

    /**
     * Reads BODYSTRUCTURE IMAP response data.
     * @param is the IMAP stream from which to read the response
     * @param ext if true, then allow extension data (not for BODY)
     * @return the BodyStructure representing the data
     * @throws IOException if an I/O error occurred
     */
    public static BodyStructure read(ImapInputStream is, boolean ext)
            throws IOException {
        // body            = "(" (body-type-1part / body-type-mpart) ")"
        BodyStructure bs = new BodyStructure();
        is.skipSpaces();
        is.skipChar('(');
        is.skipSpaces();
        if (is.peek() == '(') {
            bs.readMPart(is, ext);
        } else {
            bs.read1Part(is, ext);
        }
        is.skipSpaces();
        is.skipChar(')');
        return bs;
    }

    // body-type-1part = (body-type-basic / body-type-msg / body-type-text)
    //                   [SP body-ext-1part]
    // body-type-basic = media-basic SP body-fields
    // body-type-text  = media-text SP body-fields SP body-fld-lines
    // body-type-msg   = media-message SP body-fields SP envelope
    //                   SP body SP body-fld-lines
    // media-basic     = ((DQUOTE ("APPLICATION" / "AUDIO" / "IMAGE" /
    //                   "MESSAGE" / "VIDEO") DQUOTE) / string) SP
    //                   media-subtype
    // media-message   = DQUOTE "MESSAGE" DQUOTE SP DQUOTE "RFC822" DQUOTE
    // media-text      = DQUOTE "TEXT" DQUOTE SP media-subtype
    // media-subtype   = string
    // body-fld-lines  = number
    // body-ext-1part  = body-fld-md5 [SP body-fld-dsp [SP body-fld-lang
    //                   [SP body-fld-loc *(SP body-extension)]]]

    private void read1Part(ImapInputStream is, boolean ext) throws IOException {
        type = is.readString().toUpperCase();
        is.skipChar(' ');
        subtype = is.readString().toUpperCase();
        is.skipChar(' ');
        readFields(is);
        if (type.equals("TEXT")) {
            is.skipChar(' ');
            lines = is.readNumber();
        } else if (type.equals("MESSAGE") && subtype.equals("RFC822")) {
            is.skipChar(' ');
            envelope = Envelope.read(is);
            is.skipChar(' ');
            parts = new BodyStructure[] { BodyStructure.read(is, ext) };
            is.skipChar(' ');
            lines = is.readNumber();
        }
        if (ext && is.match(' ')) {
            md5 = is.readNString();
            if (is.match(' ')) readExt(is);
        }
    }

    // body-type-mpart = 1*body SP media-subtype
    //                   [SP body-ext-mpart]
    // body-ext-mpart  = body-fld-param [SP body-fld-dsp [SP body-fld-lang
    //                   [SP body-fld-loc *(SP body-extension)]]]
    
    private void readMPart(ImapInputStream is, boolean ext) throws IOException {
        type = "MULTIPART";
        List<BodyStructure> parts = new ArrayList<BodyStructure>();
        while (is.peekChar() == '(') {
            parts.add(read(is, ext));
            is.skipSpaces();
        }
        subtype = is.readString().toUpperCase();
        if (ext && is.match(' ')) {
            params = readParams(is);
            if (is.match(' ')) {
                is.skipSpaces();
                readExt(is);
            }
        }
        this.parts = parts.toArray(new BodyStructure[parts.size()]);
    }

    // ext             = body-fld-dsp [SP body-fld-lang
    //                   [SP body-fld-loc *(SP body-extension)]]
    // body-fld-dsp    = "(" string SP body-fld-param ")" / nil
    // body-fld-loc    = nstring
    
    private void readExt(ImapInputStream is) throws IOException {
        is.skipSpaces();
        if (is.match('(')) {
            disposition = is.readString();
            is.skipChar(' ');
            is.skipSpaces();
            dispositionParams = readParams(is);
            is.skipSpaces();
            is.skipChar(')');
        } else {
            is.skipNil();
        }
        if (is.match(' ')) {
            is.skipSpaces();
            language = readLang(is);
            if (is.match(' ')) {
                location = is.readNString();
                while (is.match(' ')) {
                    skipExtData(is);
                }
            }
        }
    }
    
    // body-fields     = body-fld-param SP body-fld-id SP body-fld-desc SP
    //                   body-fld-enc SP body-fld-octets
    // body-fld-id     = nstring
    // body-fld-desc   = nstring
    // body-fld-enc    = (DQUOTE ("7BIT" / "8BIT" / "BINARY" / "BASE64"/
    //                   "QUOTED-PRINTABLE") DQUOTE) / string
    // body-fld-octets = number

    private void readFields(ImapInputStream is) throws IOException {
        params = readParams(is);
        is.skipChar(' ');
        id = is.readNString();
        is.skipChar(' ');
        description = is.readNString();
        is.skipChar(' ');
        encoding = is.readString();
        is.skipChar(' ');
        size = is.readNumber();
    }

    // body-fld-param  = "(" string SP string *(SP string SP string) ")" / nil
    private static Map<String, String> readParams(ImapInputStream is)
        throws IOException {
        is.skipSpaces();
        if (!is.match('(')) {
            is.skipNil();
            return null;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        is.skipSpaces();
        while (!is.match(')')) {
            String name = is.readString().toLowerCase();
            is.skipChar(' ');
            String value = is.readString();
            params.put(name, value);
        }
        return params;
    }

    // body-extension  = nstring / number /
    //                "(" body-extension *(SP body-extension) ")"
    
    private static void skipExtData(ImapInputStream is) throws IOException {
        if (is.match('(')) {
            is.skipSpaces();
            while (!is.match(')')) {
                skipExtData(is);
                is.skipSpaces();
            }
        } else {
            is.readAStringData();
        }
    }

    // body-fld-lang   = nstring / "(" string *(SP string) ")"
    
    private static String[] readLang(ImapInputStream is) throws IOException {
        if (is.peek() != '(') {
            String lang = is.readNString();
            return lang != null ? new String[] { lang } : null;
        }
        is.skipChar('(');
        ArrayList<String> lang = new ArrayList<String>();
        is.skipSpaces();
        while (!is.match(')')) {
            lang.add(is.readString());
            is.skipSpaces();
        }
        return lang.toArray(new String[lang.size()]);
    }
    
    public String getType() { return type; }
    public String getSubtype() { return subtype; }
    public Map<String, String> getParameters() { return params; }
    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getEncoding() { return encoding; }
    public long getSize() { return size; }
    public long getLines() { return lines; }
    public Envelope getEnvelope() { return envelope; }
    public BodyStructure[] getParts() { return parts; }
    public String getMd5() { return md5; }
    public String[] getLanguage() { return language; }
    public String getLocation() { return location; }
    public String getDisposition() { return disposition; }
    public Map<String, String> getDispositionParameters() {
        return dispositionParams;
    }
    public boolean isMultipart() {
        return "MULTIPART".equals(type);
    }

    public String toString() {
        Formatter fmt = new Formatter();
        toString(fmt, 0, 0);
        return fmt.toString();
    }

    private void toString(Formatter fmt, int depth, int count) {
        fmt.format("%s%d: type=%s/%s encoding=%s disposition=%s",
            spaces(depth), count, type, subtype, encoding, disposition);
        if (parts != null) {
            fmt.format(" count=%d\n", parts.length);
            for (int i = 0; i < parts.length; i++) {
                toString(fmt, depth + 1, i);
            }
        } else {
            fmt.format(" size=%d\n", size);
        }
    }

    private String spaces(int depth) {
        char[] spaces = new char[depth * 4];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }
}
