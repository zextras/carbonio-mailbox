package com.zimbra.common.soap;

public final class SignatureConstants {
    public static final String CERTIFICATE = "certificate";
    public static final String SIGNATURE = "signature";
    public static final String TYPE = "type";
    public static final String VALID = "valid";
    public static final String MESSAGE_CODE = "messageCode";
    public static final String MESSAGE = "message";
    public static final String EMAIL = "email";
    public static final String NOT_BEFORE = "notBefore";
    public static final String NOT_AFTER = "notAfter";
    public static final String ISSUER = "issuer";
    public static final String TRUSTED = "trusted";
    public static final String X_509 = "X.509";
    public static final String PKIX = "PKIX";

    public enum MessageCodeEnum {
        VALID,
        INVALID,
        UNTRUSTED,
        SIGNER_CERT_EXPIRED,
        SIGNER_CERT_NOT_FOUND,
        ISSUER_CERT_NOT_FOUND,
        ERROR
    }

    public enum SignatureType {
        SMIME("S/MIME"),
        PGP("PGP");
        private final String value;
        SignatureType(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    private SignatureConstants() {
    }


}
