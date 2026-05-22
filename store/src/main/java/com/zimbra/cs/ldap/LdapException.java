// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.zimbra.common.service.ServiceException;
import java.io.IOException;

public class LdapException extends ServiceException {

    public static final String TODO = "ldap.TODO";
    
    public static final String INVALID_CONFIG = "ldap.INVALID_CONFIG";
    
    // generic LDAP error that is not mapped to a subclass of LdapException
    public static final String LDAP_ERROR = "ldap.LDAP_ERROR";
    
    public static final String CONTEXT_NOT_EMPTY        = "ldap.CONTEXT_NOT_EMPTY";
    public static final String ENTRY_ALREADY_EXIST      = "ldap.ENTRY_ALREADY_EXIST";
    public static final String ENTRY_NOT_FOUND          = "ldap.ENTRY_NOT_FOUND";
    public static final String INVALID_ATTR_NAME        = "ldap.INVALID_ATTR_NAME";
    public static final String INVALID_ATTR_VALUE       = "ldap.INVALID_ATTR_VALUE";
    public static final String INVALID_NAME             = "ldap.INVALID_NAME";
    public static final String INVALID_SEARCH_FILTER    = "ldap.INVALID_SEARCH_FILTER";
    public static final String INVALID_REQUEST          = "ldap.INVALID_REQUEST";
    public static final String MULTIPLE_ENTRIES_MATCHED = "ldap.MULTIPLE_ENTRIES_MATCHED";
    public static final String OBJECT_CLASS_VIOLATION   = "ldap.OBJECT_CLASS_VIOLATION";
    public static final String SIZE_LIMIT_EXCEEDED      = "ldap.SIZE_LIMIT_EXCEEDED";
        
    // in addition to getCause(), a more exception for callsites to relate 
    // the exception to a user message.
    private Throwable detail;
    
    private static String format(String msg1, String msg2, Throwable cause) {
        String msg = msg1;
        if (msg2 != null) {
            msg = msg + " - " + msg2;
        }
        
        if (cause != null) {
            return msg + ": " + cause.getMessage();
        } else {
            return msg;
        }
    }
    
    public void setDetail(Throwable detail) {
        this.detail = detail;
    }
    
    public Throwable getDetail() {
        return detail;
    }
    
    protected LdapException(String message, String code, Throwable cause) {
        super(message, code, RECEIVERS_FAULT, cause);
    }
    
    public static LdapException TODO() {
        return new LdapException("TODO", TODO, null);
    }
    
    public static LdapException INVALID_CONFIG(Throwable cause) {
        return new LdapException("config error", INVALID_CONFIG,  cause);
    }
    
    public static LdapException INVALID_CONFIG(String message, Throwable cause) {
        return new LdapException(format("config error: ", message, cause), INVALID_CONFIG,  cause);
    }
    
    // generic LDAP error
    public static LdapException LDAP_ERROR(Throwable cause) {
        return new LdapException("LDAP error", LDAP_ERROR,  cause);
    }
    
    // generic LDAP error
    public static LdapException LDAP_ERROR(String message, Throwable cause) {
        return new LdapException(format("LDAP error: ", message, cause), LDAP_ERROR, cause);
    }
    
    //
    // Specific LDAP errors needs handling
    //
    public static LdapException CONTEXT_NOT_EMPTY(String message, Throwable cause) {
        return new LdapContextNotEmptyException(message, cause);
    }
    
    public static LdapException INVALID_ATTR_NAME(String message, Throwable cause) {
        return new LdapInvalidAttrNameException(message, cause);
    }
    
    public static LdapException INVALID_ATTR_VALUE(String message, Throwable cause) {
        return new LdapInvalidAttrValueException(message, cause);
    }
    
    public static LdapException INVALID_NAME(String message, Throwable cause) {
        return new LdapInvalidNameException(message, cause);
    }
    
    public static LdapException INVALID_SEARCH_FILTER(String message, Throwable cause) {
        return new LdapInvalidSearchFilterException(message, cause);
    }
    
    public static LdapException INVALID_REQUEST(String message, Throwable cause) {
        return new LdapInvalidRequestException(message, cause);
    }
    
    public static LdapException ENTRY_ALREADY_EXIST(String message, Throwable cause) {
        return new LdapEntryAlreadyExistException(message, cause);
    }
    
    public static LdapException ENTRY_NOT_FOUND(String message, Throwable cause) {
        return new LdapEntryNotFoundException(message, cause);
    }
    
    public static LdapException MULTIPLE_ENTRIES_MATCHED(String base, 
            String query, String dups) {
        return new LdapMultipleEntriesMatchedException(base, query, dups);
    }
    
    public static LdapException OBJECT_CLASS_VIOLATION(String message, Throwable cause) {
        return new LdapObjectClassViolationException(message, cause);
    }
    
    public static LdapException SIZE_LIMIT_EXCEEDED(String message, Throwable cause) {
        return new LdapSizeLimitExceededException(message, cause);
    }

    public static LdapException mapToLdapException(Throwable e) {
        return mapToLdapException(null, e);
    }

    public static LdapException mapToLdapException(String message, Throwable e) {
        if (e instanceof LDAPException) {
            return mapToLdapException(message, (LDAPException) e);
        } else {
            return LdapException.LDAP_ERROR(message, e);
        }
    }

    public static LdapException mapToLdapException(LDAPException e) {
        return mapToLdapException(null, e);
    }

    public static LdapException mapToLdapException(String message, LDAPException e) {
        ResultCode rc = e.getResultCode();

        if (ResultCode.ENTRY_ALREADY_EXISTS == rc) {
            return LdapException.ENTRY_ALREADY_EXIST(message, e);
        } else if (ResultCode.NOT_ALLOWED_ON_NONLEAF == rc) {
            return LdapException.CONTEXT_NOT_EMPTY(message, e);
        } else if (ResultCode.UNDEFINED_ATTRIBUTE_TYPE == rc) {
            return LdapException.INVALID_ATTR_NAME(message, e);
        } else if (ResultCode.CONSTRAINT_VIOLATION == rc
                || ResultCode.INVALID_ATTRIBUTE_SYNTAX == rc) {
            return LdapException.INVALID_ATTR_VALUE(message, e);
        } else if (ResultCode.OBJECT_CLASS_VIOLATION == rc) {
            return LdapException.OBJECT_CLASS_VIOLATION(message, e);
        } else if (ResultCode.SIZE_LIMIT_EXCEEDED == rc) {
            return LdapException.SIZE_LIMIT_EXCEEDED(message, e);
        } else if (ResultCode.NO_SUCH_OBJECT == rc) {
            // mostly when the search base DB does not exist in the DIT
            return LdapException.ENTRY_NOT_FOUND(message, e);
        } else if (ResultCode.FILTER_ERROR == rc) {
            return LdapException.INVALID_SEARCH_FILTER(message, e);
        }

        return LdapException.LDAP_ERROR(message, e);
    }

    // need more precise mapping for external LDAP exceptions so we
    // can report config error better
    public static LdapException mapToExternalLdapException(String message, LDAPException e) {
        Throwable cause = e.getCause();

        // the LdapException instance to return
        LdapException ldapException = mapToLdapException(message, e);

        if (cause instanceof IOException) {
            // Unboundid hides the original IOException and throws a generic IOException.
            // This doesn't work with check.toResult(IOException). Do our best to figure
            // out the original IOException and set it in the detail field.
            //
            // e.g. An error occurred while attempting to establish a connection to server bogus:389:
            //      java.net.UnknownHostException: bogus
            IOException ioException = (IOException) cause;
            String causeMsg = ioException.getMessage();
            IOException rootException = null;
            if (causeMsg != null) {
                if (causeMsg.contains("java.net.UnknownHostException")) {
                    rootException = new java.net.UnknownHostException(causeMsg);
                } else if (causeMsg.contains("java.net.ConnectException")) {
                    rootException = new java.net.ConnectException(causeMsg);
                } else if (causeMsg.contains("javax.net.ssl.SSLHandshakeException")) {
                    rootException = new javax.net.ssl.SSLHandshakeException(causeMsg);
                }
            }
            if (rootException != null) {
                ldapException.setDetail(rootException);
            } else {
                ldapException.setDetail(cause);
            }
        } else {
            String causeMsg = e.getMessage();

            Throwable rootException;
            if (causeMsg.contains("unsupported extended operation")) {
                // most likely startTLS failed, for backward compatibility with check.toResult,
                // return a generic IOException
                rootException = new IOException(causeMsg);
            } else {
                rootException = mapToLdapException(message, e);
            }

            ldapException.setDetail(rootException);
        }

        return ldapException;
    }


    //
    // Subclasses mapped to native ldap exceptions
    //
    
    public static class LdapContextNotEmptyException extends LdapException {
        private LdapContextNotEmptyException(String message, Throwable cause) {
            super(format("context not empty", message, cause), CONTEXT_NOT_EMPTY, cause);
        }
    }
    
    public static class LdapEntryAlreadyExistException extends LdapException {
        private LdapEntryAlreadyExistException(String message, Throwable cause) {
            super(format("entry already exist", message, cause), ENTRY_ALREADY_EXIST, cause);
        }
    }
    
    public static class LdapEntryNotFoundException extends LdapException {
        private LdapEntryNotFoundException(String message, Throwable cause) {
            super(format("entry not found", message, cause), ENTRY_NOT_FOUND, cause);
        }
    }
    
    public static class LdapInvalidAttrNameException extends LdapException {
        private LdapInvalidAttrNameException(String message, Throwable cause) {
            super(format("invalid attr name", message, cause), INVALID_ATTR_NAME, cause);
        }
    }
    
    public static class LdapInvalidAttrValueException extends LdapException {
        private LdapInvalidAttrValueException(String message, Throwable cause) {
            super(format("invalid attr value", message, cause), INVALID_ATTR_VALUE, cause);
        }
    }
    
    public static class LdapInvalidNameException extends LdapException {
        private LdapInvalidNameException(String message, Throwable cause) {
            super(format("invalid name", message, cause), INVALID_NAME, cause);
        }
    }
    
    public static class LdapInvalidSearchFilterException extends LdapException {
        private LdapInvalidSearchFilterException(String message, Throwable cause) {
            super(format("invalid search filter", message, cause), INVALID_SEARCH_FILTER, cause);
        }
    }
    
    public static class LdapInvalidRequestException extends LdapException {
        private LdapInvalidRequestException(String message, Throwable cause) {
            super(format("invalid API usage", message, cause), INVALID_REQUEST, cause);
        }
    }
    
    public static class LdapMultipleEntriesMatchedException extends LdapException {
        private String base;
        private String query;
        private String dups;
        private LdapMultipleEntriesMatchedException(String base, String query, String dups) {
            super(String.format("multiple entries matched: base=%s, query=%s, entries=%s",
                    base, query, dups), MULTIPLE_ENTRIES_MATCHED, null);
            this.base = base;
            this.query = query;
            this.dups = dups;
        }
        
        public String getQueryBase() {
            return base;
        }
        
        public String getQuery() {
            return query;
        }
        
        public String getDuplicatedEntries() {
            return dups;
        }
    }
    
    public static class LdapObjectClassViolationException extends LdapException {
        private LdapObjectClassViolationException(String message, Throwable cause) {
            super(format("object class violation", message, cause), OBJECT_CLASS_VIOLATION, cause);
        }
    }
    
    public static class LdapSizeLimitExceededException extends LdapException {
        private LdapSizeLimitExceededException(String message, Throwable cause) {
            super(format("size limit exceeded", message, cause), SIZE_LIMIT_EXCEEDED, cause);
        }
    }
}
