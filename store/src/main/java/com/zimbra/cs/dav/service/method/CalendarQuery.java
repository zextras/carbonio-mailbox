// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.service.method;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Element;
import org.dom4j.QName;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavContext.RequestProp;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.DavException;
import com.zimbra.cs.dav.DavProtocol;
import com.zimbra.cs.dav.caldav.Filter.CompFilter;
import com.zimbra.cs.dav.caldav.Range.ExpandRange;
import com.zimbra.cs.dav.caldav.Range.TimeRange;
import com.zimbra.cs.dav.resource.CalendarCollection;
import com.zimbra.cs.dav.resource.CalendarObject;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.service.DavResponse;

/*
 * http://tools.ietf.org/html/rfc4791#section-7.8 CALDAV:calendar-query REPORT
 * previously - draft-dusseault-caldav section 9.5
 *
 *     <!ELEMENT calendar-query ((DAV:allprop |
 *                                DAV:propname |
 *                                DAV:prop)?, filter, timezone?)>
 *
 */
public class CalendarQuery extends Report {

    @Override
    public void handle(DavContext ctxt) throws DavException, ServiceException {
        Element query = ctxt.getRequestMessage().getRootElement();
        if (!query.getQName().equals(DavElements.E_CALENDAR_QUERY)) {
            throw new DavException("msg "+query.getName()+" is not calendar-query",
                    HttpServletResponse.SC_BAD_REQUEST, null);
        }

        RequestProp reqProp = ctxt.getRequestProp();
        QueryContext qctxt = new QueryContext(ctxt, query, reqProp);

        if (qctxt.componentFilter == null) {
            throw new DavException("missing filter element in the request", HttpServletResponse.SC_BAD_REQUEST, null);
        }

        DavResource rsc = ctxt.getRequestedResource();
        if (!(rsc instanceof CalendarCollection)) {
            throw new DavException("not a calendar resource", HttpServletResponse.SC_BAD_REQUEST, null);
        }

        CalendarCollection cal = (CalendarCollection) rsc;
        TimeRange tr = qctxt.componentFilter.getTimeRange();
        if (tr == null) {
            tr = new TimeRange(rsc.getOwner());
        }

        /**
         * Even if there are no matching items, we should return a DAV:multistatus report
         * http://tools.ietf.org/html/rfc4791#section-7.8 CALDAV:calendar-query REPORT
         * The response body for a successful request MUST be a DAV:multistatus XML element (i.e., the response uses
         * the same format as the response for PROPFIND).  In the case where there are no response elements, the
         * returned DAV:multistatus XML element is empty.
         */
        qctxt.davCtxt.setStatus(DavProtocol.STATUS_MULTI_STATUS);
        DavResponse resp = qctxt.davCtxt.getDavResponse();
        resp.getTop(DavElements.E_MULTISTATUS);
        for (DavResource calItem : cal.getChildren(ctxt, tr)) {
            handleCalendarItem(qctxt, calItem);
        }
    }

    private void handleCalendarItem(QueryContext ctxt, DavResource calItem) {
        if (!(calItem instanceof CalendarObject))
            return;
        try {
            CalendarObject calobj = (CalendarObject)calItem;
            if (!calobj.match(ctxt.componentFilter)) {
                return;
            }
            DavResponse resp = ctxt.davCtxt.getDavResponse();
            if (ctxt.expandRange != null) {
                calobj.expand(ctxt.expandRange);
            }
            resp.addResource(ctxt.davCtxt, calItem, ctxt.props, false);
        } catch (DavException de) {
            ZimbraLog.dav.error("can't get calendar item data", de);
        }
    }

    private static class QueryContext {
        RequestedComponent requestedComponent;
        ExpandRange        expandRange;
        CompFilter         componentFilter;
        DavContext         davCtxt;
        RequestProp        props;

        QueryContext(DavContext ctxt, Element query, RequestProp rp) throws DavException {
            davCtxt = ctxt;
            props = rp;

            for (Object o : query.elements()) {
                if (o instanceof Element) {
                    Element elem = (Element) o;
                    QName name = elem.getQName();
                    if (name.equals(DavElements.E_FILTER)) {
                        parseFilter(elem);
                    } else if (name.equals(DavElements.E_TIMEZONE)) {
                    } else if (name.equals(DavElements.E_PROP)) {
                        for (Object obj : elem.elements())
                            if (obj instanceof Element) {
                                elem = (Element) obj;
                                if (elem.getQName().equals(DavElements.E_CALENDAR_DATA))
                                    parseCalendarData(elem);
                            }
                    }
                }
            }
        }

        /*
         *   <!ELEMENT calendar-data ((comp?, (expand |
         *                                     limit-recurrence-set)?,
         *                                     limit-freebusy-set?) |
         *                            #PCDATA)?>
         */
        private void parseCalendarData(Element cd) {
            // TODO
            // limit-recurrence-set
            // limit-freebusy-set

            // comp
            Element comp = cd.element(DavElements.E_COMP);
            if (comp != null)
                requestedComponent = new RequestedComponent(comp);
            Element expand = cd.element(DavElements.E_EXPAND);
            if (expand != null)
                expandRange = new ExpandRange(expand);
        }

        private void parseFilter(Element filter) {
            for (Object o : filter.elements()) {
                if (o instanceof Element) {
                    Element e = (Element) o;
                    if (e.getQName().equals(DavElements.E_COMP_FILTER))
                        componentFilter = new CompFilter(e);
                }
            }
        }
    }

    /*
     *  <!ELEMENT comp ((allprop | prop*), (allcomp | comp*))>
     *  <!ATTLIST comp name CDATA #REQUIRED>
     */
    private static class RequestedComponent {
        String name;
        Set<String> props;
        Set<RequestedComponent> comps;

        RequestedComponent(Element elem) {
            props = new HashSet<String>();
            comps = new HashSet<RequestedComponent>();
        }

        void parse(Element elem) {
            // TODO handle allprop and allcomp
            name = elem.attributeValue(DavElements.P_NAME);
            for (Object o : elem.elements()) {
                if (o instanceof Element) {
                    Element e = (Element) o;
                    QName qname = e.getQName();
                    if (qname.equals(DavElements.E_PROP))
                        props.add(e.attributeValue(DavElements.P_NAME));
                    else if (qname.equals(DavElements.E_COMP))
                        comps.add(new RequestedComponent(e));
                }
            }
        }
    }
}
