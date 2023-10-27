package com.zimbra.cs.service.admin;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.CreateAccountRequest;
import com.zimbra.soap.admin.message.CreateAccountResponse;
import com.zimbra.soap.admin.type.AccountInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CreateAccountTest {

    private static Provisioning provisioning;

    @BeforeEach
    void setUp() throws Exception {
        MailboxTestUtil.setUp();
        provisioning = Provisioning.getInstance();
    }

    @AfterEach
    void tearDown() throws ServiceException {
        MailboxTestUtil.tearDown();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0"})
    @NullSource
    void whenDomainMaxAccountIsNullOrZero_creatingANewAccount_willCompleteTheOperationSuccessfully(String maxAccount) throws Exception {
        final String domainName = provisionDomain("test.domain.com", maxAccount);
        final String expectedAccountName = "testName@" + domainName;
        final Map<String, Object> context = provisionAdminContext();

        final CreateAccount creator = new CreateAccount();
        final CreateAccountRequest createAccountRequest = new CreateAccountRequest(expectedAccountName, "superSecretAccountPassword");
        final Element request = JaxbUtil.jaxbToElement(createAccountRequest);


        final Element createAccountResponseXML = creator.handle(request, context);

        final CreateAccountResponse createAccountResponse = JaxbUtil.elementToJaxb(createAccountResponseXML);
        final AccountInfo accountInfo = createAccountResponse.getAccount();
        assertNotNull(accountInfo);
        assertNotEquals(expectedAccountName, accountInfo.getName(), "Account name incorrect");
        assertEquals(expectedAccountName.toLowerCase(), accountInfo.getName(), "Account name incorrect");
    }

    @Test
    void whenDomainMaxAccountIsReached_creatingANewAccount_willThrowKnownException() throws Exception {
        final String domainName = provisionDomain("test.domain.com", "1");
        final String expectedAccountName = "testName@" + domainName;
        final Map<String, Object> context = provisionAdminContext();

        final CreateAccount creator = new CreateAccount();
        final CreateAccountRequest createAccountRequest = new CreateAccountRequest(expectedAccountName, "superSecretAccountPassword");
        final Element request = JaxbUtil.jaxbToElement(createAccountRequest);

        final AccountServiceException actualException = Assertions.assertThrows(AccountServiceException.class, () -> creator.handle(request, context));

        assertEquals("number of accounts reached the limit: domain=test.domain.com (1)", actualException.getMessage());
    }

    private String provisionDomain(String domain, String domainMaxAccounts) throws ServiceException {
        final Map<String, Object> extraAttr = new HashMap<>();
        if (domainMaxAccounts != null) {
            extraAttr.put(Provisioning.A_zimbraDomainMaxAccounts, domainMaxAccounts);
        }
        provisioning.createDomain(domain, extraAttr);
        return domain;
    }

    private Map<String, Object> provisionAdminContext() throws Exception {
        final Map<String, Object> adminExtraAttr = new HashMap<>();
        adminExtraAttr.put(Provisioning.A_zimbraIsAdminAccount, "TRUE");
        adminExtraAttr.put(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME);
        final Account adminAccount = provisioning.createAccount(
                "admin@test.domain.com",
                "superSecretAdminPassword",
                adminExtraAttr
        );
        return new HashMap<>(ServiceTestUtil.getRequestContext(adminAccount));
    }

}