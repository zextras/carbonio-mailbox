// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.EmailInfoInterface;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class EmailInfo
implements EmailInfoInterface {

    /**
     * @zm-api-field-tag email-address
     * @zm-api-field-description the user@domain part of an email address
     */
    @XmlAttribute(name=MailConstants.A_ADDRESS /* a */, required=false)
    private final String address;

    /**
     * @zm-api-field-tag display-name
     * @zm-api-field-description Display name. If we have personal name, first word in "word1 word2" format, or last
     * word in "word1, word2" format.  If no personal name, take string before "@" in email-address.
     */
    @XmlAttribute(name=MailConstants.A_DISPLAY /* d */, required=false)
    private final String display;

    /**
     * @zm-api-field-tag personal name
     * @zm-api-field-description The comment/name part of an address
     */
    @XmlAttribute(name=MailConstants.A_PERSONAL /* p */, required=false)
    private final String personal;

    /**
     * @zm-api-field-tag address-type
     * @zm-api-field-description Address type.
     * <br />
     * <b>{address-type}</b> = (f)rom, (t)o, (c)c, (b)cc, (r)eply-to, (s)ender, read-receipt (n)otification,
     * (rf) resent-from
     * <br />
     * <br />
     * Type is only sent when an individual message is returned. In the list of conversations, all the email
     * addresseses returned for a conversation are a subset of the participants. In the list of messages in a
     * converstation, the email addressses are the senders.
     * <br />
     * <br />
     * Note that "rf" addresses can only be <b>returned</b> on a message; when sending a message, "rf" is ignored
     */
    @XmlAttribute(name=MailConstants.A_ADDRESS_TYPE /* t */, required=false)
    private final String addressType;

    /**
     * @zm-api-field-tag email-addr-is-group
     * @zm-api-field-description Set if the email address is a group
     */
    @XmlAttribute(name=MailConstants.A_IS_GROUP /* isGroup */, required=false)
    private ZmBoolean group;

    /**
     * @zm-api-field-tag can-expand-group-members
     * @zm-api-field-description Flags whether can expand group members
     * <table>
     * <tr> <td> <b>1 (true)</b> </td> <td> authed user can (has permission to) expand members in this group </td> </tr>
     * <tr> <td> <b>0 (false)</b> </td> <td> authed user does not have permission to expand group members </td> </tr>
     * </table>
     * Note: Present only when <b>{email-addr-is-group}</b> is set.
     */
    @XmlAttribute(name=MailConstants.A_EXP /* exp */, required=false)
    private ZmBoolean canExpandGroupMembers;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private EmailInfo() {
        this(null, null, null, null);
    }

    public EmailInfo(String address, String display, String personal,
            String addressType) {
        this.address = address;
        this.display = display;
        this.personal = personal;
        this.addressType = addressType;
    }

    @Override
    public EmailInfoInterface create(String address, String display,
            String personal, String addressType) {
        return new EmailInfo (address, display, personal, addressType);
    }

    @Override
    public void setGroup(Boolean group) { this.group = ZmBoolean.fromBool(group); }
    @Override
    public void setCanExpandGroupMembers(Boolean canExpandGroupMembers) {
        this.canExpandGroupMembers = ZmBoolean.fromBool(canExpandGroupMembers);
    }

    @Override
    public String getAddress() { return address; }
    @Override
    public String getDisplay() { return display; }
    @Override
    public String getPersonal() { return personal; }
    @Override
    public String getAddressType() { return addressType; }
    @Override
    public Boolean getGroup() { return ZmBoolean.toBool(group); }
    @Override
    public Boolean getCanExpandGroupMembers() { return ZmBoolean.toBool(canExpandGroupMembers); }

    public static Iterable <EmailInfo> fromInterfaces(Iterable <EmailInfoInterface> ifs) {
        if (ifs == null) {
            return null;
        }
        final List <EmailInfo> newList = Lists.newArrayList();
        for (final EmailInfoInterface listEnt : ifs) {
            newList.add((EmailInfo) listEnt);
        }
        return newList;
    }

    public static List <EmailInfoInterface> toInterfaces(Iterable <EmailInfo> params) {
        if (params == null) {
            return null;
        }
        final List <EmailInfoInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("address", address)
            .add("display", display)
            .add("personal", personal)
            .add("addressType", addressType)
            .add("group", group)
            .add("canExpandGroupMembers", canExpandGroupMembers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
