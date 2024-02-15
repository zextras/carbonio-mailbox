package com.zimbra.cs.ldap;

import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LdapUtilTest {

    @Test
    void validateZimbraId_throws_IllegalArgumentException_when_length_is_too_large() {
        String id = "1234567890123456789012345678901234567";
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> LdapUtil.validateZimbraId(id));
        Assertions.assertInstanceOf(IllegalArgumentException.class, serviceException.getCause());
        Assertions.assertEquals("UUID string too large", serviceException.getCause().getMessage());
        Assertions.assertEquals("invalid request: 1234567890123456789012345678901234567 is not a valid UUID",
                serviceException.getMessage());
    }

    @Test
    void validateZimbraId_throws_IllegalArgumentException_when_the_format_is_not_valid() throws ServiceException {
        String id = "123456789012345678901234567890123456";
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> LdapUtil.validateZimbraId(id));
        Assertions.assertInstanceOf(IllegalArgumentException.class, serviceException.getCause());
        Assertions.assertEquals("Invalid UUID string: 123456789012345678901234567890123456", serviceException.getCause().getMessage());
        Assertions.assertEquals("invalid request: 123456789012345678901234567890123456 is not a valid UUID",
                serviceException.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789012345678901234567890123456",
            "1-1-1-1",
            "08c690d5-a6e9-4692-9035"
    })
    void validateZimbraId_throws_IllegalArgumentException_when_invalid_zimbra_ids(String zimbraId) throws ServiceException {
        String id = "123456789012345678901234567890123456";
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> LdapUtil.validateZimbraId(zimbraId));
        Assertions.assertInstanceOf(IllegalArgumentException.class, serviceException.getCause());
        Assertions.assertEquals("Invalid UUID string: " + zimbraId, serviceException.getCause().getMessage());
        Assertions.assertEquals("invalid request: " + zimbraId + " is not a valid UUID",
                serviceException.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1-1-1-1-", "----1", "----", "-1--2-3", "1-1--2-3"})
    void validateZimbraId_throws_NumberFormatException_when_invalid_zimbra_ids_without_ends_number(String zimbraId) throws ServiceException {
        String id = "123456789012345678901234567890123456";
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> LdapUtil.validateZimbraId(zimbraId));
        Assertions.assertInstanceOf(NumberFormatException.class, serviceException.getCause());
        Assertions.assertEquals("invalid request: " + zimbraId + " is not a valid UUID",
                serviceException.getMessage());
    }

    @Test
    void validateZimbraId_throws_IllegalArgumentException_when_input_is_empty() throws ServiceException {
        String id = "";
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> LdapUtil.validateZimbraId(id));
        Assertions.assertInstanceOf(IllegalArgumentException.class, serviceException.getCause());
        Assertions.assertEquals("Invalid UUID string: ", serviceException.getCause().getMessage());
        Assertions.assertEquals("invalid request:  is not a valid UUID",
                serviceException.getMessage());
    }

    @Test
    void validateZimbraId_throws_ServiceException_when_input_is_null() {
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> LdapUtil.validateZimbraId(null));
        Assertions.assertNull(serviceException.getCause());
        Assertions.assertEquals("invalid request: null is not a valid zimbraId",
                serviceException.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"08c690d5-a6e9-4692-9035-f7b5885a12b8",
            "1-1-1-1-1",
            "08c690d5-a6e9-4692-9035-f7b"
    })
    void should_validate_zimbra_id_when_id_is_valid(String zimbraId) throws ServiceException {
        LdapUtil.validateZimbraId(zimbraId);
        Assertions.assertTrue(true);
    }
}
