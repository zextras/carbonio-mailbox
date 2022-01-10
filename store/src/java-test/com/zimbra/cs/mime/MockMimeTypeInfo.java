// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import java.util.HashSet;
import java.util.Set;

/**
 * Mock implementation of {@link MimeTypeInfo} for testing.
 *
 * @author ysasaki
 */
public class MockMimeTypeInfo implements MimeTypeInfo {
    private String[] mimeTypes = new String[0];
    private Set<String> fileExtensions = new HashSet<String>();
    private String description;
    private boolean indexingEnabled;
    private String extension;
    private String handlerClass;
    private int priority;

    @Override
    public String[] getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(String... value) {
        mimeTypes = value;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    public void setExtension(String value) {
        extension = value;
    }

    @Override
    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String value) {
        handlerClass = value;
    }

    @Override
    public boolean isIndexingEnabled() {
        return indexingEnabled;
    }

    public void setIndexingEnabled(boolean value) {
        indexingEnabled = value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    @Override
    public Set<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(String... value) {
        fileExtensions.clear();
        for (String ext : value) {
            fileExtensions.add(ext);
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int value) {
        priority = value;
    }

}
