package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutoProvisionTest {

    @Test
    void mapName_should_throw_service_exception_when_externalAttrs_localpart_is_null() {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getAutoProvAccountNameMap()).thenReturn("x");
        ServiceException serviceException = assertThrows(
                ServiceException.class,
                () -> new AutoProvision(null, domain) {
                    @Override
                    Account handle() throws ServiceException {
                        return null;
                    }
                }.mapName(zAttributes, null),
                "Expected mapName() to throw, but it didn't"
        );
        assertEquals("system failure: AutoProvision: unable to get localPart: null"
                , serviceException.getMessage());

    }

    @Test
    void mapName_should_throw_service_exception_when_externalAttrs_loginName_is_null() {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        ServiceException serviceException = assertThrows(
                ServiceException.class,
                () -> new AutoProvision(null, domain) {
                    @Override
                    Account handle() throws ServiceException {
                        return null;
                    }
                }.mapName(zAttributes, null),
                "Expected mapName() to throw, but it didn't"
        );
        assertEquals("system failure: AutoProvision: unable to map account name, must configure zimbraAutoProvAccountNameMap"
                , serviceException.getMessage());

    }

    @Test
    void mapName_should_return_email_when_externalAttrs_loginName_is_null_and_domain_has_no_autoProvAccountName() throws ServiceException {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getName()).thenReturn("abc.com");
        assertEquals("test@abc.com", new AutoProvision(null, domain) {
                    @Override
                    Account handle() throws ServiceException {
                        return null;
                    }
                }.mapName(zAttributes, "test@xyz.com"));

    }

    @Test
    void mapName_should_throw_service_exception_when_searchByMail_and_domain_is_not_same() throws LdapException {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getAutoProvAccountNameMap()).thenReturn("mail");
        when(zAttributes.getAttrString("mail")).thenReturn("x@abc.com");
        when(domain.getName()).thenReturn("ab.com");
        ServiceException serviceException = assertThrows(
                ServiceException.class,
                () -> new AutoProvision(null, domain) {
                    @Override
                    Account handle() throws ServiceException {
                        return null;
                    }
                }.mapName(zAttributes, null),
                "Expected mapName() to throw, but it didn't"
        );
        assertEquals("system failure: x@abc.com can not be provisioned for domain ab.com"
                , serviceException.getMessage());

    }

    @DisplayName("mapName should return email when account name is mapped with mail, userPrincipalName and other")
    @ParameterizedTest(name = "autoProvAccountNameMap={0}, attrValue={1}, domainName={2}")
    @CsvSource({
            "mail, x@abc.com, abc.com",
            "userPrincipalName, x@abc.com, abc.com",
            "other, x, abc.com"
    })
    void mapName_should_return_email_when_domain_is_same(String autoProvAccountNameMap,
                                                         String attrValue, String domainName)
            throws ServiceException {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getAutoProvAccountNameMap()).thenReturn(autoProvAccountNameMap);
        when(zAttributes.getAttrString(autoProvAccountNameMap)).thenReturn(attrValue);
        when(domain.getName()).thenReturn(domainName);

        assertEquals("x@abc.com", new AutoProvision(null, domain) {
            @Override
            Account handle() {
                return null;
            }
        }.mapName(zAttributes, null));
    }

}
