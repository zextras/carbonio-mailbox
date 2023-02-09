// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.zimbra.common.mime.MimeConstants;

// HTTP protocol extensions and headers for WebDAV
public class DavProtocol {
    public enum Compliance {
        one, two, three,
        update, bind, access_control,
        calendar_access, calendar_auto_schedule, calendar_schedule,
        version_control,
        addressbook, extended_mkcol,
        // Apple extensions
        calendar_proxy,
        calendarserver_principal_property_search
    }

    private static HashMap<Compliance,String> sComplianceStrMap;

    // WEBDAV compliances:
    // Each DAV resources must declare the ones they are compliant to.
    //
    // class 1* : RFC 2518 - all "must" features
    // class 2* : RFC 2518 - locking
    // class 3 : draft-ietf-webdav-rfc2518bis
    // calendar-access* : draft-dusseault-caldav
    // calendar-schedule* : http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-04 and earlier
    //                      Note that 05 and later leading to RFC 6638 do things differently.
    // version-control* : RFC 3253 - section 3.6 (partial requirement from access-control and caldav)
    // checkout-in-place, version-history,
    //   workspace, update, label, working-resource,
    //   merge, baseline, activity, version-controlled-collection : RFC 3253
    // ordered-collections : RFC 3648
    // access-control* : RFC 3744
    // redirectrefs : RFC 4437
    // bind : draft-ietf-webdav-bind
    // calendar-proxy, calendarserver-principal-property-search : Apple extension
    // addressbook-access: draft-daboo-carddav

    static {
        sComplianceStrMap = new HashMap<Compliance,String>();
        sComplianceStrMap.put(Compliance.one, "1");
        sComplianceStrMap.put(Compliance.two, "2");
        sComplianceStrMap.put(Compliance.three, "3");
        sComplianceStrMap.put(Compliance.update, "update");
        sComplianceStrMap.put(Compliance.bind, "bind");
        sComplianceStrMap.put(Compliance.access_control, "access-control");
        sComplianceStrMap.put(Compliance.calendar_access, "calendar-access");
        // RFC6638 Scheduling Extensions to CalDAV
        sComplianceStrMap.put(Compliance.calendar_auto_schedule, "calendar-auto-schedule");
        // draft-desruisseaux-caldav-sched-03 (legacy scheduling)
        sComplianceStrMap.put(Compliance.calendar_schedule, "calendar-schedule");
        sComplianceStrMap.put(Compliance.version_control, "version-control");
        sComplianceStrMap.put(Compliance.calendar_proxy, "calendar-proxy");
        sComplianceStrMap.put(Compliance.calendarserver_principal_property_search, "calendarserver-principal-property-search");
        sComplianceStrMap.put(Compliance.addressbook, "addressbook");
        sComplianceStrMap.put(Compliance.extended_mkcol, "extended-mkcol");
    }

    public static Compliance[] COMPLIANCES = {
        Compliance.one, Compliance.two, Compliance.three,
        Compliance.calendar_access,
        Compliance.calendar_proxy, Compliance.calendarserver_principal_property_search,
        Compliance.access_control, Compliance.addressbook
    };

    public static String getDefaultComplianceString() {
        return getComplianceString(Arrays.asList(COMPLIANCES));
    }

    public static String getComplianceString(Collection<Compliance> comp) {
        if (comp == null)
            return null;
        StringBuilder buf = new StringBuilder();
        for (Compliance c : comp) {
            if (buf.length() > 0)
                buf.append(", ");
            buf.append(sComplianceStrMap.get(c));
        }
        return buf.toString();
    }

    public static final String XML_CONTENT_TYPE = MimeConstants.CT_TEXT_XML;
    public static final String XML_CONTENT_TYPE2 = MimeConstants.CT_TEXT_XML_LEGACY;
    public static final String DAV_CONTENT_TYPE = "text/xml; charset=\"UTF-8\"";
    public static final String VCARD_CONTENT_TYPE = MimeConstants.CT_TEXT_VCARD;
    public static final String DEFAULT_CONTENT_TYPE = MimeConstants.CT_APPLICATION_OCTET_STREAM;
    public static final String ENCODING_GZIP = "gzip";
    public static final String VCARD_VERSION = "3.0";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_ALLOW = "Allow";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_RANGE = "RANGE";
    public static final String HEADER_SCHEDULE_REPLY = "Schedule-Reply";  /* RFC 6638 section 8.1 */

    public static final String NO_CACHE = "no-cache";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_LOCATION = "Location";

    // dav extensions
    public static final String HEADER_DAV = "DAV";
    public static final String HEADER_DEPTH = "Depth";
    public static final String HEADER_DESTINATION = "Destination";
    public static final String HEADER_IF = "If";
    public static final String HEADER_LOCK_TOKEN = "Lock-Token";
    public static final String HEADER_OVERWRITE = "Overwrite";
    public static final String HEADER_BRIEF = "Brief";
    public static final String HEADER_STATUS_URI = "Status-URI";
    public static final String HEADER_TIMEOUT = "Timeout";

    // caldav extensions
    public static final String HEADER_IF_MATCH = "If-Match";
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_ORIGINATOR = "Originator";
    public static final String HEADER_RECIPIENT = "Recipient";

    // Microsoft WebDAV extension
    // http://msdn.microsoft.com/en-us/library/cc250215%28v=PROT.10%29.aspx
    public static final String HEADER_MS_AUTHOR_VIA = "MS-Author-Via";

    public static final int STATUS_PROCESSING = 102;
    public static final int STATUS_MULTI_STATUS = 207;
    public static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    public static final int STATUS_LOCKED = 423;
    public static final int STATUS_FAILED_DEPENDENCY = 424;
    public static final int STATUS_INSUFFICIENT_STORAGE = 507;
}
