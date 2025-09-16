package com.zimbra.cs.account.generators;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.SetUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RightDomainAdminResourceGenerator {
	public String genDomainAdminSetAttrsRights() throws Exception {
		Set<String> acctAttrs = getDomainAdminModifiableAttrs(AttributeClass.account);
		Set<String> crAttrs = getDomainAdminModifiableAttrs(AttributeClass.calendarResource);
		Set<String> dlAttrs = getDomainAdminModifiableAttrs(AttributeClass.distributionList);
		Set<String> domainAttrs = getDomainAdminModifiableAttrs(AttributeClass.domain);

		Set<String> acctAndCrAttrs = SetUtil.intersect(acctAttrs, crAttrs);
		Set<String> acctOnlyAttrs = SetUtil.subtract(acctAttrs, crAttrs);
		Set<String> crOnlyAttrs = SetUtil.subtract(crAttrs, acctAttrs);

		// sanity check, since we are not generating it, make sure it is indeed empty
		if (!acctOnlyAttrs.isEmpty())
			throw ServiceException.FAILURE("account only attrs is not empty???", null);

		String acctAndCrAttrsFiller = genAttrs(acctAndCrAttrs);
		String crOnlyAttrsFiller = genAttrs(crOnlyAttrs);
		String dlAttrsFiller = genAttrs(dlAttrs);
		String domainAttrsFiller = genAttrs(domainAttrs);

		Map<String,String> templateFillers = new HashMap<>();
		templateFillers.put("ACCOUNT_AND_CALENDAR_RESOURCE_ATTRS", acctAndCrAttrsFiller);
		templateFillers.put("CALENDAR_RESOURCE_ATTRS", crOnlyAttrsFiller);
		templateFillers.put("DISTRIBUTION_LIST_ATTRS", dlAttrsFiller);
		templateFillers.put("DOMAIN_ATTRS", domainAttrsFiller);

		final InputStream resourceAsStream = RightDomainAdminResourceGenerator.class.getResourceAsStream("/conf/rights/rights-domainadmin.xml-template");
		String templateString = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);

		return StringUtil.fillTemplate(templateString, templateFillers);
	}

	private static Set<String> getDomainAdminModifiableAttrs(AttributeClass klass)
			throws ServiceException {
		AttributeManager am = AttributeManager.getInstance();
		Set<String> allAttrs = am.getAllAttrsInClass(klass);

		Set<String> domainAdminModifiableAttrs = new HashSet<>();
		for (String attr : allAttrs) {
			if (am.isDomainAdminModifiable(attr, klass)) {
				domainAdminModifiableAttrs.add(attr);
			}
		}
		return domainAdminModifiableAttrs;
	}

	private static String genAttrs(Set<String> attrs) {
		// sort it
		Set<String> sortedAttrs = new TreeSet<>(attrs);

		StringBuilder sb = new StringBuilder();
		for (String attr : sortedAttrs) {
			sb.append("    <a n=\"").append(attr).append("\"/>\n");
		}
		return sb.toString();
	}

	public static void main(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		final Options sOptions = new Options();
		sOptions.addOption("o", "output", true,
				"Output directory of domain admin rights resources");
		CommandLine cl = parser.parse(sOptions, args);
		final String output = cl.getOptionValue("o");
		final Path outputFile = Paths.get(output, "rights-domainadmin.xml");
		try {
			final String content = new RightDomainAdminResourceGenerator().genDomainAdminSetAttrsRights();
			Path basePath = Paths.get(output);
			Files.createDirectories(basePath);
			System.out.println("Writing rights-domainadmin.xml to " + outputFile);
			Files.write(outputFile, content.getBytes());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
