// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.security.sasl;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * Class used to wrap either SaslServer or SaslClient and lift out common
 * methods related to the negotiated security layer as well as add some new
 * convenience methods.
 */
public abstract class SaslSecurityLayer {
    public static final String QOP_AUTH = "auth";
    public static final String QOP_AUTH_INT = "auth-int";
    public static final String QOP_AUTH_CONF = "auth-conf";

    /** Default max send buffer size */
    public static final int MAX_SEND_SIZE = 4096;

    /** Default max receive buffer size */
    public static final int MAX_RECV_SIZE = 65536;

    public static SaslSecurityLayer getInstance(final SaslClient client) {
        return new SaslSecurityLayer() {
            @Override public String getMechanismName() {
                return client.getMechanismName();
            }
            @Override public Object getNegotiatedProperty(String name) {
                return client.getNegotiatedProperty(name);
            }
            @Override public byte[] wrap(byte[] b, int off, int len) throws SaslException {
                return client.wrap(b, off, len);
            }
            @Override public byte[] unwrap(byte[] b, int off, int len) throws SaslException {
                return client.unwrap(b, off, len);
            }
            @Override public void dispose() throws SaslException {
                client.dispose();
            }
        };
    }

    public static SaslSecurityLayer getInstance(final SaslServer server) {
        return new SaslSecurityLayer() {
            @Override public String getMechanismName() {
                return server.getMechanismName();
            }
            @Override public Object getNegotiatedProperty(String name) {
                return server.getNegotiatedProperty(name);
            }
            @Override public byte[] wrap(byte[] b, int off, int len) throws SaslException {
                return server.wrap(b, off, len);
            }
            @Override public byte[] unwrap(byte[] b, int off, int len) throws SaslException {
                return server.unwrap(b, off, len);
            }
            @Override public void dispose() throws SaslException {
                server.dispose();
            }
        };
    }
    
    public abstract String getMechanismName();
    public abstract Object getNegotiatedProperty(String name);
    public abstract byte[] wrap(byte[] b, int off, int len) throws SaslException;
    public abstract byte[] unwrap(byte[] b, int off, int len) throws SaslException;
    public abstract void dispose() throws SaslException;

    /**
     * Returns true if a security layer (either integrity or confidentiality)
     * is enabled for the associated SaslClient or SaslServer.
     *
     * @return true if a security layer is enabled, false otherwise
     */
    public boolean isEnabled() {
        String qop = (String) getNegotiatedProperty(Sasl.QOP);
        return qop != null && (QOP_AUTH_INT.equals(qop) || QOP_AUTH_CONF.equals(qop));
    }

    public int getMaxSendSize() {
        try {
            String s = (String) getNegotiatedProperty(Sasl.RAW_SEND_SIZE);
            return Integer.parseInt(s);
        } catch (Exception e) {
            return MAX_SEND_SIZE;
        }
    }

    public int getMaxRecvSize() {
        try {
            String s = (String) getNegotiatedProperty(Sasl.MAX_BUFFER);
            return Integer.parseInt(s);
        } catch (Exception e) {
            return MAX_RECV_SIZE;
        }
    }
}
