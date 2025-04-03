package com.zimbra.cs.account.ldap;

import com.zimbra.cs.ldap.LdapDateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

class AutoProvisionEagerTest {

    @Test
    void test_getLastTimestamp_when_timestampString_is_null_return_lastTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastTimestamp(null, 5));
    }

    @Test
    void test_getLastTimestamp_when_timestampString_is_empty_return_lastTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastTimestamp("", 5));
    }

    @Test
    void test_getLastTimestamp_when_timestampString_is_empty_with_space_return_lastTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastTimestamp("  ", 5));
    }

    @Test
    void test_getLastTimestamp_when_timestampString_is_size_less_than_14_than_lastTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastTimestamp("6", 5));
    }

    @Test
    void test_getLastTimestamp_when_timestampString_is_not_valid_return_lastTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastTimestamp("ABC45678901234", 5));
    }

    @Test
    void test_getLastTimestamp_when_timestampString_is_older_than_serverTime_then_TimeStamp_plus_Delta() {
        long serverTime = System.currentTimeMillis();
        long createTimestamp = serverTime - 2000L;
        String generalizedTimeWithMs = LdapDateUtil.toGeneralizedTimeWithMs(new Date(createTimestamp));
        Assertions.assertEquals(createTimestamp, AutoProvisionEager.getLastTimestamp(generalizedTimeWithMs, 0));
    }

}
