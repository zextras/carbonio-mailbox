// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 14, 2005
 *
 */
package com.zimbra.cs.mime;

import java.util.Set;

public interface MimeTypeInfo {
    
    /**
     * Returns the associated MIME types.  The MIME type can be a regular expression.
     */
    String[] getMimeTypes();
    
    /**
     * Gets the name of the extension where the handler class is defined.
     * If it is part of the core, return null.
     * @return
     */
    String getExtension();
    
    /**
     * Gets the name of the handler class. If no package is specified, 
     * com.zimbra.cs.mime.handler is assumed. 
     * @return
     */
    String getHandlerClass();
    
    /**
     * Whether the content is to be indexed for this mime type.
     * @return
     */
    boolean isIndexingEnabled();
    
    /**
     * Gets the description of the mime type
     * @return
     */
    String getDescription();
    
    /**
     * Returns the <tt>Set</tt> of file extensions.  Extensions are returned
     * in lower case.
     */
    Set<String> getFileExtensions();

    /**
     * Gets the priority.  In the case where multiple <tt>MimeTypeInfo</tt>s
     * match a search, the one with the highest priority wins.
     */
    int getPriority();
}
