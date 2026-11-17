// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

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

                    salt = new byte[SALT_LEN];
                    SecureRandom sr = new SecureRandom();
                    sr.nextBytes(salt);
                }
                md.update(password.getBytes(StandardCharsets.UTF_8));
                md.update(salt);
                byte[] digest = md.digest();
                byte[] buff = new byte[digest.length + salt.length];
                System.arraycopy(digest, 0, buff, 0, digest.length);
                System.arraycopy(salt, 0, buff, digest.length, salt.length);
                return SSHA_ENCODING + new String(Base64.encodeBase64(buff));
            } catch (NoSuchAlgorithmException e) {
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

                    salt = new byte[SALT_LEN];
                    SecureRandom sr = new SecureRandom();
                    sr.nextBytes(salt);
                }
                md.update(password.getBytes(StandardCharsets.UTF_8));
                md.update(salt);
                byte[] digest = md.digest();
                byte[] buff = new byte[digest.length + salt.length];
                System.arraycopy(digest, 0, buff, 0, digest.length);
                System.arraycopy(salt, 0, buff, digest.length, salt.length);
                return SSHA512_ENCODING + new String(Base64.encodeBase64(buff));
            } catch (NoSuchAlgorithmException e) {
                // this shouldn't happen unless JDK is foobar
                throw new RuntimeException(e);
            }
        }

    }

}
