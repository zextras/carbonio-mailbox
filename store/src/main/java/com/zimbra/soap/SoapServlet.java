// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zextras.mailbox.metric.Metrics;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.BufferStream;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.LoadingCacheUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.common.util.ZimbraServletOutputStream;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.stats.ZimbraPerf;
import com.zimbra.cs.util.Zimbra;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicLineParser;

/** The soap service servlet */
public class SoapServlet extends ZimbraServlet {
  private static final long serialVersionUID = 38710345271877593L;

  protected static final String PARAM_ENGINE_HANDLER = "engine.handler.";

  /** context name of auth token extracted from cookie */
  public static final String ZIMBRA_AUTH_TOKEN = "zimbra.authToken";

  /** context name of servlet context */
  public static final String SERVLET_CONTEXT = "servlet.context";

  /** context name of servlet HTTP request */
  public static final String SERVLET_REQUEST = "servlet.request";

  /** context name of servlet HTTP response */
  public static final String SERVLET_RESPONSE = "servlet.response";

  /** If this is a request sent to the admin port */
  public static final String IS_ADMIN_REQUEST = "zimbra.isadminreq";

  /** Flag for requests that want to force invalidation of client cookies */
  public static final String INVALIDATE_COOKIES = "zimbra.invalidateCookies";

  static final int SC_UNPROCESSABLE_CONTENT = 422;

  /** Keeps track of extra services added by extensions. */
  private static LoadingCache<String, List<DocumentService>> sExtraServices =
      CacheBuilder.newBuilder().build(CacheLoader.from(new ArrayListFactory()));

  private static Log sLog = LogFactory.getLog(SoapServlet.class);
  private SoapEngine mEngine;

  // Used by sExtraServices
  private static class ArrayListFactory implements Function<String, List<DocumentService>> {
    @Override
    public List<DocumentService> apply(String from) {
      return new ArrayList<>();
    }
  }

  @Override
  public void init() throws ServletException {
    String name = getServletName();
    ZimbraLog.soap.info("Servlet " + name + " starting up");
    super.init();

    mEngine = new SoapEngine(Metrics.METER_REGISTRY);

    int i = 0;
    String cname;
    while ((cname = getInitParameter(PARAM_ENGINE_HANDLER + i)) != null) {
      try {
        loadHandler(cname);
      } catch (ServiceException e) {
        throw new ServletException(
            "Failed to startup SoapServlet. Cannot load handler " + cname + ": " + e.getMessage());
      }
      i++;
    }

    // See if any extra services were previously added by extensions
    synchronized (sExtraServices) {
      List<DocumentService> services = LoadingCacheUtil.get(sExtraServices, getServletName());
      for (DocumentService service : services) {
        try {
          service.registerHandlers(mEngine.getDocumentDispatcher());
        } catch (ServiceException exception) {
          throw new ServletException(
              "Failed to startup SoapServlet. Cannot register service handlers: "
                  + exception.getMessage());
        }
        i++;
      }
    }

    mEngine.getDocumentDispatcher().clearSoapWhiteList();

    if (i == 0)
      throw new ServletException("Must specify at least one handler " + PARAM_ENGINE_HANDLER + i);

    try {
      Zimbra.startup();
    } catch (OutOfMemoryError e) {
      Zimbra.halt("out of memory", e);
    } catch (Throwable t) {
      ZimbraLog.soap.fatal("Unable to start servlet", t);
      throw new UnavailableException(t.getMessage());
    }
  }

  @Override
  public void destroy() {
    String name = getServletName();
    ZimbraLog.soap.info("Servlet " + name + " shutting down");
    try {
      Zimbra.shutdown();
    } catch (ServiceException e) {
      // Log as error and ignore.
      ZimbraLog.soap.error("ServiceException while shutting down servlet " + name, e);
    } catch (RuntimeException e) {
      ZimbraLog.soap.error("Unchecked Exception while shutting down servlet " + name, e);
      throw e;
    }
    // FIXME: we might want to add mEngine.destroy()
    // to allow the mEngine to cleanup?
    mEngine = null;

    super.destroy();
  }

  private void loadHandler(String cname) throws ServletException, ServiceException {
    Class<?> dispatcherClass = null;
    try {
      dispatcherClass = Class.forName(cname);
    } catch (OutOfMemoryError e) {
      Zimbra.halt("out of memory", e);
    } catch (Throwable cnfe) {
      throw new ServletException("can't find handler initializer class " + cname, cnfe);
    }

    Object dispatcher;

    try {
      dispatcher = dispatcherClass.newInstance();
    } catch (InstantiationException | IllegalAccessException ie) {
      throw new ServletException("can't instantiate class " + cname, ie);
    }

    if (!(dispatcher instanceof DocumentService)) {
      throw new ServletException("class not an instanceof HandlerInitializer: " + cname);
    }

    DocumentService hi = (DocumentService) dispatcher;
    addService(hi);
  }

  /**
   * Note: this method is used by extensions loaded through {@link
   * com.zimbra.cs.extension.ExtensionUtil} during {@link Zimbra#startup()}. An extension should not
   * prevent application/servlet startup. If adding a service through extension fails we do not want
   * to prevent startup of the servlet.
   *
   * <p>Adds a service to the instance of <code>SoapServlet</code> with the given name. If the
   * servlet has not been loaded, stores the service for later initialization.
   */
  public static void addService(String servletName, DocumentService service) {
    synchronized (sExtraServices) {
      ZimbraServlet servlet = ZimbraServlet.getServlet(servletName);
      if (servlet != null) {
        try {
          ((SoapServlet) servlet).addService(service);
        } catch (ServiceException e) {
          ZimbraLog.soap.error("Cannot add service", e);
        }
      } else {
        sLog.debug(
            "addService(%s, %s): servlet has not been initialized",
            servletName, service.getClass().getSimpleName());
        List<DocumentService> services = LoadingCacheUtil.get(sExtraServices, servletName);
        services.add(service);
      }
    }
  }

  private void addService(DocumentService service) throws ServiceException {
    ZimbraLog.soap.info(
        "Adding service %s to %s", service.getClass().getSimpleName(), getServletName());
    service.registerHandlers(mEngine.getDocumentDispatcher());
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    ZimbraLog.clearContext();
    long startTime = ZimbraPerf.STOPWATCH_SOAP.start();

    try {
      doWork(req, resp);
    } finally {
      ZimbraLog.clearContext();
      ZimbraPerf.STOPWATCH_SOAP.stop(startTime);
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ZimbraLog.clearContext();
    long startTime = ZimbraPerf.STOPWATCH_SOAP.start();

    try {
      doWork(req, resp);
    } finally {
      ZimbraLog.clearContext();
      ZimbraPerf.STOPWATCH_SOAP.stop(startTime);
    }
  }

  private void doWork(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    int len = req.getContentLength();
    byte[] buffer;
    boolean isResumed = true;

    // resuming from a Jetty Continuation does *not* reset the HttpRequest's input stream -
    // therefore we store the read buffer in the Continuation, and use the stored buffer
    // if we're resuming
    buffer = (byte[]) req.getAttribute("com.zimbra.request.buffer");
    if (buffer == null) {
      isResumed = false;

      // Look up max request size
      int maxSize = 0;
      try {
        maxSize =
            Provisioning.getInstance()
                .getLocalServer()
                .getIntAttr(Provisioning.A_zimbraSoapRequestMaxSize, 0);
      } catch (ServiceException e) {
        ZimbraLog.soap.warn(
            "Unable to look up %s.  Not limiting the request size.",
            Provisioning.A_zimbraSoapRequestMaxSize, e);
      }
      if (maxSize <= 0) {
        maxSize = Integer.MAX_VALUE;
      }

      // Read the request
      boolean success;
      if (len > maxSize) {
        success = false;
      } else {
        BufferStream bs = null;

        try {
          bs = new BufferStream(len, maxSize, maxSize);
          int in = (int) bs.readFrom(req.getInputStream(), len >= 0 ? len : Integer.MAX_VALUE);

          if (len > 0 && in < len)
            throw new EOFException("SOAP content truncated " + in + "!=" + len);
          success = in <= maxSize;
          buffer = bs.toByteArray();
        } finally {
          ByteUtil.closeStream(bs);
        }
      }

      // Handle requests that exceed the size limit
      if (!success) {
        String sizeString = (len < 0 ? "" : " size " + len);
        String msg =
            String.format(
                "Request%s exceeded limit of %d bytes set for %s.",
                sizeString, maxSize, Provisioning.A_zimbraSoapRequestMaxSize);
        ServiceException e = ServiceException.INVALID_REQUEST(msg, null);
        ZimbraLog.soap.warn(null, e);
        Element fault = SoapProtocol.Soap12.soapFault(e);
        Element envelope = SoapProtocol.Soap12.soapEnvelope(fault);
        sendResponse(req, resp, envelope);
        return;
      }

      req.setAttribute("com.zimbra.request.buffer", buffer);
    }

    HashMap<String, Object> context = new HashMap<>();
    context.put(SERVLET_CONTEXT, getServletContext());
    context.put(SERVLET_REQUEST, req);
    context.put(SERVLET_RESPONSE, resp);

    try {
      Boolean isAdminReq = isAdminRequest(req);
      context.put(IS_ADMIN_REQUEST, isAdminReq);
    } catch (ServiceException se) {
      ZimbraLog.soap.warn("unable to determine isAdminReq", se);
    }

    // setup IPs in the context and add to logging context
    RemoteIP remoteIp = new RemoteIP(req, ZimbraServlet.getTrustedIPs());
    context.put(SoapEngine.SOAP_REQUEST_IP, remoteIp.getClientIP());
    context.put(SoapEngine.ORIG_REQUEST_IP, remoteIp.getOrigIP());
    context.put(SoapEngine.REQUEST_IP, remoteIp.getRequestIP());
    remoteIp.addToLoggingContext();

    // checkAuthToken(req.getCookies(), context);
    context.put(SoapEngine.REQUEST_PORT, req.getServerPort());
    context.put(
        SoapEngine.ORIG_REQUEST_USER_AGENT,
        req.getHeader(HeaderConstants.HTTP_HEADER_ORIG_USER_AGENT));
    Element envelope = null;
    try {
      envelope = mEngine.dispatch(req.getRequestURI(), buffer, context);
      if (context.containsKey(INVALIDATE_COOKIES)) {
        ZAuthToken.clearCookies(resp);
      }
    } catch (Throwable e) {
      if (e instanceof OutOfMemoryError) {
        Zimbra.halt("handler exception", e);
      }

      if (ZimbraLog.soap.isTraceEnabled() && !context.containsKey(SoapEngine.SOAP_REQUEST_LOGGED)) {
        ZimbraLog.soap.trace(
            !isResumed ? "C:\n%s" : "C: (resumed)\n%s", new String(buffer, Charsets.UTF_8));
      }

      // don't interfere with Jetty Continuations -- pass the exception right up
      if (e.getClass().getName().equals("org.eclipse.jetty.continuation.ContinuationThrowable"))
        throw (Error) e;

      ZimbraLog.soap.warn("handler exception", e);
      Element fault = SoapProtocol.Soap12.soapFault(ServiceException.FAILURE(e.toString(), e));
      envelope = SoapProtocol.Soap12.soapEnvelope(fault);
    }

    if (ZimbraLog.soap.isTraceEnabled()) {
      ZimbraLog.soap.trace("S:\n%s", envelope.prettyPrint());
    }
    sendResponse(req, resp, envelope);
  }

  private int soapResponseBufferSize() {
    String val = LC.soap_response_buffer_size.value();
    if (val == null || val.length() == 0) return -1; // will be using jetty default
    else return LC.soap_response_buffer_size.intValue();
  }

  private void sendResponse(HttpServletRequest req, HttpServletResponse resp, Element envelope)
      throws IOException {
    SoapProtocol soapProto = SoapProtocol.determineProtocol(envelope);
    int statusCode =
        soapProto.hasFault(envelope) ? SC_UNPROCESSABLE_CONTENT : HttpServletResponse.SC_OK;

    boolean chunkingEnabled = LC.soap_response_chunked_transfer_encoding_enabled.booleanValue();

    if (chunkingEnabled) {
      // disable chunking if proto < HTTP 1.1
      String proto = req.getProtocol();
      try {
        ProtocolVersion httpVer =
            BasicLineParser.parseProtocolVersion(proto, new BasicLineParser());
        chunkingEnabled = !httpVer.lessEquals(HttpVersion.HTTP_1_0);
      } catch (ParseException e) {
        ZimbraLog.soap.warn(
            "cannot parse http version in request: %s, http chunked transfer encoding disabled",
            proto, e);
        chunkingEnabled = false;
      }
    }

    // use jetty default if the LC key is not set
    int responseBufferSize = soapResponseBufferSize();
    if (responseBufferSize != -1) resp.setBufferSize(responseBufferSize);

    resp.setContentType(soapProto.getContentType());
    resp.setStatus(statusCode);
    resp.setHeader("Cache-Control", "no-store, no-cache");

    if (chunkingEnabled) {
      // Let jetty chunk the response if applicable.
      ZimbraServletOutputStream out = new ZimbraServletOutputStream(resp.getOutputStream());
      envelope.output(out);
      out.flush();
    } else {
      // serialize the envelope to a byte array and send the response with Content-Length header.
      byte[] soapBytes = envelope.toUTF8();
      resp.setContentLength(soapBytes.length);
      resp.getOutputStream().write(soapBytes);
      resp.getOutputStream().flush();
    }
    envelope.destroy();
  }
}
