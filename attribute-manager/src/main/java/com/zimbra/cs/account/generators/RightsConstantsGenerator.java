package com.zimbra.cs.account.generators;

import com.zimbra.cs.account.AttributeManagerException;
import com.zimbra.cs.account.FileGenUtil;
import com.zimbra.cs.account.util.StringUtil;
import com.zimbra.cs.account.util.W3cDomUtil;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class RightsConstantsGenerator {

	private static final String E_RIGHTS = "rights";
	private static final String E_RIGHT = "right";
	private static final String A_NAME = "name";
	private static final String A_USER_RIGHT = "userRight";
	public static List<String> RIGHTS_FILES = List.of(
			"/conf/rights/adminconsole-ui.xml",
			"/conf/rights/rights.xml",
			"/conf/rights/rights-roles.xml",
			"/conf/rights/user-rights.xml",
			"/conf/rights/rights-adminconsole.xml",
			"/conf/rights/rights-adminconsole-domainadmin.xml",
			"/conf/rights/rights-domainadmin.xml"
	);
	private final UserAdminRights rights;

	private RightsConstantsGenerator(UserAdminRights userAdminRights) {
		this.rights = userAdminRights;
	}

	public record UserAdminRights(List<RightName> adminRights, List<RightName> userRights) {

	}

	private static UserAdminRights loadRightsFromFile(InputStream fileContent)
			throws Exception {
		Document doc = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(fileContent);
		Element root = doc.getRootElement();
		if (!root.getName().equals(E_RIGHTS)) {
			throw new AttributeManagerException("root tag is not " + E_RIGHTS);
		}
		final ArrayList<RightName> adminRights = new ArrayList<>();
		final ArrayList<RightName> userRights = new ArrayList<>();
		for (Iterator<Element> iter = root.elementIterator(); iter.hasNext(); ) {
			Element elem = iter.next();
			if (elem.getName().equals(E_RIGHT)) {
				String name = elem.attributeValue(A_NAME);
				if (name == null) {
					throw new AttributeManagerException("no name specified");
				}
				// TODO: add desc
				var right = new RightName(name, "");
				boolean userRight = getBooleanAttr(elem, A_USER_RIGHT);
				if (userRight) {
					userRights.add(right);
				} else {
					adminRights.add(right);
				}
			}
		}
		return new UserAdminRights(adminRights, userRights);
	}

	public static RightsConstantsGenerator getInstance() throws Exception {
		List<RightName> adminRights = new ArrayList<>();
		List<RightName> userRights = new ArrayList<>();
		for (String rightsFile : RIGHTS_FILES) {
			final InputStream resourceAsStream = RightsConstantsGenerator.class.getResourceAsStream(
					rightsFile);
			final UserAdminRights fileRights = loadRightsFromFile(resourceAsStream);
			adminRights.addAll(fileRights.adminRights);
			userRights.addAll(fileRights.userRights);
		}
		return new RightsConstantsGenerator(new UserAdminRights(adminRights, userRights));
	}


	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new GnuParser();
		final RightsConstantsGenerator generator = RightsConstantsGenerator.getInstance();
		final Options sOptions = new Options();
		sOptions.addOption("o", "output", true,
				"Output directory of generated classes");
		sOptions.addOption("a", "action", true,
				"Action to perform. Supports: genMessageProperties. If not provided generates java files.");
		CommandLine cl = parser.parse(sOptions, args);
		final String output = cl.getOptionValue("o");
		final String action = cl.getOptionValue("a");

		final String genRightConstsJava = generator.genRightConstsJava();
		final String genUserRightsJava = generator.genUserRights();
		final String genAdminRightsJava = generator.genAdminRightsJava();
		if (Objects.equals(action, "genMessageProperties")) {
			final Path basePath = Paths.get(output, "conf/msgs");
			Files.createDirectories(basePath);
			final String messageProperties = generator.genMessageProperties();
			Files.write(basePath.resolve("ZsMsgRights.properties"), messageProperties.getBytes());
			return;
		}
		if (Objects.isNull(output)) {
			System.out.println("Rights constants");
			System.out.println(genRightConstsJava);
			System.out.println("User rights");
			System.out.println(genUserRightsJava);
			System.out.println("Admin rights");
			System.out.println(genAdminRightsJava);
		} else {
			Path basePath = Paths.get(output, "com/zimbra/cs/account/accesscontrol/generated/");
			Files.createDirectories(basePath);
			Files.write(basePath.resolve("UserRights.java"), genUserRightsJava.getBytes());
			Files.write(basePath.resolve("AdminRights.java"), genAdminRightsJava.getBytes());
			Files.write(basePath.resolve("RightConsts.java"), genRightConstsJava.getBytes());
		}
	}

	private static boolean getBoolean(String value) throws AttributeManagerException {
		if ("1".equals(value)) {
			return true;
		} else if ("0".equals(value)) {
			return false;
		} else {
			throw new AttributeManagerException("invalid value:" + value);
		}
	}

	private static boolean getBooleanAttr(Element elem, String attr)
			throws AttributeManagerException {
		String value = elem.attributeValue(attr);
		if (value == null) {
			return false;
		}
		return getBoolean(value);
	}

	public String genRightConstsJava() {
		StringBuilder sb = new StringBuilder();
		sb.append("package com.zimbra.cs.account.accesscontrol.generated;\n");
		sb.append("public class RightConsts {");
		sb.append("\n\n");
		sb.append("    /*\n");
		sb.append("    ============\n");
		sb.append("    user rights:\n");
		sb.append("    ============\n");
		sb.append("    */\n\n");
		for (RightName ur : rights.userRights()) {
			genRightConst(ur, sb);
		}

		sb.append("\n\n");
		sb.append("    /*\n");
		sb.append("    =============\n");
		sb.append("    admin rights:\n");
		sb.append("    =============\n");
		sb.append("    */\n\n");
		for (RightName ar : rights.adminRights()) {
			genRightConst(ar, sb);
		}
		sb.append("}\n");
		return sb.toString();
	}

	public String genAdminRightsJava() {
		StringBuilder sb = new StringBuilder();
		sb.append("package com.zimbra.cs.account.accesscontrol.generated;\n");
		sb.append("import com.zimbra.common.service.ServiceException;\n");
		sb.append("import com.zimbra.cs.account.accesscontrol.AdminRight;\n");
		sb.append("import com.zimbra.cs.account.accesscontrol.Right;\n");
		sb.append("import com.zimbra.cs.account.accesscontrol.RightManager;\n");
		sb.append("public class AdminRights {");
		sb.append("\n\n");
		for (RightName r : rights.adminRights()) {
			sb.append("    public static AdminRight R_").append(r.getName()).append(";")
					.append("\n");
		}

		sb.append("\n\n");
		sb.append("    public static void init(RightManager rm) throws ServiceException {\n");
		for (RightName r : rights.adminRights()) {
			String s = String.format("        R_%-36s = rm.getAdminRight(Right.RT_%s);\n",
					r.getName(), r.getName());
			sb.append(s);
		}
		sb.append("    }\n");
		sb.append("}\n");
		return sb.toString();
	}

	public String genUserRights() {
		StringBuilder sb = new StringBuilder();
		sb.append("package com.zimbra.cs.account.accesscontrol.generated;\n");
		sb.append("import com.zimbra.common.service.ServiceException;\n");
		sb.append("import com.zimbra.cs.account.accesscontrol.Right;\n");
		sb.append("import com.zimbra.cs.account.accesscontrol.RightManager;\n");
		sb.append("import com.zimbra.cs.account.accesscontrol.UserRight;\n");
		sb.append("public class UserRights {");
		sb.append("\n\n");
		for (RightName r : rights.userRights()) {
			sb.append("    public static UserRight R_").append(r.getName()).append(";")
					.append("\n");
		}

		sb.append("\n\n");
		sb.append("    public static void init(RightManager rm) throws ServiceException {\n");
		for (RightName r : rights.userRights()) {
			String s = String.format("        R_%-36s = rm.getUserRight(Right.RT_%s);\n",
					r.getName(), r.getName());
			sb.append(s);
		}
		sb.append("    }\n");
		sb.append("}");
		return sb.toString();
	}

	private void genRightConst(RightName r, StringBuilder sb) {
		sb.append("\n    /**\n");
		if (r.getDesc() != null) {
			sb.append(FileGenUtil.wrapComments(StringUtil.escapeHtml(r.getDesc()), 70, "     * "));
			sb.append("\n");
		}
		sb.append("     */\n");
		sb.append("    public static final String RT_").append(r.getName()).append(" = \"")
				.append(r.getName()).append("\";").append("\n");
	}

	private String genMessageProperties() {
		StringBuilder result = new StringBuilder();

		result.append(
				FileGenUtil.genDoNotModifyDisclaimer("#", RightsConstantsGenerator.class.getSimpleName()));
		result.append("# Rights");
		result.append("\n\n");

		genMessageProperties(result, rights.userRights());
		result.append("\n\n");
		genMessageProperties(result, rights.adminRights());

		return result.toString();
	}

	public void genMessageProperties(StringBuilder result, List<RightName> rights) {
		// TODO: sort by name
		for (RightName r : rights) {
			// strip off the 2 spaces on the first line
			String text = FileGenUtil.wrapComments(r.getDesc(), 80, "  ", " \\").substring(2);
			result.append(r.getName()).append(" = ").append(text).append("\n");
		}
	}
}
