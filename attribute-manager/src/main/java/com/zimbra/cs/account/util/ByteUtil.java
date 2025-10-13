// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 18, 2004
 */
package com.zimbra.cs.account.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.logging.Logger;

/**
 * @author schemers
 */
public class ByteUtil {
    private static final Logger logger = Logger.getLogger(ByteUtil.class.getName());
    public static void closeStream(InputStream is) {
        if (is == null)
            return;

        if (is instanceof PipedInputStream) {
            try {
                drain(is, false);
            } catch (Exception e) {
                logger.warning("ignoring exception while draining PipedInputStream: " + e.getMessage());
            }
        }

        try {
            is.close();
        } catch (Exception e) {
            logger.warning("ignoring exception while closing input stream: " + e.getMessage());
        }
    }

    public static <T extends InputStream> T drain(T is) throws IOException {
        return drain(is, true);
    }

    /** Read the stream to its end, discarding all read data. */
    public static <T extends InputStream> T drain(T is, boolean closeStream) throws IOException {
        // side effect of our implementation of counting bytes is draining the stream
        countBytes(is, closeStream);
        return is;
    }


    /**
     * Count the total number of bytes of the <code>InputStream</code>
     * @param is The stream to read from.
     * @return total number of bytes
     * @throws IOException
     */
    public static int countBytes(InputStream is, boolean closeStream) throws IOException {
        try {
            byte[] buf = new byte[8192];
            int count = 0;
            int num = 0;
            // if you tweak this implementation, make sure drain() still works...
            while ((num = is.read(buf)) != -1)
                count += num;
            return count;
        } finally {
            if (closeStream) {
                ByteUtil.closeStream(is);
            }
        }
    }
    /**
     * read all the content in the specified file and
     * return as byte array.
     * @param file file to read
     * @return content of the file
     * @throws IOException
     */
    public static byte[] getContent(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];

        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int total_read = 0, num_read;

            int num_left = buffer.length;

            while (num_left > 0 && (num_read = is.read(buffer, total_read, num_left)) != -1) {
                total_read += num_read;
                num_left -= num_read;
            }
        } finally {
            closeStream(is);
        }
        return buffer;
    }
}
