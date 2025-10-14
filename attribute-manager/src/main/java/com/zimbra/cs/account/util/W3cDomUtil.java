// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.zimbra.cs.account.AttributeManagerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dom4j.io.DOMReader;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class W3cDomUtil {

	private W3cDomUtil() {
	}

	/**
	 * Cache one DocumentBuilder per thread to avoid unnecessarily recreating them for every XML
	 * parse.
	 */
	private static final ThreadLocal<DocumentBuilder> w3DomBuilderTL = ThreadLocal.withInitial(() -> {
		try {
			DocumentBuilderFactory dbf = makeDocumentBuilderFactory();
			return dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			return null;
		}
	});

	public static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
	public static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	public static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
	public static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	public static DocumentBuilderFactory makeDocumentBuilderFactory()
			throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setIgnoringComments(true);
		// protect against recursive entity expansion DOS attack and perhaps other things
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		// XXE attack prevention
		dbf.setFeature(DISALLOW_DOCTYPE_DECL, true);
		dbf.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
		dbf.setFeature(LOAD_EXTERNAL_DTD, false);
		dbf.setXIncludeAware(false);
		dbf.setExpandEntityReferences(false);
		dbf.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
		return dbf;
	}

	public static DocumentBuilder getBuilder() {
		return w3DomBuilderTL.get();
	}


	public static ByteArrayInputStream removeInvalidXMLChars(byte[] bytes) {
		String xml = new String(bytes);
		Pattern xmlInvalidChars = Pattern.compile("\\&\\#(?:x([0-9a-fA-F]+)|([0-9]+))\\;");
		xml = xmlInvalidChars.matcher(xml).replaceAll("");
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
		return is;
	}

	public static Document parseXMLToDoc(InputStream is)
			throws AttributeManagerException {
		byte[] bytes = null;
		ByteArrayInputStream bais = null;
		DocumentBuilder jaxbBuilder = getBuilder();
		jaxbBuilder.reset();
		jaxbBuilder.setErrorHandler(new JAXPErrorHandler());
		try {
			bytes = ByteStreams.toByteArray(is);
			bais = new ByteArrayInputStream(bytes);
			return jaxbBuilder.parse(bais);
		} catch (SAXException | IOException e) {
			if (e.getMessage().contains("invalid XML character")) {
				try {
					bais = removeInvalidXMLChars(bytes);
					return jaxbBuilder.parse(bais);
				} catch (SAXException | IOException ex) {
					throw new AttributeManagerException(ex.getMessage());
				}
			} else {
				throw new AttributeManagerException(e.getMessage());
			}
		} finally {
			Closeables.closeQuietly(bais);
		}
	}

	/**
	 * Note: DOCTYPE is disallowed for reasons of security and protection against denial of service
	 */
	public static org.dom4j.Document parseXMLToDom4jDocUsingSecureProcessing(InputStream is)
			throws AttributeManagerException {
		Document w3cDoc = W3cDomUtil.parseXMLToDoc(is);
		DOMReader reader = new DOMReader();
		return reader.read(w3cDoc);
	}

	// Error handler to report errors and warnings
	public static class JAXPErrorHandler implements ErrorHandler {

		JAXPErrorHandler() {
		}

		/**
		 * Returns a string describing parse exception details
		 */
		private String getParseExceptionInfo(String category, SAXParseException spe) {
			return String.format("%s: Problem on line %d of document : %s",
					category, spe.getLineNumber(), spe.getMessage());
		}

		@Override
		public void warning(SAXParseException spe) throws SAXException {
		}

		@Override
		public void error(SAXParseException spe) throws SAXException {
			throw new SAXException(getParseExceptionInfo("Error", spe));
		}

		@Override
		public void fatalError(SAXParseException spe) throws SAXException {
			throw new SAXException(getParseExceptionInfo("Fatal Error", spe));
		}
	}

}
