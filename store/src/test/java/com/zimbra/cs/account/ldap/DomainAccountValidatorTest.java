package com.zimbra.cs.account.ldap;


import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.type.CountObjectsType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

class DomainAccountValidatorTest {

    private final Provisioning provisioningMock = Mockito.mock(Provisioning.class);
    private final Domain domainMock = Mockito.mock(Domain.class);

    @AfterEach
    void tearDown() throws ServiceException {
        Mockito.reset(provisioningMock, domainMock);
    }

    @Test
    public void withoutArguments_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT);
    }

    @Test
    public void withOneArgumentAndWrongAction_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "0",
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.DELETE_ACCOUNT_SUCCEEDED, conditionArguments);
    }

    @Test
    public void withOneNonStringArgument_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                0
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnCondition1_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "0",
                new String[]{AttributeClass.OC_zimbraCalendarResource}
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionSystemProperty_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "0",
                new String[]{},
                Map.of(Provisioning.A_zimbraIsSystemResource, "true")
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionSystemPropertyObjClass_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "0",
                new String[]{},
                Map.of(Provisioning.A_objectClass, new String[] {AttributeClass.OC_zimbraCalendarResource})
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionExternalVirtualAccount_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "0",
                new String[]{},
                Map.of(Provisioning.A_zimbraIsExternalVirtualAccount, "true")
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionNullDomain_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "shouldHaveBeenAnEmailAddress",
                new String[]{}
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionNullDomain2_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(null);
        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionMaxAccountLimitNull_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn(null);
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionMaxAccountLimitZero_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn("0");
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withMoreArgumentsAndReturnConditionMaxAccountLimitValid_validateAccountCreation_returnsWithoutFailing() throws ServiceException {
        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn("3");
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);
        Mockito.when(provisioningMock.countObjects(CountObjectsType.internalUserAccount, domainMock)).thenReturn(1L);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments);
    }

    @Test
    public void withValidArgumentsAndProvisionExceptionTimeout_validateAccountCreation_throwsExpectedException() throws ServiceException {
        final String expectedExceptionMessage = "system failure: The directory may not be responding or is responding slowly.  The directory may" +
                " need tuning or the LDAP read timeout may need to be raised.  Otherwise," +
                " removing the zimbraDomainMaxAccounts restriction will avoid this check.";
        final Throwable expectedCause = new Throwable("timeout");
        final ServiceException expectedException = ServiceException.FAILURE("", expectedCause);

        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn("3");
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);
        Mockito.when(provisioningMock.countObjects(CountObjectsType.internalUserAccount, domainMock)).thenThrow(expectedException);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        final ServiceException actualException = Assertions.assertThrows(ServiceException.class, () -> validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments));

        Assertions.assertEquals(expectedExceptionMessage, actualException.getMessage());
    }

    @Test
    public void withValidArgumentsAndProvisionExceptionCounting_validateAccountCreation_throwsExpectedException() throws ServiceException {
        final String expectedMaxAccounts = "3";
        final String expectedDomainName = "domain.test";
        final String expectedExceptionMessage = "system failure: Unable to count users for setting zimbraDomainMaxAccounts=" + expectedMaxAccounts + " in domain " + expectedDomainName;
        final Throwable expectedCause = new Throwable("other");
        final ServiceException expectedException = ServiceException.FAILURE("", expectedCause);

        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn(expectedMaxAccounts);
        Mockito.when(domainMock.getName()).thenReturn(expectedDomainName);
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);
        Mockito.when(provisioningMock.countObjects(CountObjectsType.internalUserAccount, domainMock)).thenThrow(expectedException);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        final ServiceException actualException = Assertions.assertThrows(ServiceException.class, () -> validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments));

        Assertions.assertEquals(expectedExceptionMessage, actualException.getMessage());
    }

    @Test
    public void withValidArgumentsAndProvisionExceptionCountingNullCauseMessage_validateAccountCreation_throwsExpectedException() throws ServiceException {
        final String expectedMaxAccounts = "3";
        final String expectedDomainName = "domain.test";
        final String expectedExceptionMessage = "system failure: Unable to count users for setting zimbraDomainMaxAccounts=" + expectedMaxAccounts + " in domain " + expectedDomainName;
        final Throwable expectedCause = new Throwable((String) null);
        final ServiceException expectedException = ServiceException.FAILURE("", expectedCause);

        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn(expectedMaxAccounts);
        Mockito.when(domainMock.getName()).thenReturn(expectedDomainName);
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);
        Mockito.when(provisioningMock.countObjects(CountObjectsType.internalUserAccount, domainMock)).thenThrow(expectedException);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@domain.test",
                new String[]{}
        };

        final ServiceException actualException = Assertions.assertThrows(ServiceException.class, () -> validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments));

        Assertions.assertEquals(expectedExceptionMessage, actualException.getMessage());
    }

    @Test
    public void withValidArgumentsAndMaxAccountLessThenLastUserCount_validateAccountCreation_throwsExpectedException() throws ServiceException {
        final String expectedMaxAccounts = "3";
        final long expectedObjectCounted = Long.parseLong(expectedMaxAccounts) + 1;
        final String expectedDomainName = "domain.test";
        final String expectedExceptionMessage = "number of accounts reached the limit: domain=" + expectedDomainName + " (" + expectedMaxAccounts + ")";

        Mockito.when(domainMock.getAttr(Provisioning.A_zimbraDomainMaxAccounts)).thenReturn(expectedMaxAccounts);
        Mockito.when(provisioningMock.get(Mockito.any(Key.DomainBy.class), Mockito.anyString())).thenReturn(domainMock);
        Mockito.when(provisioningMock.countObjects(CountObjectsType.internalUserAccount, domainMock)).thenReturn(expectedObjectCounted);

        final Validators.DomainAccountValidator validator = new Validators.DomainAccountValidator();
        final Object[] conditionArguments = new Object[]{
                "user@" + expectedDomainName,
                new String[]{}
        };

        final ServiceException actualException = Assertions.assertThrows(ServiceException.class, () -> validator.validate(provisioningMock, Provisioning.ProvisioningValidator.CREATE_ACCOUNT, conditionArguments));

        Assertions.assertEquals(expectedExceptionMessage, actualException.getMessage());
    }

}