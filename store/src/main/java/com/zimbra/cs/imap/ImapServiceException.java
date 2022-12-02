// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.service.ServiceException;

public class ImapServiceException extends ServiceException {
    private static final long serialVersionUID = -8493068960356614305L;

    public static final String CANT_DELETE_SYSTEM_FOLDER = "imap.CANNOT_DELETE_SYSTEM_FOLDER";
    public static final String CANT_RENAME_INBOX = "imap.CANNOT_RENAME_INBOX";
    public static final String FOLDER_NOT_VISIBLE = "imap.FOLDER_NOT_VISIBLE";
    public static final String FOLDER_NOT_WRITABLE = "imap.FOLDER_NOT_WRITABLE";

    public static final String FOLDER_NAME = "folderName";

    private ImapServiceException(String message, String code, boolean isReceiversFault, Argument... args) {
        super(message, code, isReceiversFault, args);
    }
    
//  private ImapServiceException(String message, String code, boolean isReceiversFault, Throwable cause, Argument... args) {
//      super(message, code, isReceiversFault, cause, args);
//  }

    public static ImapServiceException CANNOT_DELETE_SYSTEM_FOLDER(String folderName) {
        return new ImapServiceException("cannot delete system folder: ", CANT_DELETE_SYSTEM_FOLDER, SENDERS_FAULT, new Argument(FOLDER_NAME, folderName, Argument.Type.STR));
    }

    public static ImapServiceException CANT_RENAME_INBOX() {
        return new ImapServiceException("Rename of INBOX not supported", CANT_RENAME_INBOX, SENDERS_FAULT);
    }

    public static ImapServiceException FOLDER_NOT_VISIBLE(String folderName) {
        return new ImapServiceException("folder not visible: ", FOLDER_NOT_VISIBLE, SENDERS_FAULT, new Argument(FOLDER_NAME, folderName, Argument.Type.STR)); 
    }

    public static ImapServiceException FOLDER_NOT_WRITABLE(String folderName) {
        return new ImapServiceException("folder is READ-ONLY: ", FOLDER_NOT_WRITABLE, SENDERS_FAULT, new Argument(FOLDER_NAME, folderName, Argument.Type.STR)); 
    }
}
