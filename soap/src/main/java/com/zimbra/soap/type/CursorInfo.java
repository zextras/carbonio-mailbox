// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public final class CursorInfo {

    /**
     * @zm-api-field-tag cursor-prev-id
     * @zm-api-field-description Previous ID. <b>cursor-prev-id</b> and <b>cursor-sort-value</b> and correspond to
     * the last hit on the current page (assuming you're going forward, if you're backing up then they should be the
     * first hit on the current page) or the selected item before changing the sort order.  <b>cursor-sort-value</b>
     * should be set to the value of the 'sf' (SortField) attribute. If you are changing the sort field, don't
     * specify sortVal because 'sf' is sort field dependent. (In this case, the server supplements sortVal using
     * the specified item ID. If the item no longer exist, the cursor gets cleared.) The server uses those attributes
     * to find the spot in the new results that corresponds to your old position: even if some entries have been
     * removed or added to the search results (e.g. if you are searching is:unread and you read some).
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private final String id;

    /**
     * @zm-api-field-tag cursor-sort-value
     * @zm-api-field-description Should be set to the value of the 'sf' (SortField) attribute. See description for
     * <b>cursor-prev-id</b> for more information.
     */
    @XmlAttribute(name=MailConstants.A_SORTVAL /* sortVal */, required=false)
    private final String sortVal;

    /**
     * @zm-api-field-tag cursor-end-sort-value
     * @zm-api-field-description Used for ranges to tell the cursor where to stop (non-inclusive) returning values
     */
    @XmlAttribute(name=MailConstants.A_ENDSORTVAL /* endSortVal */, required=false)
    private final String endSortVal;

    /**
     * @zm-api-field-tag cursor-include-offset
     * @zm-api-field-description If true, the response will include the cursor position (starting from 0) in the
     * entire hits. This can't be used with text queries. Don't abuse this option because this operation is relatively
     * expensive
     */
    @XmlAttribute(name=MailConstants.A_INCLUDE_OFFSET /* includeOffset */, required=false)
    private ZmBoolean includeOffset;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CursorInfo() {
        this(null, null, null);
    }

    public CursorInfo(String id, String sortVal, String endSortVal) {
        this.id = id;
        this.sortVal = sortVal;
        this.endSortVal = endSortVal;
    }

    public static CursorInfo createForIdSortValAndEndSortVal(String id, String sortVal, String endSortVal) {
        return new CursorInfo(id, sortVal, endSortVal);
    }

    public void setIncludeOffset(Boolean includeOffset) { this.includeOffset = ZmBoolean.fromBool(includeOffset); }

    public String getId() { return id; }
    public String getSortVal() { return sortVal; }
    public String getEndSortVal() { return endSortVal; }
    public Boolean getIncludeOffset() { return ZmBoolean.toBool(includeOffset); }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("sortVal", sortVal)
            .add("endSortVal", endSortVal)
            .add("includeOffset", getIncludeOffset());
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
