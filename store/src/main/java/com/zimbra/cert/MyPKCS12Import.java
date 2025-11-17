// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
* This class can be used to import a key/certificate pair from a pkcs12 file
* into a regular JKS format keystore for use with jetty and other java based
* SSL applications, etc. 
*<PRE>
*    usage: java PKCS12Import {pkcs12file} [newjksfile] [inputPassphrase] [outputPassphrase]
*    
*    inputPassphrase is for pkcs12file
*    outputPassphrase is for newjksfile
*</PRE>
*
* 
* <P>
* Upon execution, you will be prompted for the password for the pkcs12 keystore
* as well as the password for the jdk file.  After execution you should have a
* JKS keystore file that contains the private key and certificate that were in
* the pkcs12
* <P>
* You can generate a pkcs12 file from PEM encoded certificate and key files
* using the following openssl command:
* <PRE>
*    openssl pkcs12 -export -out keystore.pkcs12 -in www.crt -inkey www.key
* </PRE>
* then run:
* <PRE>
*    java PKCS12Import keystore.pkcs12 keytore.jks inputPassphrase  outputPassphrase
* </PRE>
*
* The customization for Zimbra to read the keystore password from 
* as an argument instead of the stdin .
*

* @author ccao (Customized for zimbra)
*/
public class MyPKCS12Import
{
    public static void main(String[] args) throws Exception
    {
       if (args.length < 4) {
          System.err.println(
                "usage: java MyPKCS12Import {pkcs12file} [newjksfile] [inputPassphrase] [outputPassphrase]");
          System.exit(1);
       }
    
       File fileIn = new File(args[0]);
       File fileOut = new File(args[1]);
       String passIn = args[2] ;
       String passOut = args[3] ;
    
       if (!fileIn.canRead()) {
          System.err.println(
                "Unable to access input keystore: " + fileIn.getPath());
          System.exit(2);
       }
    
       if (fileOut.exists() && !fileOut.canWrite()) {
          System.err.println(
                "Output file is not writable: " + fileOut.getPath());
          System.exit(2);
       }
    
       KeyStore kspkcs12 = KeyStore.getInstance("pkcs12");
       KeyStore ksjks = KeyStore.getInstance("jks");
    
       char[] inphrase  = passIn.toCharArray() ;
       char[] outphrase  = passOut.toCharArray() ;
       
       FileInputStream input = new FileInputStream(fileIn);
       kspkcs12.load(input, inphrase);

       FileInputStream output = null;
       if (fileOut.exists()) {
    	   output = new FileInputStream(fileOut); 
           ksjks.load(output, outphrase);
       } else {
           ksjks.load(null, outphrase);
       }
    
       Enumeration eAliases = kspkcs12.aliases();
       int n = 0;
       while (eAliases.hasMoreElements()) {
          String strAlias = (String)eAliases.nextElement();
          System.err.println("Alias " + n++ + ": " + strAlias);
    
          if (kspkcs12.isKeyEntry(strAlias)) {
             System.err.println("Adding key for alias " + strAlias);
             Key key = kspkcs12.getKey(strAlias, inphrase);
    
             Certificate[] chain = kspkcs12.getCertificateChain(strAlias);
    
             ksjks.setKeyEntry(strAlias, key, outphrase, chain);
          }
       }
    
       OutputStream out = new FileOutputStream(fileOut);
       ksjks.store(out, outphrase);
       out.close(); 
       input.close();
       if (output != null) {
    	   output.close();
       }
    } 
    
    static void dumpChain(Certificate[] chain)
    {
       for (int i = 0; i < chain.length; i++) {
          Certificate cert = chain[i];
          if (cert instanceof X509Certificate) {
             X509Certificate x509 = (X509Certificate)chain[i];
             System.err.println("subject: " + x509.getSubjectDN());
             System.err.println("issuer: " + x509.getIssuerDN());
          }
       }
    }
    
    static char[] readPassphrase() throws IOException
    {
       InputStreamReader in = new InputStreamReader(System.in);
    
       char[] cbuf = new char[256];
       int i = 0;
    
    readchars:
       while (i < cbuf.length) {
          char c = (char)in.read();
          switch (c) {
             case '\r':
                break readchars;
             case '\n':
                break readchars;
             default:
                cbuf[i++] = c;
          }
       }
    
       char[] phrase = new char[i];
       System.arraycopy(cbuf, 0, phrase, 0, i);
       return phrase;
    }
}

