package com.zimbra.cs.account;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.service.ServiceException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttributeManagerUtilTest {

	@TempDir
	private static File testDir;

	@Test
	void shouldGenerateZAttrConfig() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrConfig.java");
		attributeManagerUtil.generateGetters("globalConfig", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrConfig extends Entry {"), output);
	}


	@Test
	void shouldGenerateZAttrCos() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrCos.java");
		attributeManagerUtil.generateGetters("cos", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrCos extends NamedEntry {"), output);
	}

	@Test
	void shouldGenerateZAttrAccount() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrAccount.java");
		attributeManagerUtil.generateGetters("account", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrAccount extends MailTarget {"), output);
	}

	@Test
	void shouldGenerateZAttrServer() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrServer.java");
		attributeManagerUtil.generateGetters("server", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrServer extends NamedEntry {"), output);
	}

	@Test
	void shouldGenerateZAttrDistributionList() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrDistributionList.java");
		attributeManagerUtil.generateGetters("distributionList", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrDistributionList extends Group {"), output);
	}

	@Test
	void shouldGenerateZAttrDynamicGroup() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrDynamicGroup.java");
		attributeManagerUtil.generateGetters("group", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrDynamicGroup extends Group {"), output);
	}

	@Test
	void shouldGenerateZAttrShareLocator() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrShareLocator.java");
		attributeManagerUtil.generateGetters("shareLocator", path.toString());
		final String output = Files.readString(path);
		assertTrue(output.contains("public abstract class ZAttrShareLocator extends NamedEntry {"), output);
	}

	@Test
	void shouldGenerateZAttrProvisioning() throws ServiceException, IOException {
		final AttributeManagerUtil attributeManagerUtil = AttributeManagerUtil.get();
		final Path path = Paths.get(testDir.toURI().getPath(), "ZAttrProvisioning.java");
		attributeManagerUtil.generateProvisioningConstants(path.toString());
		final String output = Files.readString(path);
		assertTrue( output.contains("public class ZAttrProvisioning {"), output);
	}
}