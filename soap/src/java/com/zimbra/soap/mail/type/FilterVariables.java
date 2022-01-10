// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"variables"})
@JsonPropertyOrder({ "variables" })
public final class FilterVariables extends FilterAction {

    @XmlElement(name=MailConstants.E_FILTER_VARIABLE /* filterVariable */, required=true)
    private List<FilterVariable> variables;

    /**
     * @return the variables
     */
    @ZimbraJsonArrayForWrapper
    public List<FilterVariable> getVariables() {
        return variables;
    }

    /**
     * @param variables the variables to set
     */
    public void setVariables(Collection<FilterVariable> variables) {
        if(variables != null) {
            if(this.variables == null) {
                this.variables = Lists.newArrayList();
            }
            for (FilterVariable filterVariable : variables) {
                if(filterVariable != null) {
                    this.variables.add(filterVariable);
                }
            }
        }
    }

    /**
     * no-argument constructor wanted by JAXB
     */
    public FilterVariables() {
        this(Lists.newArrayList());
    }

    public FilterVariables(Collection<FilterVariable> variables) {
        this.variables = (ArrayList<FilterVariable>) variables;
    }

    public void addFilterVariable(FilterVariable filterVariable) {
        if(filterVariable != null) {
            if(variables == null) {
                variables = Lists.newArrayList();
            }
            variables.add(filterVariable);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("variables", variables)
            .toString();
    }
}
