// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.zimbra.common.service.ServiceException;

public class SoapFaultException extends ServiceException {

	private static final long serialVersionUID = 2021293100288028461L;

	/** used for default value and when we get a fault without a detail code */
    public static final String UNKNOWN = "soap.UNKNOWN";

    private Element mDetail;
    private Element mFault;
    private boolean mIsLocal;

    // in case catcher of SoapFaultException wants to see the request/response
    private String mRequest;
    private String mResponse;

    public SoapFaultException(String message, String code, boolean isReceiversFault, Throwable cause) {
        super(message, code, isReceiversFault);
    }

    /**
     * Generic SoapFaultException
     * @param message message to set in response
     * @param code code for the error
     * @param isReceiversFault if the error occurred because of server/receiver fault
     */
    public SoapFaultException(String message, String code, boolean isReceiversFault) {
        super(message, code, isReceiversFault);
    }

    /** Create a new SoapFaultException. */
    public SoapFaultException(String message, Element detail, boolean isReceiversFault) {
        super(message, getCode(detail), isReceiversFault);
        //subcode = subcode;
        mDetail = detail;
        mFault = null;
    }

    /** Create a new SoapFaultException. Used by SoapProtocol only. */
    SoapFaultException(String message, Element detail, boolean isReceiversFault, Element fault) {
        super(message, getCode(detail), isReceiversFault, null, parseArgs(detail));
        mDetail = detail;
        mFault = fault;
        if (detail != null) {
            Element error = detail.getOptionalElement(ZimbraNamespace.E_ERROR);
            if (error != null) {
                String traceId = error.getAttribute(ZimbraNamespace.E_TRACE.getName(), null);
                if (traceId != null) setId(traceId);
            }
        }
    }

    /** used by the document handler proxy mechanism */
    public SoapFaultException(String message, boolean isLocal, Element fault) {
        this(message, fault.getOptionalElement("Detail"), false, fault);
        mIsLocal = isLocal;
    }

    /** used by transports and stub mCode */
    public SoapFaultException(String message, Element fault) {
        super(message, UNKNOWN, false);
        mFault = fault;
    }


    private static String getCode(Element detail) {
        if (detail != null) {
            Element error = detail.getOptionalElement(ZimbraNamespace.E_ERROR);
            if (error != null)
                return error.getAttribute(ZimbraNamespace.E_CODE.getName(), UNKNOWN);
        }
        return UNKNOWN;
    }

    public Element getDetail() {
        return mDetail;
    }

    /** can only be called if mDetail is null. */
    protected void initDetail(Element detail) throws IllegalStateException {
        if (mDetail != null)
            throw new IllegalStateException("mDetail is not null");

        mDetail = detail;
    }

    /**
     * Returns the raw soap mFault, if available.
     */
    public Element getFault() {
        return mFault;
    }

    /**
     * Returns whether the SOAP fault was generated locally.
     * <code>false</code> means that it came from a remote source.
     */
    public boolean isSourceLocal() {
        return mIsLocal;
    }

    /**
     * Attache the request that caused this exception for downstream consumption
     * @param request
     */
    public void setFaultRequest(String request) {
    	mRequest = request;
    }

    /**
     * Attache the response that contains this exception for downstream consumption
     * @param response
     */
    public void setFaultResponse(String response) {
    	mResponse = response;
    }

    /**
     * Retrieves the request that caused this exception
     * @return the request, null if not available
     */
    public String getFaultRequest() {
    	return mRequest;
    }

    /**
     * Retrieves the response that contains this exception
     * @return the response, null if not available
     */
    public String getFaultResponse() {
    	return mResponse;
    }

    /**
     * dump out detailed debugging information about this mFault
     */
    public String dump() {
        StringBuffer sb = new StringBuffer();
        sb.append("class=").append(getClass().getName()).append("\n");
        sb.append("message=").append(getMessage()).append("\n");
        sb.append("isReceiversFault=").append(isReceiversFault()).append("\n");
        sb.append("mIsLocal=").append(mIsLocal).append("\n");

        sb.append("mDetail=").append(mDetail).append("\n");
        sb.append("mFault=").append(mFault).append("\n");
        return sb.toString();
    }


    /**
     * Returns the arguments in the given SOAP fault, or an empty list.
     */
    private static List<ServiceException.Argument> parseArgs(Element detail) {
        if (detail == null) {
            return Collections.emptyList();
        }
        Element errorEl = detail.getOptionalElement(ZimbraNamespace.E_ERROR);
        if (errorEl == null) {
            return Collections.emptyList();
        }

        List<ServiceException.Argument> args = Lists.newArrayList();
        for (Element argEl : errorEl.listElements(ZimbraNamespace.E_ARGUMENT.getName())) {
            String name = argEl.getAttribute(ZimbraNamespace.A_ARG_NAME, null);
            String typeString = argEl.getAttribute(ZimbraNamespace.A_ARG_TYPE, null);
            ServiceException.Argument.Type type = (typeString == null ? null : ServiceException.Argument.Type.valueOf(typeString));
            String value = argEl.getText();
            args.add(new ServiceException.Argument(name, value, type));
        }

        return args;
    }
}
