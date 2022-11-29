// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

import com.zimbra.cs.ldap.unboundid.InMemoryLdapServer;

public class PasswordUtil {

    /*
     * SSHA  (salted-SHA1)
     */
    public static class SSHA {

        private static int SALT_LEN = 4; // to match LDAP SSHA password encoding
        private static String SSHA_ENCODING = "{SSHA}";

        public static boolean isSSHA(String encodedPassword) {
            return encodedPassword.startsWith(SSHA_ENCODING);
        }

        public static boolean verifySSHA(String encodedPassword, String password) {
            if (!encodedPassword.startsWith(SSHA_ENCODING))
                return false;
            byte[] encodedBuff = encodedPassword.substring(SSHA_ENCODING.length()).getBytes();
            byte[] buff = Base64.decodeBase64(encodedBuff);
            if (buff.length <= SALT_LEN)
                return false;
            int slen = (buff.length == 28) ? 8 : SALT_LEN;
            byte[] salt = new byte[slen];
            System.arraycopy(buff, buff.length-slen, salt, 0, slen);
            String generated = generateSSHA(password, salt);
            return generated.equals(encodedPassword);
        }

        public static String generateSSHA(String password, byte[] salt) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                if (salt == null) {

                    if (InMemoryLdapServer.isOn()) {
                        // use a fixed salt
                        salt = new byte[]{127,127,127,127};
                    } else {
                        salt = new byte[SALT_LEN];
                        SecureRandom sr = new SecureRandom();
                        sr.nextBytes(salt);
                    }
                }
                md.update(password.getBytes("UTF-8"));
                md.update(salt);
                byte[] digest = md.digest();
                byte[] buff = new byte[digest.length + salt.length];
                System.arraycopy(digest, 0, buff, 0, digest.length);
                System.arraycopy(salt, 0, buff, digest.length, salt.length);
                return SSHA_ENCODING + new String(Base64.encodeBase64(buff));
            } catch (NoSuchAlgorithmException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            }
        }

    }

    /*
     * SSHA512  (salted-SHA512)
     */
    public static class SSHA512 {

        private static int SALT_LEN = 8; // to match LDAP SSHA512 password encoding
        private static String SSHA512_ENCODING = "{SSHA512}";

        public static boolean isSSHA512(String encodedPassword) {
            return encodedPassword.startsWith(SSHA512_ENCODING);
        }

        public static boolean verifySSHA512(String encodedPassword, String password) {
            if (!encodedPassword.startsWith(SSHA512_ENCODING))
                return false;
            byte[] encodedBuff = encodedPassword.substring(SSHA512_ENCODING.length()).getBytes();
            byte[] buff = Base64.decodeBase64(encodedBuff);
            if (buff.length <= SALT_LEN)
                return false;
            int slen = (buff.length == 28) ? 8 : SALT_LEN;
            byte[] salt = new byte[slen];
            System.arraycopy(buff, buff.length-slen, salt, 0, slen);
            String generated = generateSSHA512(password, salt);
            return generated.equals(encodedPassword);
        }

        public static String generateSSHA512(String password, byte[] salt) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                if (salt == null) {

                    if (InMemoryLdapServer.isOn()) {
                        // use a fixed salt
                        salt = new byte[]{127,127,127,127};
                    } else {
                        salt = new byte[SALT_LEN];
                        SecureRandom sr = new SecureRandom();
                        sr.nextBytes(salt);
                    }
                }
                md.update(password.getBytes("UTF-8"));
                md.update(salt);
                byte[] digest = md.digest();
                byte[] buff = new byte[digest.length + salt.length];
                System.arraycopy(digest, 0, buff, 0, digest.length);
                System.arraycopy(salt, 0, buff, digest.length, salt.length);
                return SSHA512_ENCODING + new String(Base64.encodeBase64(buff));
            } catch (NoSuchAlgorithmException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            }
        }

    }

    /*
     * SHA1 (unseeded)
     */
    public static class SHA1 {

        private static String SHA1_ENCODING = "{SHA1}";
        private static String SHA_ENCODING = "{SHA}";

        public static boolean isSHA1(String encodedPassword) {
            return encodedPassword.startsWith(SHA1_ENCODING) ||
				encodedPassword.startsWith(SHA_ENCODING);
        }

        public static boolean verifySHA1(String encodedPassword, String password) {
            byte[] encodedBuff;
			String prefix = SHA1_ENCODING;
            if (encodedPassword.startsWith(SHA1_ENCODING))
				prefix = SHA1_ENCODING;
			else if (encodedPassword.startsWith(SHA_ENCODING))
				prefix = SHA_ENCODING;
			else
				return false;
			encodedBuff = encodedPassword.substring(prefix.length()).getBytes();
            byte[] buff = Base64.decodeBase64(encodedBuff);
            String generated = generateSHA1(password, prefix);
            return generated.equals(encodedPassword);
        }

        public static String generateSHA1(String password) {
			return generateSHA1(password, SHA1_ENCODING);
		}

        public static String generateSHA1(String password, String prefix) {
			if (prefix == null) prefix = SHA1_ENCODING;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(password.getBytes("UTF-8"));

                byte[] digest = md.digest();
                return prefix + new String(Base64.encodeBase64(digest));
            } catch (NoSuchAlgorithmException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * MD5
     */
    public static class MD5 {

        private static String MD5_ENCODING = "{MD5}";

        public static boolean isMD5(String encodedPassword) {
            return encodedPassword.startsWith(MD5_ENCODING);
        }

        public static boolean verifyMD5(String encodedPassword, String password) {
            if (!encodedPassword.startsWith(MD5_ENCODING))
                return false;
            byte[] encodedBuff = encodedPassword.substring(MD5_ENCODING.length()).getBytes();
            byte[] buff = Base64.decodeBase64(encodedBuff);
            String generated = generateMD5(password);
            return generated.equals(encodedPassword);
        }

        public static String generateMD5(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(password.getBytes("UTF-8"));

                byte[] digest = md.digest();
                return MD5_ENCODING + new String(Base64.encodeBase64(digest));
            } catch (NoSuchAlgorithmException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String args[]) {

        String plain = "test123";
        System.out.println("plain:        " + plain);
        System.out.println("encoded SSHA: " + SSHA.generateSSHA(plain, null));
        System.out.println("encoded SSHA512: " + SSHA512.generateSSHA512(plain, null));
        System.out.println("encoded SSH1: " + SHA1.generateSHA1(plain));
        System.out.println("encoded MD5:  " + MD5.generateMD5(plain));
        System.out.println();

        plain = "helloWorld";
        System.out.println("plain:        " + plain);
        System.out.println("encoded SSHA: " + SSHA.generateSSHA(plain, null));
        System.out.println("encoded SSHA512: " + SSHA512.generateSSHA512(plain, null));
        System.out.println("encoded SSH1: " + SHA1.generateSHA1(plain));
        System.out.println("encoded MD5:  " + MD5.generateMD5(plain));
        System.out.println();

        plain = "testme";
        String encodedSHA1 = SHA1.generateSHA1(plain, SHA1.SHA1_ENCODING);
        String encodedSHA  = SHA1.generateSHA1(plain, SHA1.SHA_ENCODING);
        boolean result;
        result = SHA1.verifySHA1(encodedSHA1, plain);
        System.out.println("result is " + (result?"good":"bad"));
        result = SHA1.verifySHA1(encodedSHA, plain);
        System.out.println("result is " + (result?"good":"bad"));
    }

}
