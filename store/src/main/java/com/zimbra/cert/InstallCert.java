// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cert;

import com.google.common.base.Strings;
import com.zimbra.common.account.Key.ServerBy;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.ldap.LdapUtil;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.rmgmt.RemoteResult;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.InstallCertRequest;
import com.zimbra.soap.admin.type.AidAndFilename;
import com.zimbra.soap.admin.type.CommCert;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

public class InstallCert extends AdminDocumentHandler {
    private final static String ALLSERVER_FLAG = "-allserver" ;
    private Server server = null;

    private Provisioning prov = null;
    private String tmpFolderName = null;
    private String uploadedCertFileName = null;
    private String uploadedCrtChainFileName = null;
    private String savedCommKeyFileName = null;
    private boolean isCommercial = false;
    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);

        prov = Provisioning.getInstance();
        InstallCertRequest req = JaxbUtil.elementToJaxb(request);
        Boolean skipCleanup = req.getSkipCleanup();
        String serverId = req.getServer();
        boolean isTargetAllServer = false ;
        if (serverId != null && serverId.equals(ZimbraCertMgrExt.ALL_SERVERS)) {
            server = prov.getLocalServer() ;
            isTargetAllServer = true ;
        } else {
            server = prov.get(ServerBy.id, serverId);
        }

        if (server == null) {
            throw ServiceException.INVALID_REQUEST("Server with id " + serverId + " could not be found", null);
        }
        checkRight(lc, context, server, Admin.R_installCertificate);
        ZimbraLog.security.debug("Install certificate for server: %s", server.getName());
        //the deployment of certs should happen on the target server
        RemoteManager rmgr = RemoteManager.getRemoteManager(server);
        StringBuilder cmd = new StringBuilder(ZimbraCertMgrExt.CREATE_CRT_CMD);
        StringBuilder deploycrt_cmd = new StringBuilder(ZimbraCertMgrExt.DEPLOY_CERT_CMD);
        String certType = req.getType();
        if (Strings.isNullOrEmpty(certType)) {
            throw ServiceException.INVALID_REQUEST("No valid certificate type is set", null);
        } else if (certType.equals(ZimbraCertMgrExt.CERT_TYPE_SELF) || certType.equals(ZimbraCertMgrExt.CERT_TYPE_COMM)) {
            deploycrt_cmd.append(" ").append(certType);
        } else {
            throw ServiceException.INVALID_REQUEST(String.format(
                    "Invalid certificate type: '%s'. Must be '%s' or '%s' ", certType, ZimbraCertMgrExt.CERT_TYPE_SELF,
                    ZimbraCertMgrExt.CERT_TYPE_COMM), null);
        }
        try {
            isCommercial = certType.equals(ZimbraCertMgrExt.CERT_TYPE_COMM);
            if (isCommercial) {
                tmpFolderName = LC.zimbra_tmp_directory.value() + File.separator + LdapUtil.generateUUID()
                        + File.separator;
                Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
                FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
                try {
                    Files.createDirectory(Paths.get(tmpFolderName), fileAttributes);
                } catch (IOException e) {
                    throw ServiceException.FAILURE("Could not create temporary folder for certificate files", e);
                }
                String uuid = LdapUtil.generateUUID();
                uploadedCertFileName = tmpFolderName + "crt_" + uuid;
                uploadedCrtChainFileName = tmpFolderName + "chain_" + uuid;
                savedCommKeyFileName = tmpFolderName + "key_" + uuid;
                checkUploadedCommCert(req, lc, isTargetAllServer);
            }

            // always set the -new flag for the cmd since the ac requests for a new cert always
            cmd.append(" -new");

            if (!isCommercial) {
                String validation_days = req.getValidationDays();
                if (!Strings.isNullOrEmpty(validation_days)) {
                    if (!validation_days.matches("[0-9]*")) {
                        throw ServiceException.INVALID_REQUEST(
                                String.format("validation_days %s is not valid.", validation_days), null);
                    }
                    cmd.append(" -days ").append(validation_days);
                }
            }

            String subject = GenerateCSR.getSubject(req.getSubject());

            String subjectAltNames = GenerateCSR.getSubjectAltNames(req.getSubjectAltNames());

            if (!isCommercial) {
                String digest = req.getDigest();
                if (digest != null && !digest.isEmpty()) {
                    if (!digest.matches("[a-zA-z0-9]*")) {
                        throw ServiceException.INVALID_REQUEST("digest is not valid.", null);
                    }
                    cmd.append(" -digest ").append(digest);
                }

                String keysize = req.getKeySize();
                if (keysize != null && !keysize.isEmpty()) {
                    try {
                        int iKeySize = Integer.parseInt(keysize);
                        if (iKeySize < 2048) {
                            throw ServiceException.INVALID_REQUEST("Minimum allowed key size is 2048", null);
                        }
                        cmd.append(" -keysize ").append(keysize);
                    } catch (NumberFormatException nfe) {
                        throw ServiceException.INVALID_REQUEST("Invalid value for parameter "
                                + CertMgrConstants.E_KEYSIZE, nfe);
                    }
                }

                GenerateCSR.appendSubjectArgToCommand(cmd, subject);

                if (subjectAltNames != null && subjectAltNames.length() > 0) {
                    cmd.append(" -subjectAltNames \"").append(subjectAltNames).append("\"");
                }
            } else if (isCommercial) {
                deploycrt_cmd.append(" ").append(uploadedCertFileName).append(" ").append(uploadedCrtChainFileName);
            }

            if (isTargetAllServer) {
                if (!isCommercial) { // self -allserver install - need to pass the subject to the createcrt cmd
                    if (subject != null && subject.length() > 0) {
                        ZimbraLog.security.debug("Subject for allserver: %s", subject);
                        GenerateCSR.appendSubjectArgToCommand(cmd, subject);
                    }
                }

                cmd.append(" ").append(ALLSERVER_FLAG);
                deploycrt_cmd.append(" ").append(ALLSERVER_FLAG);
            }

            RemoteResult rr;
            if (!isCommercial) {
                ZimbraLog.security.debug("***** Executing the cmd = %s", cmd);
                rr = rmgr.execute(cmd.toString());
                // ZimbraLog.security.info("***** Exit Status Code = " + rr.getMExitStatus()) ;
                try {
                    OutputParser.parseOuput(rr.getMStdout());
                } catch (IOException ioe) {
                    throw ServiceException.FAILURE("exception occurred handling command", ioe);
                }
            }

            // need to deploy the crt now
            ZimbraLog.security.debug("***** Executing the cmd = %s", deploycrt_cmd);
            rr = rmgr.execute(deploycrt_cmd.toString());
            try {
                OutputParser.parseOuput(rr.getMStdout());
            } catch (IOException ioe) {
                throw ServiceException.FAILURE("exception occurred handling command", ioe);
            }
        } finally {
            if (!skipCleanup) {
                // cleanup
                if (isCommercial && tmpFolderName != null) {
                    try {
                        File d = new File(tmpFolderName);
                        if (d.exists() && d.isDirectory()) {
                            FileUtils.deleteDirectory(d);
                        }
                    } catch (IOException e) {
                        throw ServiceException.FAILURE("Failed to delete temporary folder with certificate files", e);
                    }
                }
            }
        }
        Element response = lc.createElement(CertMgrConstants.INSTALL_CERT_RESPONSE);
        response.addAttribute(AdminConstants.A_SERVER, server.getName());
        return response;
    }

    private boolean checkUploadedCommCert(InstallCertRequest req, ZimbraSoapContext lc, boolean isAllServer)
    throws ServiceException {
        Upload up = null ;
        InputStream is = null ;
        //the verification commands are all executed on the local server
        RemoteManager rmgr = RemoteManager.getRemoteManager(prov.getLocalServer());

        try {
            //read the cert file
            ByteArrayOutputStream completeCertChain = new ByteArrayOutputStream(8192);
            CommCert commCert = req.getCommCert();
            if (null == commCert) {
                throw ServiceException.INVALID_REQUEST("commCert element could not be found", null);
            }
            AidAndFilename certInfo = commCert.getCert();
            if (null == certInfo) {
                throw ServiceException.INVALID_REQUEST("comm_cert/cert element could not be found", null);
            }
            String attachId = certInfo.getAttachmentId();
            String filename = certInfo.getFilename();
            ZimbraLog.security.debug("Certificate filename = %s; attid = %s", filename, attachId);

            up = FileUploadServlet.fetchUpload(lc.getAuthtokenAccountId(), attachId, lc.getAuthToken());
            if (up == null) {
                throw ServiceException.FAILURE(
                        String.format("File %s uploaded as %s was not found.", filename, attachId),
                        null);
            }
            is = up.getInputStream() ;
            byte [] cert = ByteUtil.getContent(is, 1024) ;
            ZimbraLog.security.debug("Uploaded the commercial crt to %s", uploadedCertFileName);
            ByteUtil.putContent(uploadedCertFileName, cert);
            is.close();
            completeCertChain.write(cert);
            completeCertChain.write('\n') ;

            //read the root CA
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

            AidAndFilename rootCAinfo = commCert.getRootCA();
            attachId = rootCAinfo.getAttachmentId();
            filename = rootCAinfo.getFilename();

            ZimbraLog.security.debug("Root CA filename = %s; attid = %s", filename, attachId);

            up = FileUploadServlet.fetchUpload(lc.getAuthtokenAccountId(), attachId, lc.getAuthToken());
            if (up == null) {
                throw ServiceException.FAILURE(
                        String.format("File %s uploaded as %s was not found.", filename, attachId),
                        null);
            }
            is = up.getInputStream();
            byte [] rootCA = ByteUtil.getContent(is, 1024) ;
            is.close();

            //read interemediate CA
            byte [] intermediateCA ;
            List<AidAndFilename> intermediateCAlist = commCert.getIntermediateCAs();
            if (null != intermediateCAlist) {
                for (AidAndFilename info : intermediateCAlist) {
                    attachId = info.getAttachmentId();
                    filename = info.getFilename();

                    if (attachId != null && filename != null) {
                        ZimbraLog.security.debug("Intermediate CA filename = %s; attid = %s", filename, attachId);

                        up = FileUploadServlet.fetchUpload(lc.getAuthtokenAccountId(), attachId, lc.getAuthToken());
                        if (up == null)
                            throw ServiceException.FAILURE(
                                    String.format("File %s uploaded as %s was not found.", filename, attachId),
                                    null);
                        is = up.getInputStream();
                        intermediateCA = ByteUtil.getContent(is, 1024);
                        is.close();

                        baos.write(intermediateCA);
                        baos.write('\n');

                        completeCertChain.write(intermediateCA);
                        completeCertChain.write('\n');
                    }
                }
            }

            baos.write(rootCA);
            baos.write('\n');

            byte [] chain = baos.toByteArray() ;
            baos.close();

            completeCertChain.write(rootCA);
            completeCertChain.write('\n');
            completeCertChain.close();

            ZimbraLog.security.debug("Put the uploaded crt chain  to " + uploadedCrtChainFileName);
            ByteUtil.putContent(uploadedCrtChainFileName, chain);

            String privateKey = null;
            if (isAllServer) {
                ZimbraLog.security.debug ("Retrieving zimbraSSLPrivateKey from Global Config.");
                privateKey = prov.getConfig().getSSLPrivateKey();
                //Note: We do this because zmcertmgr don't save the private key to global config
                //since -allserver is not supported by createcsr
                // and deploycrt has to take the hard path of cert and CA chain
                if (privateKey == null || privateKey.length() <= 0) {
                    //permission is denied for the  COMM_CRT_KEY_FILE which is readable to root only
                    //ZimbraLog.security.debug ("Retrieving commercial private key from " + ZimbraCertMgrExt.COMM_CRT_KEY_FILE);
                    //privateKey = new String (ByteUtil.getContent(new File(ZimbraCertMgrExt.COMM_CRT_KEY_FILE))) ;

                    //retrieve the key from the local server  since the key is always saved in the local server when createcsr is called
                    ZimbraLog.security.debug ("Retrieving zimbraSSLPrivateKey from server: " + server.getName());
                    privateKey = server.getSSLPrivateKey();
                }
            } else {
                ZimbraLog.security.debug ("Retrieving zimbraSSLPrivateKey from server: " + server.getName());
                privateKey = server.getSSLPrivateKey();
            }

            if (privateKey != null && privateKey.length() > 0) {
                ZimbraLog.security.debug("Saving zimbraSSLPrivateKey to %s ", savedCommKeyFileName);
            } else {
                 throw ServiceException.FAILURE("zimbraSSLPrivateKey is not present.", new Exception());
            }
            ByteUtil.putContent(savedCommKeyFileName, privateKey.getBytes());

            try {
                // run zmcertmgr verifycrtchain to validate the certificate chain
                String verifychaincmd = String.format("%s %s %s", ZimbraCertMgrExt.VERIFY_CRTCHAIN_CMD,
                        uploadedCrtChainFileName, uploadedCertFileName);
                ZimbraLog.security.debug("*****  Executing the cmd: " + verifychaincmd);
                RemoteResult rr = rmgr.execute(verifychaincmd);
                OutputParser.parseOuput(rr.getMStdout());

                //run zmcertmgr verifycrt to validate the cert and key
                String cmd = String.format("%s %s %s", ZimbraCertMgrExt.VERIFY_CRTKEY_CMD, savedCommKeyFileName,
                        uploadedCertFileName);
                ZimbraLog.security.debug("*****  Executing the cmd: " + cmd);
                rr = rmgr.execute(cmd);

                OutputParser.parseOuput(rr.getMStdout()) ;
                //Certs are validated and Save the uploaded certificate to the LDAP
                String[] zimbraSSLCertificate = { Provisioning.A_zimbraSSLCertificate, completeCertChain.toString() };

                ZimbraLog.security.debug("Save complete cert chain: %s%s", Provisioning.A_zimbraSSLCertificate,
                        completeCertChain.toString());

                if (isAllServer) {
                    prov.modifyAttrs(prov.getConfig(),
                        StringUtil.keyValueArrayToMultiMap(zimbraSSLCertificate, 0), true);
                } else {
                    prov.modifyAttrs(server,
                        StringUtil.keyValueArrayToMultiMap(zimbraSSLCertificate, 0), true);
                }

            } catch (IOException ioe) {
                throw ServiceException.FAILURE("IOException occurred while running cert verification command", ioe);
            }
        } catch (IOException ioe) {
            throw ServiceException.FAILURE("IOException while handling uploaded certificate", ioe);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    ZimbraLog.security.warn("exception closing uploaded certificate:", ioe);
                }
            }
        }
        return true ;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_installCertificate);
    }
}