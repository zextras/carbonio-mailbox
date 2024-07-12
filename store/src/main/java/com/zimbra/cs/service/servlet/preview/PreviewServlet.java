package com.zimbra.cs.service.servlet.preview;

import com.zextras.carbonio.preview.PreviewClient;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.MailboxAttachmentService;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The preview service servlet - serves preview for requested mail attachments using Carbonio previewer service
 *
 * <pre>
 *
 *   Based on Carbonio Preview SDK 1.0.2
 *
 *   The API is the almost same as of preview service(https://zextras.atlassian.net/wiki/spaces/SW/pages/2353430753/Preview+API)
 *   with few modification that let us make it use as preview service for mailbox attachments.
 *
 *   itemId, partNo, disposition(disp) are notable new parameters, their usage can be found in the URL given below:
 *
 *   https://nbm-s01.demo.zextras.io/service/preview/{format}/{itemId}/{partNo}/{area}/{thumbnail}/?[{query-params}]
 *
 *          Path parameters:
 *                    format  =  image | pdf | document
 *                    itemId  =  mail-item-id
 *                    partNo  =  mail-item-part-number
 *                      area  =  width of the output image (>=0) x height of the output image (>=0),
 *                               width x height => 100x200. The first is width, the latter height, the order is important!
 *                 thumbnail  =  omit for full preview type
 *                               'thumbnail' if requesting the preview type
 *                                thumbnail
 *
 *          Query parameters:
 *                      disp  =  attachment(a) | inline(i)
 *                                  Default value : inline(i)
 *                     shape  =  rounded | rectangular
 *                                  Default value : rectangular
 *                   quality  =  lowest | low | medium | high | highest
 *                                  Default value : medium
 *             output_format  =  jpeg | png; default inline(jpeg)
 *                                  Default value : jpeg
 *                      crop  =  True will crop the picture starting from the borders
 *                               This option will lose information, leaving it False will scale and have borders to fill the requested size.
 *                                  Default value : false
 *                first_page  =  integer value of first page to preview (n>=1)
 *                                  Default value : 1
 *                 last_page  =  integer value of last page to preview (0 = last of the pdf/document)
 *                                  Default value : 0
 *
 *            Authentication  =  expects ZM_AUTH_TOKEN cookie passed in the request
 *                               headers
 *
 * </pre>
 *
 * @author keshavbhatt
 */

public class PreviewServlet extends ZimbraServlet {
  @SuppressWarnings("squid:S1075")
  public static final String SERVLET_PATH = "/preview";
  private static final long serialVersionUID = -4834966842520538743L;
  private static final String PREVIEW_SERVICE_BASE_URL = "http://127.78.0.7:20001/";
  private static final PreviewClient PREVIEW_CLIENT = PreviewClient.atURL(PREVIEW_SERVICE_BASE_URL);
  private static final PreviewHandler PREVIEW_HANDLER = new PreviewHandler(PREVIEW_CLIENT,
      new MailboxAttachmentService());
  private static final Log LOG = LogFactory.getLog(PreviewServlet.class);

  @Override
  public void init() throws ServletException {
    LOG.info(getServletName() + " starting up..");
    super.init();
  }

  @Override
  public void destroy() {
    LOG.info(getServletName() + " shutting down..");
    super.destroy();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    ZimbraLog.clearContext();
    addRemoteIpToLoggingContext(request);

    var start = Instant.now();
    var requestId = UUID.randomUUID().toString();
    request.setAttribute(Utils.REQUEST_ID_ATTRIBUTE_KEY, requestId);
    var fullURL = Utils.getFullURLFromRequest(request);
    LOG.info("[" + requestId + "] Received GET request for URL: " + fullURL);

    try {
      PREVIEW_HANDLER.handle(request, response);
    } finally {
      var end = Instant.now();
      var elapsed = Duration.between(start, end);
      LOG.info("[" + requestId + "] Handled request for URL: " + fullURL + " in " + elapsed.toMillis() + "ms ");
    }
  }
}
