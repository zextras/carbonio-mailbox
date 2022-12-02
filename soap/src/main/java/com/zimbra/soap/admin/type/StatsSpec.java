// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class StatsSpec {

    /**
     * @zm-api-field-tag limit
     * @zm-api-field-description if limit="true" is specified, attempt to reduce result set to under 500 records
     */
    @XmlAttribute(name=AdminConstants.A_LIMIT /* limit */, required=false)
    private final String limit;

    /**
     * @zm-api-field-tag stats-name
     * @zm-api-field-description Name
     */
    @XmlAttribute(name=AdminConstants.A_NAME /* name */, required=false)
    private final String name;

    // Used for Jaxb marshalling
    // Mixed content can contain instances of Element class "values"
    // Text data is represented as java.util.String for text.
    //
    // Note: StatsValueWrapper needs an @XmlRootElement annotation in order
    // to avoid getting a schemagen error:
    /**
     * @zm-api-field-description Either something like:
     * <pre>
     *     &lt;values>&lt;stat name="counter1"/>&lt;stat name="counterN"/>&lt;/values>
     * </pre>
     * or just non-empty text - e.g. "1"
     */
    @XmlElementRefs({
        @XmlElementRef(/* values */ type=StatsValueWrapper.class)
    })
    @XmlMixed
    private List <Object> content;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private StatsSpec() {
        this((String) null, (String) null);
    }

    public StatsSpec(String limit, String name) {
        this.limit = limit;
        this.name = name;
    }

    /**
     * Needed by JAXB.
     */
    private void setContent(List <Object> content) {
        this.content = content;
    }

    /**
     * Needed by JAXB.
     */
    private List <Object> getContent() {
        return content;
    }

    /**
     * Non-JAXB method
     */
    public StatsSpec setStatValues(StatsValueWrapper statValues) {
        if (getContent() == null)
            setContent(Lists.newArrayList());
        for (Object obj : getContent()) {
            if (obj instanceof StatsValueWrapper) {
                getContent().remove(obj);
            }
        }
        this.getContent().add(statValues);
        return this;
    }

    /**
     * Non-JAXB method
     */
    public void setValue(String value) {
        if (getContent() == null)
            setContent(Lists.newArrayList());
        for (Object obj : getContent()) {
            if (obj instanceof String) {
                getContent().remove(obj);
            }
        }
        this.getContent().add(value);
    }

    public String getLimit() { return limit; }
    public String getName() { return name; }

    /**
     * Non-JAXB method
     */
    public StatsValueWrapper getStatValues() {
        for (Object obj : getContent()) {
            if (obj instanceof StatsValueWrapper)
                return (StatsValueWrapper) obj;
        }
        return null;
    }

    /**
     * Non-JAXB method
     */
    public String getValue() {
        if (getContent() == null)
            return null;
        StringBuilder sb = null;
        for (Object obj : getContent()) {
            if (obj instanceof String) {
                if (sb == null)
                    sb = new StringBuilder();
                sb.append((String) obj);
            }
        }
        if (sb == null)
            return null;
        else
            return sb.toString();
    }
}
