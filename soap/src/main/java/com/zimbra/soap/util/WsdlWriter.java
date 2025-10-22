package com.zimbra.soap.util;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.List;

public class WsdlWriter {

	// Existing fields, constants, methods...

	// Keep your existing helper methods as is!

	// Replace makeWsdlDoc + writeXmlOut method with this streaming version
	public void writeWsdl(OutputStream xmlOut, List<WsdlInfoForNamespace> nsInfos, String serviceName, String targetNamespace)
			throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(xmlOut, "UTF-8");

		writer.writeStartDocument("UTF-8", "1.0");

		// <wsdl:definitions ...>
		writer.writeStartElement("wsdl", "definitions", "http://schemas.xmlsoap.org/wsdl/");
		writer.writeNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		writer.writeNamespace("soap", "http://schemas.xmlsoap.org/wsdl/soap12/");
		writer.writeNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
		writer.writeNamespace("zimbra", "urn:zimbra");
		writer.writeNamespace("svc", targetNamespace);

		writer.writeAttribute("targetNamespace", targetNamespace);
		writer.writeAttribute("name", serviceName);

		// types section
		writeTypesSection(writer, nsInfos);

		// messages section
		writeMessages(writer, nsInfos);

		// call your existing methods to write portTypes, bindings, service, but they must be rewritten
		// to accept XMLStreamWriter instead of DOM4J Document
		// For example:
		// writePortTypes(writer, nsInfos);
		// writeBindings(writer, nsInfos);
		// writeService(writer, serviceName, targetNamespace);

		// close definitions
		writer.writeEndElement();

		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

	private void writeTypesSection(XMLStreamWriter writer, List<WsdlInfoForNamespace> nsInfos) throws XMLStreamException {
		writer.writeStartElement("wsdl", "types", "http://schemas.xmlsoap.org/wsdl/");

		writer.writeStartElement("xsd", "schema", "http://www.w3.org/2001/XMLSchema");

		writer.writeEmptyElement("xsd", "import", "http://www.w3.org/2001/XMLSchema");
		writer.writeAttribute("namespace", "urn:zimbra");
		writer.writeAttribute("schemaLocation", "zimbra.xsd");

		for (WsdlInfoForNamespace nsInfo : nsInfos) {
			writer.writeEmptyElement("xsd", "import", "http://www.w3.org/2001/XMLSchema");
			writer.writeAttribute("namespace", nsInfo.getXsdNamespaceString());
			writer.writeAttribute("schemaLocation", nsInfo.getXsdFilename());
		}

		writer.writeEndElement(); // xsd:schema
		writer.writeEndElement(); // wsdl:types
	}

	private void writeMessages(XMLStreamWriter writer, List<WsdlInfoForNamespace> nsInfos) throws XMLStreamException {
		for (WsdlInfoForNamespace nsInfo : nsInfos) {
			String xsdPrefix = nsInfo.getXsdPrefix();

			for (String reqName : nsInfo.getRequests()) {
				String baseName = reqName.replaceFirst("Request$", "");
				String respName = baseName + "Response";

				String reqMsgName = nsInfo.getTag() + reqName + "Message";
				String respMsgName = nsInfo.getTag() + respName + "Message";

				writeMessage(writer, reqMsgName, xsdPrefix + ":" + reqName);
				writeMessage(writer, respMsgName, xsdPrefix + ":" + respName);
			}
		}

		writer.writeStartElement("wsdl", "message", "http://schemas.xmlsoap.org/wsdl/");
		writer.writeAttribute("name", "soapHdrContext");

		writer.writeEmptyElement("wsdl", "part", "http://schemas.xmlsoap.org/wsdl/");
		writer.writeAttribute("name", "context");
		writer.writeAttribute("element", "zimbra:context");

		writer.writeEndElement(); // wsdl:message
	}

	private void writeMessage(XMLStreamWriter writer, String messageName, String element) throws XMLStreamException {
		writer.writeStartElement("wsdl", "message", "http://schemas.xmlsoap.org/wsdl/");
		writer.writeAttribute("name", messageName);

		writer.writeEmptyElement("wsdl", "part", "http://schemas.xmlsoap.org/wsdl/");
		writer.writeAttribute("name", "params");
		writer.writeAttribute("element", element);

		writer.writeEndElement(); // wsdl:message
	}

	// Leave your existing methods intact
}
