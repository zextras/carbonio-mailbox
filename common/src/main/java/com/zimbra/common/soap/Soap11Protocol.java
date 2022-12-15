// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Soap11Protocol.java
 */

package com.zimbra.common.soap;

import org.dom4j.Namespace;
import org.dom4j.QName;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.ZimbraNamespace;
import com.zimbra.common.util.ExceptionToString;

/**
 * Interface to Soap 1.1 Protocol
 */

class Soap11Protocol extends SoapProtocol {

    private static final String NS_STR =
        "http://schemas.xmlsoap.org/soap/envelope/";
    private static final Namespace NS = Namespace.get(NS_PREFIX, NS_STR);
    // com.sun.xml.internal.ws.fault.SOAP11Fault fails to unmarshall XML response containing a Fault
    // if faultcode is namespace qualified.  i.e. <faultcode>soap:Client</faultcode> is OK
    // but                                        <soap:faultcode>soap:Client</soap:faultcode> is not
    private static final QName FAULTCODE = new QName("faultcode");
    private static final QName FAULTSTRING = new QName("faultstring");
    public static final QName DETAIL = new QName("detail");
    private static final QName SENDER_CODE = new QName("Client", NS);
    private static final QName RECEIVER_CODE = new QName("Server", NS);
    

    /** empty package-private constructor */
    Soap11Protocol() { 
        super();
    }

    public Element.ElementFactory getFactory() {
        return Element.XMLElement.mFactory;
    }

    /**
     * Return the namespace String
     */
    public Namespace getNamespace() {
        return NS;
    }

    /* (non-Javadoc)
     * @see com.zimbra.soap.shared.SoapProtocol#soapFault(org.dom4j.Element)
     */
    public SoapFaultException soapFault(Element fault) {
        if (!isFault(fault))
            return new SoapFaultException("not a soap fault ", fault);
        
        Element code = fault.getOptionalElement(FAULTCODE);
        boolean isReceiversFault = RECEIVER_CODE.equals(code == null ? null : code.getQName());

        String reasonValue;
        Element faultString = fault.getOptionalElement(FAULTSTRING);
        if (faultString != null)
            reasonValue = faultString.getTextTrim();
        else
            reasonValue = "unknown reason";

        Element detail = fault.getOptionalElement(DETAIL);

        return new SoapFaultException(reasonValue, detail, isReceiversFault, fault);
    }

    /* (non-Javadoc)
     * @see com.zimbra.common.soap.SoapProtocol#soapFault(com.zimbra.cs.service.ServiceException)
     */
    public Element soapFault(ServiceException e) {
        String reason = e.getMessage();
        if (reason == null)
            reason = e.toString();

        QName code = e.isReceiversFault() ? RECEIVER_CODE : SENDER_CODE;

        Element eFault = mFactory.createElement(mFaultQName);
        eFault.addUniqueElement(FAULTCODE).setText(code.getQualifiedName());
        eFault.addUniqueElement(FAULTSTRING).setText(reason);
        Element eDetail = eFault.addUniqueElement(DETAIL);
        Element error = eDetail.addUniqueElement(ZimbraNamespace.E_ERROR);
        // FIXME: should really be a qualified "attribute"
        error.addUniqueElement(ZimbraNamespace.E_CODE).setText(e.getCode());
        if (LC.soap_fault_include_stack_trace.booleanValue())
            error.addUniqueElement(ZimbraNamespace.E_TRACE).setText(ExceptionToString.ToString(e));
        else
            error.addUniqueElement(ZimbraNamespace.E_TRACE).setText(e.getThreadName());

        for (ServiceException.Argument arg : e.getArgs()) {
            if (arg.externalVisible()) {
                Element val = error.addElement(ZimbraNamespace.E_ARGUMENT);
                val.addAttribute(ZimbraNamespace.A_ARG_NAME, arg.name);
                val.addAttribute(ZimbraNamespace.A_ARG_TYPE, arg.type.toString());
                val.setText(arg.value);
            }
        }
        return eFault;
    }

    /** Return Content-Type header */
    public String getContentType() {
        return "text/xml; charset=utf-8";
    }

    /** Whether or not to include a SOAPActionHeader */
    public boolean hasSOAPActionHeader() {
        return true;
    }

    public String getVersion() {
        return "1.1.";
    }
}
