// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

public interface ZimletDesc {
    public void setName(String name);
    public void setVersion(String version);
    public void setDescription(String description);
    public void setExtension(String extension);
    public void setTarget(String target);
    public void setLabel(String label);
    public void setElements(Iterable <Object> elements);
    public void addElement(Object element);

    public String getName();
    public String getVersion();
    public String getDescription();
    public String getExtension();
    public String getTarget();
    public String getLabel();
    public List<Object> getElements();
}
