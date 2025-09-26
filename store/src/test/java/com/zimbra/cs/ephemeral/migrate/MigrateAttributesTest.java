// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ephemeral.migrate;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralInput.AbsoluteExpiration;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralResult;
import com.zimbra.cs.ephemeral.EphemeralStore;
import com.zimbra.cs.ephemeral.LdapEntryLocation;
import com.zimbra.cs.ephemeral.migrate.AttributeMigration.EntrySource;
import com.zimbra.cs.ephemeral.migrate.AttributeMigration.MigrationCallback;
import com.zimbra.cs.ephemeral.migrate.AttributeMigration.MigrationTask;
import com.zimbra.cs.ephemeral.migrate.MigrationInfo.Factory;
import com.zimbra.cs.ephemeral.migrate.MigrationInfo.Status;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class MigrateAttributesTest extends MailboxTestSuite {

	public String testName;

	private static final Map<String, AttributeConverter> converters = new HashMap<String, AttributeConverter>();

	static {
		converters.put(Provisioning.A_zimbraAuthTokens, new AuthTokenConverter());
		converters.put(Provisioning.A_zimbraCsrfTokenData, new CsrfTokenConverter());
		converters.put(Provisioning.A_zimbraLastLogonTimestamp, new StringAttributeConverter());
	}

	private static Account acct;

	private static final String lastLogon = new SimpleDateFormat("yyyyMMddHHmmss'Z'").format(
			new Date());
	private static final int millis = 100000;

	@BeforeAll
	public static void setUp() throws Exception {
		acct = createAccount().create();
		acct.addAuthTokens("1234", "server_1", new AbsoluteExpiration(millis));
		acct.addAuthTokens("5678", "server_2", new AbsoluteExpiration(millis));
		acct.addCsrfTokenData("crumb1", "data1", new AbsoluteExpiration(millis));
		acct.addCsrfTokenData("crumb2", "data2", new AbsoluteExpiration(millis));
		acct.setLastLogonTimestampAsString(lastLogon);
	}

	@BeforeEach
	public void init(TestInfo testInfo) throws Exception {
		Optional<Method> testMethod = testInfo.getTestMethod();
		if (testMethod.isPresent()) {
			this.testName = testMethod.get().getName();
		}
		System.out.println(testName);
	}

	/*
	 * Test the individual converters
	 */
	@Test
	void testConverters() {
		EphemeralInput input = runConverter(Provisioning.A_zimbraAuthTokens, new AuthTokenConverter());
		verifyAuthTokenEphemeralInput(input, "1234", "server_1", 100000L);

		input = runConverter(Provisioning.A_zimbraCsrfTokenData, new CsrfTokenConverter());
		verifyCsrfTokenEphemeralInput(input, "crumb1", "data1", 100000L);

		input = runConverter(Provisioning.A_zimbraLastLogonTimestamp, new StringAttributeConverter());
		verifyLastLogonTimestampEphemeralInput(input, lastLogon);
	}

	private EphemeralInput runConverter(String attrName, AttributeConverter converter) {
		String value = acct.getAttr(attrName, false, true);
		return converter.convert(attrName, value);
	}

	/*
	 * Test MigrationTask for auth tokens
	 */
	@Test
	void testAuthTokenMigrationTask() throws ServiceException {
		List<EphemeralInput> results = new LinkedList<EphemeralInput>();
		Map<String, AttributeConverter> converters = new HashMap<String, AttributeConverter>();
		converters.put(Provisioning.A_zimbraAuthTokens, new AuthTokenConverter());
		MigrationTask task = new MigrationTask(acct, converters, new DummyMigrationCallback(results));
		task.migrateAttributes();
		assertEquals(2, results.size());
		verifyAuthTokenEphemeralInput(results.get(0), "1234", "server_1", 100000L);
		verifyAuthTokenEphemeralInput(results.get(1), "5678", "server_2", 100000L);
	}

	/*
	 * Test MigrationTask for CSRF tokens
	 */
	@Test
	void testCsrfTokenMigrationTask() throws ServiceException {
		List<EphemeralInput> results = new LinkedList<EphemeralInput>();
		Map<String, AttributeConverter> converters = new HashMap<String, AttributeConverter>();
		converters.put(Provisioning.A_zimbraCsrfTokenData, new CsrfTokenConverter());
		MigrationTask task = new MigrationTask(acct, converters, new DummyMigrationCallback(results));
		task.migrateAttributes();
		assertEquals(2, results.size());
		verifyCsrfTokenEphemeralInput(results.get(0), "crumb1", "data1", 100000L);
		verifyCsrfTokenEphemeralInput(results.get(1), "crumb2", "data2", 100000L);
	}

	/*
	 * Test MigrationTask for last login timestamp
	 */
	@Test
	void testLastLogonTimestampMigrationTask() throws ServiceException {
		List<EphemeralInput> results = new LinkedList<EphemeralInput>();
		Map<String, AttributeConverter> converters = new HashMap<String, AttributeConverter>();
		converters.put(Provisioning.A_zimbraLastLogonTimestamp, new StringAttributeConverter());
		MigrationTask task = new MigrationTask(acct, converters, new DummyMigrationCallback(results));
		task.migrateAttributes();
		assertEquals(1, results.size());
		verifyLastLogonTimestampEphemeralInput(results.get(0), lastLogon);
	}


	@Test
	@Disabled("Check me.")
	void testMigrationInfo() throws Exception {
		MigrationInfo info = MigrationInfo.getFactory().getInfo();
		try {
			EphemeralStore destination = EphemeralStore.getFactory().getStore();
			EntrySource source = new DummyEntrySource(acct);
			List<String> attrsToMigrate = new ArrayList<String>();
			MigrationCallback callback = new DummyMigrationCallback(destination);
			AttributeMigration.setCallback(callback);
			AttributeMigration migration = new AttributeMigration("testAttributeMigration",
					attrsToMigrate, source, null);
			assertEquals(Status.NONE, info.getStatus());
			migration.beginMigration();
			assertEquals(Status.IN_PROGRESS, info.getStatus());
			migration.endMigration();
			assertEquals(Status.COMPLETED, info.getStatus());
		} finally {
			info.clearData();
		}
	}

	/*
	 * Test end-to-end AttributeMigration
	 */
	@Test
	void testAttributeMigration() throws Exception {
		EphemeralStore destination = EphemeralStore.getFactory().getStore();
		EntrySource source = new DummyEntrySource(acct);

		List<String> attrsToMigrate = Arrays.asList(new String[]{
				Provisioning.A_zimbraAuthTokens,
				Provisioning.A_zimbraCsrfTokenData,
				Provisioning.A_zimbraLastLogonTimestamp});

		//DummyMigrationCallback will store attributes in InMemoryEphemeralStore, and track deletions in deletedAttrs map
		MigrationCallback callback = new DummyMigrationCallback(destination);
		AttributeMigration.setCallback(callback);
		AttributeMigration migration = new AttributeMigration("testAttributeMigration", attrsToMigrate,
				source, null);
		final Factory factory = MigrationInfo.getFactory();
		MigrationInfo info = factory.getInfo();

		//disable running in separate thread
		//run migration
		migration.migrateAllAccounts();
		EphemeralLocation location = new LdapEntryLocation(acct);
		EphemeralResult result = destination.get(
				new EphemeralKey(Provisioning.A_zimbraAuthTokens, "1234"), location);
		assertEquals("server_1", result.getValue());
		result = destination.get(new EphemeralKey(Provisioning.A_zimbraAuthTokens, "5678"), location);
		assertEquals("server_2", result.getValue());
		result = destination.get(new EphemeralKey(Provisioning.A_zimbraCsrfTokenData, "crumb1"),
				location);
		assertEquals("data1", result.getValue());
		result = destination.get(new EphemeralKey(Provisioning.A_zimbraCsrfTokenData, "crumb2"),
				location);
		assertEquals("data2", result.getValue());
		result = destination.get(new EphemeralKey(Provisioning.A_zimbraLastLogonTimestamp), location);
		assertEquals(lastLogon, result.getValue());
		assertEquals(Status.COMPLETED, factory.getInfo().getStatus());
	}

	/**
	 * This test breaks all the others when run before for some reason, that's why it has a Z. So I
	 * chose to run test in alphabetical order. It is an issue and probably masking a bigger issue of
	 * the underlying code.
	 *
	 * @throws Exception
	 */
	@Test
	void testZErrorDuringMigration() throws Exception {
		List<EphemeralInput> results = new LinkedList<EphemeralInput>();
		EntrySource source = new DummyEntrySource(acct, acct, acct);

		List<String> attrsToMigrate = Arrays.asList(new String[]{
				Provisioning.A_zimbraAuthTokens,
				Provisioning.A_zimbraCsrfTokenData,
				Provisioning.A_zimbraLastLogonTimestamp});

		DummyMigrationCallback callback = new DummyMigrationCallback(results);
		callback.throwErrorDuringMigration = true;
		AttributeMigration.setCallback(callback);
		AttributeMigration migration = new AttributeMigration("testAttributeMigration", attrsToMigrate,
				source, null);
		final Factory factory = MigrationInfo.getFactory();
		MigrationInfo info = factory.getInfo();
		info.clearData();
		try {
			migration.migrateAllAccounts();
			fail("synchronous migration should throw an exception");
		} catch (ServiceException e) {
			//make sure the root exception got thrown
			assertTrue(e.getMessage().contains("Failure during migration"));
		}
		assertEquals(0, results.size()); //make sure nothing got migrated
		migration = new AttributeMigration("testAttributeMigration", attrsToMigrate, source, 3);
		info.clearData();
		try {
			migration.migrateAllAccounts();
			fail("async migration should throw an exception");
		} catch (ServiceException e) {
			assertTrue(e.getMessage().contains("Failure during migration"));
			assertEquals(Status.FAILED, factory.getInfo().getStatus());
		}
		assertEquals(0, results.size());
	}

	@Test
	void testMigrateAlreadyMigratedAccount() throws Exception {
		//create a new account that will not have any data to migrate
		Account acct = createAccount().create();
		EntrySource source = new DummyEntrySource(acct);
		Multimap<String, Object> deletedAttrs = LinkedListMultimap.create();
		List<EphemeralInput> results = new LinkedList<EphemeralInput>();
		List<String> attrsToMigrate = Arrays.asList(new String[]{
				Provisioning.A_zimbraAuthTokens,
				Provisioning.A_zimbraCsrfTokenData,
				Provisioning.A_zimbraLastLogonTimestamp});

		DummyMigrationCallback callback = new DummyMigrationCallback(results);
		callback.throwErrorDuringMigration = false;
		AttributeMigration.setCallback(callback);
		AttributeMigration migration = new AttributeMigration("testAttributeMigration", attrsToMigrate,
				source, null);
		migration.migrateAllAccounts();
		assertTrue(results.isEmpty());
	}

	private void verifyAuthTokenEphemeralInput(EphemeralInput input, String token,
			String serverVersion, Long expiration) {
		EphemeralKey key = input.getEphemeralKey();
		assertEquals(Provisioning.A_zimbraAuthTokens, key.getKey());
		assertEquals(token, key.getDynamicComponent());
		assertEquals(serverVersion, input.getValue());
		assertEquals(expiration, input.getExpiration());
	}

	private void verifyCsrfTokenEphemeralInput(EphemeralInput input, String crumb, String data,
			Long expiration) {
		EphemeralKey key = input.getEphemeralKey();
		assertEquals(Provisioning.A_zimbraCsrfTokenData, key.getKey());
		assertEquals(crumb, key.getDynamicComponent());
		assertEquals(data, input.getValue());
		assertEquals(expiration, input.getExpiration());
	}

	private void verifyLastLogonTimestampEphemeralInput(EphemeralInput input, String expected) {
		EphemeralKey key = input.getEphemeralKey();
		assertEquals(Provisioning.A_zimbraLastLogonTimestamp, key.getKey());
		assertNull(key.getDynamicComponent());
		assertEquals(lastLogon, input.getValue());
	}

	public static class DummyMigrationCallback implements AttributeMigration.MigrationCallback {

		private List<EphemeralInput> trackedInputs;
		private EphemeralStore store = null;
		private boolean throwErrorDuringMigration = false;

		// for end-to-end testing with InMemoryEphemeralStore
		DummyMigrationCallback(EphemeralStore store) {
			this.store = store;
		}

		//for testing outputs of AttributeConverters
		DummyMigrationCallback(List<EphemeralInput> inputs) {
			this.trackedInputs = inputs;
		}

		@Override
		public void setEphemeralData(EphemeralInput input,
				EphemeralLocation location, String origKey, Object origValue) throws ServiceException {
			if (throwErrorDuringMigration) {
				throw ServiceException.FAILURE("error during migration", null);
			} else {
				if (trackedInputs != null) {
					trackedInputs.add(input);
				}
				if (store != null) {
					store.update(input, location);
				}
			}
		}

		@Override
		public EphemeralStore getStore() {
			return store;
		}

		@Override
		public boolean disableCreatingReports() {
			return true;
		}

		@Override
		public void flushCache() {
		}
	}

	public class DummyEntrySource implements EntrySource {

		List<NamedEntry> entries;

		public DummyEntrySource(Account... accts) {
			entries = new ArrayList<NamedEntry>(accts.length);
			entries.addAll(Arrays.asList(accts));
		}

		@Override
		public List<NamedEntry> getEntries() throws ServiceException {
			return entries;
		}
	}

}
