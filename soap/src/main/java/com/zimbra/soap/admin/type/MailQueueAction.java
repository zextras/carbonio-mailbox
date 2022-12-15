// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlMixed;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class MailQueueAction {

    @XmlEnum
    public enum QueueAction {
        // case must match protocol
        hold, release, delete, requeue;
        public static QueueAction fromString(String s)
        throws ServiceException {
            try {
                return QueueAction.valueOf(s);
            } catch (IllegalArgumentException e) {
               throw ServiceException.INVALID_REQUEST("unknown action : " + s +
                    ", valid values: " +
                    Arrays.asList(QueueAction.values()), null);
            }
        }
    }

    @XmlEnum
    public enum QueueActionBy {
        // case must match protocol
        id, query;
        public static QueueActionBy fromString(String s)
        throws ServiceException {
            try {
                return QueueActionBy.valueOf(s);
            } catch (IllegalArgumentException e) {
               throw ServiceException.INVALID_REQUEST("unknown by : " + s +
                    ", valid values: " +
                    Arrays.asList(QueueActionBy.values()), null);
            }
        }
    }

    /**
     * @zm-api-field-tag operation
     * @zm-api-field-description Operation.
     */
    @XmlAttribute(name=AdminConstants.A_OP /* op */, required=true)
    private QueueAction op;

    /**
     * @zm-api-field-tag by
     * @zm-api-field-description By selector.
     * <table>
     * <tr> <td> <b>by</b> </td> <td> Body contains a list of ids </td> </tr>
     * <tr> <td> <b>query</b> </td> <td> Body contains a query element</td> </tr>
     * </table>
     */
    @XmlAttribute(name=AdminConstants.A_BY /* by */, required=true)
    private QueueActionBy by;

    // Used for Jaxb marshalling
    // Mixed content can contain instances of Element class "query"
    // Text data is represented as java.util.String for text.
    //
    // Note: QueueQuery needs an @XmlRootElement annotation in order
    // to avoid schemagen error:
    //  error: Invalid @XmlElementRef :
    //      Type "com.zimbra.soap.admin.type.QueueQuery"
    //      or any of its subclasses are not known to this context.

    /**
     * @zm-api-field-description Either <b>&lt;query></b> element or a list of ids
     */
    @XmlElementRefs({
        @XmlElementRef(/* query */ type=QueueQuery.class)
    })
    @XmlMixed
    private List <Object> content;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MailQueueAction() {
    }

    public MailQueueAction(QueueAction op, QueueActionBy by, String ids,
                QueueQuery query) {
        this.op = op;
        this.by = by;
        content = Lists.newArrayList();
        if ( (by.equals(QueueActionBy.id)) && (ids != null) ) {
            content.add(ids);
        } else if (query != null) {
            content.add(query);
        }
    }

    public String getIds() {
        if (content == null)
            return null;
        if ( by != null && (!by.equals(QueueActionBy.id)) )
            return null;
        for (Object obj : content) {
            if (obj instanceof String)
                return (String) obj;
        }
        return null;
    }

    public QueueQuery getQuery() {
        if (content == null)
            return null;
        if ( by != null && (!by.equals(QueueActionBy.query)) )
            return null;
        for (Object obj : content) {
            if (obj instanceof QueueQuery)
                return (QueueQuery) obj;
        }
        return null;
    }

    public QueueAction getOp() { return op; }
    public QueueActionBy getBy() { return by; }
}
