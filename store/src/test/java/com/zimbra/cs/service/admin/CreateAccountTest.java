package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CreateAccountTest extends MailboxTestSuite {

    private static Provisioning provisioning;

    @BeforeAll
    static void setUp() throws Exception {
        provisioning = Provisioning.getInstance();
    }

    @AfterEach
    void clearData() throws Exception {
        MailboxTestUtil.clearData();
    }

    @BeforeEach
    void initData() throws Exception {
        MailboxTestUtil.initData();
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

    @Test
    void create_account_should_throw_service_exception_if_zimbraId_invalid() throws Exception {
        final String domainName = provisionDomain("test.domain.com", "2");
        final String expectedAccountName = "testName@" + domainName;
        final Map<String, Object> context = provisionAdminContext();

        final CreateAccount creator = new CreateAccount();
        final CreateAccountRequest createAccountRequest = new CreateAccountRequest(expectedAccountName, "superSecretAccountPassword");
        Map<String, Object> map = Map.of(Provisioning.A_zimbraId, "invalid");
        createAccountRequest.setAttrs(map);
        final Element request = JaxbUtil.jaxbToElement(createAccountRequest);

        final ServiceException actualException = Assertions.assertThrows(ServiceException.class, () -> creator.handle(request, context));

        assertEquals("invalid request: invalid is not a valid UUID", actualException.getMessage());

    }

    @Test
    void should_create_account_when_passed_zimbraId_is_valid() throws Exception {
        final String domainName = provisionDomain("test.domain.com", "2");
        final String expectedAccountName = "testName@" + domainName;
        final Map<String, Object> context = provisionAdminContext();

        final CreateAccount creator = new CreateAccount();
        final CreateAccountRequest createAccountRequest = new CreateAccountRequest(expectedAccountName, "superSecretAccountPassword");
        String zimbraId = UUID.randomUUID().toString();
        Map<String, Object> map = Map.of(Provisioning.A_zimbraId, zimbraId);
        createAccountRequest.setAttrs(map);
        final Element request = JaxbUtil.jaxbToElement(createAccountRequest);
        final Element createAccountResponseXML = creator.handle(request, context);

        final CreateAccountResponse createAccountResponse = JaxbUtil.elementToJaxb(createAccountResponseXML);
        assert createAccountResponse != null;
        final AccountInfo accountInfo = createAccountResponse.getAccount();
        assertEquals(zimbraId, accountInfo.getId());
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
        adminExtraAttr.put(Provisioning.A_zimbraMailHost, mailboxTestExtension.getServerName());
        final Account adminAccount = provisioning.createAccount(
                "admin@test.domain.com",
                "superSecretAdminPassword",
                adminExtraAttr
        );
        return new HashMap<>(ServiceTestUtil.getRequestContext(adminAccount));
    }

}