// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.property;

import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.QName;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.resource.AddressObject;
import com.zimbra.cs.dav.service.DavServlet;

public class CardDavProperty extends ResourceProperty {

    public static ResourceProperty getAddressbookHomeSet(String user) {
        return new AddressbookHomeSet(user);
    }
    
    public static ResourceProperty getAddressbookData(Element prop, AddressObject contact) {
        return new AddressbookData(prop, contact);
    }
    
    protected CardDavProperty(QName name) {
        super(name);
        setProtected(true);
        setVisible(true);
    }

    private static class AddressbookHomeSet extends CardDavProperty {
        public AddressbookHomeSet(String user) {
            super(DavElements.CardDav.E_ADDRESSBOOK_HOME_SET);
            mChildren.add(createHref(DavServlet.DAV_PATH + "/" + user + "/"));
        }
    }
    
    private static class AddressbookData extends CardDavProperty {
        ArrayList<String> props;
        AddressObject contact;
        public AddressbookData(Element prop, AddressObject c) {
            super(DavElements.CardDav.E_ADDRESS_DATA);
            props = new ArrayList<String>();
            for (Object child : prop.elements()) {
                if (child instanceof Element) {
                    Element e = (Element) child;
                    if (e.getQName().equals(DavElements.CardDav.E_PROP))
                        props.add(e.attributeValue(DavElements.P_NAME));
                }
            }
            contact = c;
        }
        public Element toElement(DavContext ctxt, Element parent, boolean nameOnly) {
            Element abd = super.toElement(ctxt, parent, nameOnly);
            try {
                abd.setText(contact.toVCard(ctxt, props));
            } catch (Exception e) {
                ZimbraLog.dav.warn("can't get vcard content", e);
            }
            return abd;
        }
    }
}
