// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface ZimletDesc {
    void setName(String name);
    void setVersion(String version);
    void setDescription(String description);
    void setExtension(String extension);
    void setTarget(String target);
    void setLabel(String label);
    void setElements(Iterable<Object> elements);
    void addElement(Object element);

    String getName();
    String getVersion();
    String getDescription();
    String getExtension();
    String getTarget();
    String getLabel();
    List<Object> getElements();
}
