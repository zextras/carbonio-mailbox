// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.FileUtil;

import junit.framework.TestCase;

public class TestFileUtil extends TestCase {
    private File TEST_DIR;

    private File genFile(String path, int numBytes) throws Exception {
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);
        
        byte[] bytes = new byte[numBytes];
        int offset = 0;
        for (int i=0; i<numBytes; i++) {
            byte[] src = (String.valueOf(i) + "\n").getBytes();
            if (offset+src.length < numBytes) {
                System.arraycopy(src, 0, bytes, offset, src.length);
                offset += src.length;
            } else {
                // pad remaining bytes with *
                while (offset < numBytes)
                    bytes[offset++] = '*';
                break;
            }
        }  
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ByteUtil.copy(bais, true, fos, true);
        return file;      
    }
    
    private void printFile(File file) throws Exception {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(isr);
        String line;
        while ((line = reader.readLine())!=null ) {
            System.out.println(line);    
        }
    }
    
    private boolean isGzip(File file) throws Exception {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        int header = is.read() | (is.read() << 8);
        is.close();
        if (header == GZIPInputStream.GZIP_MAGIC)
            return true;
        else
            return false;
    }

    protected void setUp() throws Exception {
        String tempdir = System.getProperty("java.io.tmpdir");
        TEST_DIR = new File(tempdir + "/" + "testFileUtil");
        FileUtil.ensureDirExists(TEST_DIR);
    }
    
    protected void tearDown() throws Exception {
        FileUtil.deleteDir(TEST_DIR);
    }
    
    private File newFile(String fileName) {
        return new File(newFileName(fileName));
    }
    
    private String newFileName(String fileName) {
        return TEST_DIR.getAbsolutePath() + "/" + fileName;
    }
    
    public void testCompress() throws Exception {
        File orig = genFile(newFileName("junk.txt"), 1024);
        // printFile(orig);
        
        // compress it
        File compressed = newFile("junk.compressed");
        FileUtil.compress(orig, compressed, true);
        assertTrue(isGzip(compressed));
        
        // uncompress it
        File uncompressed = newFile("junk.uncompressed");
        FileUtil.uncompress(compressed, uncompressed, true);
        
        // uncompressed file should be identical to the original file
        byte[] origBytes = ByteUtil.getContent(orig);
        byte[] uncompressedBytes = ByteUtil.getContent(uncompressed);
        assertTrue(Arrays.equals(origBytes, uncompressedBytes));
    }
    
    public static void main(String[] args)
    throws Exception {
        TestUtil.cliSetup();
        TestUtil.runTest(TestFileUtil.class);
    }
}