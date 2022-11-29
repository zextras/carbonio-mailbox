// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.service.UserServletContext;

/**
 * Interface for classes which need to known when a formatter is running
 * For example if a background process needs to suspend while a particular formatting routine occurs
 */
public interface FormatListener {
    /**
     * Called when the format callback begins
     */
    public void formatCallbackStarted(UserServletContext context) throws ServiceException;

    /**
     * Called when the format callback completes
     */
    public void formatCallbackEnded(UserServletContext context) throws ServiceException;

    /**
     * Called when the save callback begins
     */
    public void saveCallbackStarted(UserServletContext context) throws ServiceException;
    
    /**
     * Called when the save callback completes
     */
    public void saveCallbackEnded(UserServletContext context) throws ServiceException;
}
