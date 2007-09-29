/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006, 2007 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.service.util.ThreadLocalData;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.stats.StatsFile;
import com.zimbra.cs.stats.ZimbraPerf;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.util.Zimbra;
import com.zimbra.common.util.ZimbraLog;

/**
 * The soap service servlet
 */

public class SoapServlet extends ZimbraServlet {

    private static final String PARAM_ENGINE_HANDLER = "engine.handler.";
    private static final String IP_LOCALHOST = "127.0.0.1"; 

    /** context name of auth token extracted from cookie */
    public static final String ZIMBRA_AUTH_TOKEN = "zimbra.authToken";    
    /** context name of servlet HTTP request */
    public static final String SERVLET_REQUEST = "servlet.request";

    // Used by sExtraServices
    private static Factory sListFactory = new Factory() {
        public Object create() {
            return new ArrayList<DocumentService>();
        }
    };
    
    /**
     * Keeps track of extra services added by extensions.
     */
    private static Map<String, List<DocumentService>> sExtraServices =
        LazyMap.decorate(new HashMap(), sListFactory);
    
    private static Log sLog = LogFactory.getLog(SoapServlet.class);
    private SoapEngine mEngine;

    public void init() throws ServletException {
        // TODO we should have a ReloadConfig soap command that will reload
        // on demand, instead of modifying and waiting for some time.
        PropertyConfigurator.configureAndWatch(LC.zimbra_log4j_properties.value());

        String name = getServletName();
        ZimbraLog.soap.info("Servlet " + name + " starting up");
        super.init();

        mEngine = new SoapEngine();

        int i=0;
        String cname;
        while ((cname = getInitParameter(PARAM_ENGINE_HANDLER+i)) != null) {
            loadHandler(cname);
            i++;
        }
        
        // See if any extra services were perviously added by extensions 
        synchronized (sExtraServices) {
            List<DocumentService> services = sExtraServices.get(getServletName());
            for (DocumentService service : services) {
                addService(service);
                i++;
            }
        }
        
        if (i==0)
            throw new ServletException("Must specify at least one handler "+PARAM_ENGINE_HANDLER+i);

        try {
            Zimbra.startup();
        } catch (Throwable t) {
            ZimbraLog.soap.fatal("Unable to start servlet", t);
        	throw new UnavailableException(t.getMessage());
        }
    }

    public void destroy() {
        String name = getServletName();
        ZimbraLog.soap.info("Servlet " + name + " shutting down");
        try {
            Zimbra.shutdown();
        } catch (ServiceException e) {
            // Log as error and ignore.
        	ZimbraLog.soap.error("Exception while shutting down servlet " + name, e);
        }
        // FIXME: we might want to add mEngine.destroy()
        // to allow the mEngine to cleanup?
        mEngine = null;

        super.destroy();
    }

    private void loadHandler(String cname) throws ServletException {
        Class dispatcherClass;
        try {
            dispatcherClass = Class.forName(cname);
        } catch (ClassNotFoundException cnfe) {
            throw new ServletException("can't find handler initializer class "+cname,
                                       cnfe);
        } catch (Throwable t) {
            throw new ServletException("can't find handler initializer class " + cname, t);
        }

        Object dispatcher;

        try {
            dispatcher = dispatcherClass.newInstance();
        } catch (InstantiationException ie) {
            throw new ServletException("can't instantiate class "+cname, ie);
        } catch (IllegalAccessException iae) {
            throw new ServletException("can't instantiate class "+cname, iae);
        }

        if (!(dispatcher instanceof DocumentService)) {
            throw new ServletException(
                   "class not an instanceof HandlerInitializer: "+cname);
        }

        DocumentService hi = (DocumentService) dispatcher;
        addService(hi);
    }
    
    /**
     * Adds a service to the instance of <code>SoapServlet</code> with the given
     * name.  If the servlet has not been loaded, stores the service for later
     * initialization.
     */
    public static void addService(String servletName, DocumentService service) {
        synchronized (sExtraServices) {
            ZimbraServlet servlet = ZimbraServlet.getServlet(servletName);
            if (servlet != null) {
                ((SoapServlet) servlet).addService(service);
            } else {
                sLog.debug("addService(" + servletName + ", " +
                    StringUtil.getSimpleClassName(service) + "): servlet has not been initialized");
                List<DocumentService> services = sExtraServices.get(servletName);
                services.add(service);
            }
        }
    }
    
    private void addService(DocumentService service) {
        ZimbraLog.soap.info("Adding service " + StringUtil.getSimpleClassName(service) +
            " to " + getServletName());
        service.registerHandlers(mEngine.getDocumentDispatcher());
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ZimbraLog.clearContext();
        long startTime = ZimbraPerf.STOPWATCH_SOAP.start();

        // Performance
        if (ZimbraLog.perf.isDebugEnabled()) {
            ThreadLocalData.reset();
        }

        int len = req.getContentLength();
        byte[] buffer;
        if (len == -1) {
            buffer = readUntilEOF(req.getInputStream());
        } else {
            buffer = new byte[len];
            readFully(req.getInputStream(), buffer, 0, len);
        }
        if (ZimbraLog.soap.isDebugEnabled()) {
            ZimbraLog.soap.debug("SOAP request:\n" + new String(buffer, "utf8"));
        }

        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put(SERVLET_REQUEST, req);
        
        // Set the requester IP.  If the request was made by the HTML client,
        // set it to the value of the X-Originating-IP header.
        String remoteAddr = req.getRemoteAddr();
        String origIp = req.getHeader(SoapHttpTransport.X_ORIGINATING_IP);
        if (origIp != null && IP_LOCALHOST.equals(remoteAddr)) {
            remoteAddr = origIp;
        }
        context.put(SoapEngine.REQUEST_IP, remoteAddr);            
        //checkAuthToken(req.getCookies(), context);

        Element envelope = null;
        try {
            envelope = mEngine.dispatch(req.getRequestURI(), buffer, context);
        } catch (Throwable e) {
            if (e instanceof OutOfMemoryError) {
                Zimbra.halt("handler exception", e);
            }
            ZimbraLog.soap.warn("handler exception", e);
            Element fault = SoapProtocol.Soap12.soapFault(ServiceException.FAILURE(e.toString(), e));
            envelope = SoapProtocol.Soap12.soapEnvelope(fault);
        }

        SoapProtocol soapProto = SoapProtocol.determineProtocol(envelope);
        int statusCode = soapProto.hasFault(envelope) ?
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR : HttpServletResponse.SC_OK;
        
        byte[] soapBytes = envelope.toUTF8();
        if (ZimbraLog.soap.isDebugEnabled()) {
            ZimbraLog.soap.debug("SOAP response: \n" + new String(soapBytes, "utf8"));
        }
        
        resp.setContentType(soapProto.getContentType());
        resp.setBufferSize(soapBytes.length + 2048);
        resp.setContentLength(soapBytes.length);
        resp.setStatus(statusCode);
        resp.getOutputStream().write(soapBytes);

        ZimbraPerf.STOPWATCH_SOAP.stop(startTime);
    }
}
