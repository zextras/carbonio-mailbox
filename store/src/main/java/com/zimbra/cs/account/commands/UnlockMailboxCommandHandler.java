package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.common.soap.SoapHttpTransport;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Console;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.ProvUtil.Exit1Exception;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.LockoutMailboxRequest;
import com.zimbra.soap.admin.message.UnregisterMailboxMoveOutRequest;
import com.zimbra.soap.admin.type.MailboxMoveSpec;
import com.zimbra.soap.type.AccountNameSelector;

import java.io.IOException;

class UnlockMailboxCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public UnlockMailboxCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args)
			throws ServiceException, UsageException, Exit1Exception {
    doUnlockMailbox(args);
  }

  private void doUnlockMailbox(String[] args)
			throws ServiceException, UsageException, Exit1Exception {
    String accountVal = null;
    if (args.length > 1) {
      accountVal = args[1];
    } else {
      provUtil.usage();
      return;
    }

    if (accountVal != null) {
      Account acct = provUtil.lookupAccount(accountVal); // will throw NO_SUCH_ACCOUNT if not found
      Console console = provUtil.getConsole();
      if (!acct.getAccountStatus().isActive()) {
        final String error = String.format(
                "Cannot unlock mailbox for account %s. Account status must be %s."
                        + " Current account status is %s. You must change the value of"
                        + " zimbraAccountStatus to '%s' first",
                accountVal, ZAttrProvisioning.AccountStatus.active, acct.getAccountStatus(), ZAttrProvisioning.AccountStatus.active);
        console.printError(error);
        throw new Exit1Exception();
      }
      String accName = acct.getName();
      String server = acct.getMailHost();
      try {
        sendMailboxLockoutRequest(accName, server, AdminConstants.A_END);
      } catch (ServiceException e) {
        if (ServiceException.UNKNOWN_DOCUMENT.equals(e.getCode())) {
          throw ServiceException.FAILURE(
                  "source server version does not support " + AdminConstants.E_LOCKOUT_MAILBOX_REQUEST,
                  e);
        } else if (ServiceException.NOT_FOUND.equals(
                e.getCode())) { // if mailbox is not locked, move on
          console.printOutput("Warning: " + e.getMessage());
        } else {
          throw e;
        }
      } catch (IOException e) {
        throw ServiceException.FAILURE(
                String.format(
                        "Error sending %s (operation = %s) request for %s to %s",
                        AdminConstants.E_LOCKOUT_MAILBOX_REQUEST, AdminConstants.A_END, accountVal, server),
                e);
      }

      // unregister moveout if hostname is provided
      if (args.length > 2) {
        // set account status to maintenance and lock the mailbox to avoid race conditions
        acct.setAccountStatus(ZAttrProvisioning.AccountStatus.maintenance);
        try {
          sendMailboxLockoutRequest(accName, server, AdminConstants.A_START);
        } catch (IOException e) {
          throw ServiceException.FAILURE(
                  String.format(
                          "Error sending %s (opertion = %s) request for %s to %s.\n"
                                  + " Warning: Account is left in maintenance state!",
                          AdminConstants.E_LOCKOUT_MAILBOX_REQUEST,
                          AdminConstants.A_START,
                          accountVal,
                          server),
                  e);
        }

        // unregister moveout via SOAP
        String targetServer = args[2];
        var prov = provUtil.getProvisioning();
        try {
          UnregisterMailboxMoveOutRequest unregisterReq =
                  UnregisterMailboxMoveOutRequest.create(
                          MailboxMoveSpec.createForNameAndTarget(accName, targetServer));
          String url = URLUtil.getAdminURL(server);
          ZAuthToken token = ((SoapProvisioning) prov).getAuthToken();
          SoapHttpTransport transport = new SoapHttpTransport(url);
          transport.setAuthToken(token);
          transport.invokeWithoutSession(JaxbUtil.jaxbToElement(unregisterReq));
        } catch (ServiceException e) {
          if (ServiceException.UNKNOWN_DOCUMENT.equals(e.getCode())) {
            throw ServiceException.FAILURE(
                    String.format(
                            "target server version does not support %s.",
                            BackupConstants.E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST),
                    e);
          } else {
            throw ServiceException.FAILURE("Failed to unregister mailbox moveout", e);
          }
        } catch (IOException e) {
          throw ServiceException.FAILURE(
                  String.format(
                          "Error sending %s request for %s to %s.",
                          BackupConstants.E_UNREGISTER_MAILBOX_MOVE_OUT_REQUEST, accountVal, server),
                  e);
        } finally {
          // unlock mailbox object and end account maintenance even if failed to
          // unregister moveout
          try {
            sendMailboxLockoutRequest(accName, server, AdminConstants.A_END);
          } catch (ServiceException e) {
            // print error messages, but don't throw any more exceptions, because we
            // have to set account status back to 'active'
            if (ServiceException.UNKNOWN_DOCUMENT.equals(e.getCode())) {
              console.printError(
                      "source server version does not support "
                              + AdminConstants.E_LOCKOUT_MAILBOX_REQUEST);
            } else {
              console.printError(
                      String.format(
                              "Error: failed to unregister mailbox moveout.\n" + " Exception: %s.",
                              e.getMessage()));
            }
          } catch (IOException e) {
            console.printError(
                    String.format(
                            "Error sending %s (operation = %s) request for %s to %s"
                                    + " after unregistering moveout. Exception: %s",
                            AdminConstants.E_LOCKOUT_MAILBOX_REQUEST,
                            AdminConstants.A_END,
                            accountVal,
                            server,
                            e.getMessage()));
          }
          // end account maintenance
          acct.setAccountStatus(ZAttrProvisioning.AccountStatus.active);
        }
      }
    }
  }

  private void sendMailboxLockoutRequest(String acctName, String server, String operation)
          throws ServiceException, IOException {
    LockoutMailboxRequest req =
            LockoutMailboxRequest.create(AccountNameSelector.fromName(acctName));
    req.setOperation(operation);
    String url = URLUtil.getAdminURL(server);
    var prov = provUtil.getProvisioning();
    ZAuthToken token = ((SoapProvisioning) prov).getAuthToken();
    SoapHttpTransport transport = new SoapHttpTransport(url);
    transport.setAuthToken(token);
    transport.invokeWithoutSession(JaxbUtil.jaxbToElement(req));
  }

}
