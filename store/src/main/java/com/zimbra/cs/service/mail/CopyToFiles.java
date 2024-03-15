package com.zimbra.cs.service.mail;

import static com.google.common.base.Predicates.instanceOf;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static java.util.function.Predicate.not;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.CopyToFilesRequest;
import com.zimbra.soap.mail.message.CopyToFilesResponse;
import io.vavr.control.Try;
import java.util.Map;
import java.util.Optional;

/**
 * Service class to handle copy item to Files.
 *
 * @author davidefrison
 * @since 4.0.7
 */
public class CopyToFiles extends MailDocumentHandler {

  private static final Log mLog = LogFactory.getLog(CopyToFiles.class);
  private final FilesCopyHandler filesCopyHandler;

  public CopyToFiles(FilesCopyHandler filesCopyHandler) {
    this.filesCopyHandler = filesCopyHandler;
  }

  /**
   * Main method to handle the copy to drive request.
   *
   * @param request request type Element for {@link CopyToFilesRequest}
   * @param context request context
   * @return Element for {@link CopyToFilesResponse}
   * @throws ServiceException
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final CopyToFilesResponse copyToFilesResponse =
        Optional.ofNullable(
                getRequestObject(request)
                    .flatMap(
                        copyToFilesRequest ->
                            filesCopyHandler.copy(copyToFilesRequest, zsc.getAuthtokenAccountId(), zsc.getAuthToken()))
                    .onFailure(ex -> mLog.error(ex.getMessage()))
                    .mapFailure(
                        Case(
                            $(not(instanceOf(ServiceException.class))),
                            ServiceException::INTERNAL_ERROR))
                    .get())
            .orElseThrow(() -> ServiceException.FAILURE("got null response from Files server."));
    return zsc.jaxbToElement(copyToFilesResponse);
  }

  /**
   * Transforms the SOAP request in {@link CopyToFilesRequest} instance
   *
   * @param request soap {@link Element} as received from client
   * @return try of {@link CopyToFilesRequest}
   */
  private Try<CopyToFilesRequest> getRequestObject(Element request) {
    return Try.<CopyToFilesRequest>of(() -> JaxbUtil.elementToJaxb(request))
        .onFailure(ex -> mLog.error(ex.getMessage()))
        .recoverWith(ex -> Try.failure(ServiceException.PARSE_ERROR("Malformed request.", ex)));
  }

}
