package com.zimbra.cs.account.ldap;

import com.zimbra.cs.ldap.LdapDateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

class AutoProvisionEagerTest {

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_null_return_lastCreateTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastCreateTimestamp(null, 5, 6));
    }

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_empty_return_lastCreateTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastCreateTimestamp("", 5, 6));
    }

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_empty_with_space_return_lastCreateTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastCreateTimestamp("  ", 5, 6));
    }

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_size_less_than_14_than_lastCreateTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastCreateTimestamp("6", 5, 6));
    }

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_not_valid_return_lastCreateTimestamp() {
        Assertions.assertEquals(5, AutoProvisionEager.getLastCreateTimestamp("ABC45678901234", 5, 6));
    }

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_older_than_serverTime_then_createTimeStamp_plus_Delta() {
        long serverTime = System.currentTimeMillis();
        long createTimestamp = serverTime - 2000L;
        String generalizedTimeWithMs = LdapDateUtil.toGeneralizedTimeWithMs(new Date(createTimestamp));
        Assertions.assertEquals(createTimestamp + AutoProvisionEager.DELTA_FOR_LAST_TIME, AutoProvisionEager.getLastCreateTimestamp(generalizedTimeWithMs, 0, serverTime));
    }

    @Test
    void test_getLastCreateTimestamp_when_createTimestampString_is_newer_than_serverTime_then_serverTime_plus_delta() {
        long serverTime = System.currentTimeMillis();
        long createTimestamp = serverTime + 20000L;
        String generalizedTimeWithMs = LdapDateUtil.toGeneralizedTimeWithMs(new Date(createTimestamp));
        Assertions.assertEquals(serverTime + AutoProvisionEager.DELTA_FOR_LAST_TIME, AutoProvisionEager.getLastCreateTimestamp(generalizedTimeWithMs, 0, serverTime));
    }

    @Test
    void test_getLastCreateTimestamp_when_lastCreationTime_is_newer_than_serverTime_then_return_lastCreationTime() {
        long serverTime = System.currentTimeMillis();
        long createTimestamp = serverTime + 20000L;
        String generalizedTimeWithMs = LdapDateUtil.toGeneralizedTimeWithMs(new Date(createTimestamp));
        Assertions.assertEquals(serverTime + 1, AutoProvisionEager.getLastCreateTimestamp(generalizedTimeWithMs, serverTime + 1, serverTime));
    }

    @Test
    void test_getLastCreateTimestamp_when_lastCreationTime_is_same_than_serverTime_then_return_lastCreationTime() {
        long serverTime = System.currentTimeMillis();
        long createTimestamp = serverTime + 20000L;
        String generalizedTimeWithMs = LdapDateUtil.toGeneralizedTimeWithMs(new Date(createTimestamp));
        Assertions.assertEquals(serverTime, AutoProvisionEager.getLastCreateTimestamp(generalizedTimeWithMs, serverTime, serverTime));
    }

}
