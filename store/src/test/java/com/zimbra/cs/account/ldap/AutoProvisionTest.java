package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoProvisionTest {

    @Test
    void mapName_should_throw_service_exception_when_externalAttrs_loocalpart_is_null() {
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
        assertEquals("system failure: AutoProvision: unable to map acount name, must configure zimbraAutoProvAccountNameMap"
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

    @Test
    void mapName_should_return_email_when_searchByMail_and_domain_is_same() throws ServiceException {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getAutoProvAccountNameMap()).thenReturn("mail");
        when(zAttributes.getAttrString("mail")).thenReturn("x@abc.com");
        when(domain.getName()).thenReturn("abc.com");
        assertEquals("x@abc.com", new AutoProvision(null, domain) {
                    @Override
                    Account handle() throws ServiceException {
                        return null;
                    }
                }.mapName(zAttributes, null));

    }

    @Test
    void mapName_should_return_email_when_searchByUserPrinciple_and_domain_is_same() throws ServiceException {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getAutoProvAccountNameMap()).thenReturn("userPrincipalName");
        when(zAttributes.getAttrString("userPrincipalName")).thenReturn("x@abc.com");
        when(domain.getName()).thenReturn("abc.com");
        assertEquals("x@abc.com", new AutoProvision(null, domain) {
            @Override
            Account handle() throws ServiceException {
                return null;
            }
        }.mapName(zAttributes, null));

    }

    @Test
    void mapName_should_return_email_when_searchByOthere_and_domain_is_same() throws ServiceException {
        Domain domain = mock(Domain.class);
        ZAttributes zAttributes = mock(ZAttributes.class);
        when(domain.getAutoProvAccountNameMap()).thenReturn("other");
        when(zAttributes.getAttrString("other")).thenReturn("x");
        when(domain.getName()).thenReturn("abc.com");
        assertEquals("x@abc.com", new AutoProvision(null, domain) {
            @Override
            Account handle() throws ServiceException {
                return null;
            }
        }.mapName(zAttributes, null));

    }

}
