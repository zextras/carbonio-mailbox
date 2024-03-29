// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.RecurRuleBaseInterface;
import com.zimbra.soap.base.RecurrenceInfoInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class RecurrenceInfo
implements RecurRuleBase, RecurrenceInfoInterface {

    /**
     * @zm-api-field-description Recurrence rules
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_CAL_ADD /* add */, type=AddRecurrenceInfo.class),
        @XmlElement(name=MailConstants.E_CAL_EXCLUDE /* exclude */, type=ExcludeRecurrenceInfo.class),
        @XmlElement(name=MailConstants.E_CAL_EXCEPT /* except */, type=ExceptionRuleInfo.class),
        @XmlElement(name=MailConstants.E_CAL_CANCEL /* cancel */, type=CancelRuleInfo.class),
        @XmlElement(name=MailConstants.E_CAL_DATES /* dates */, type=SingleDates.class),
        @XmlElement(name=MailConstants.E_CAL_RULE /* rule */, type=SimpleRepeatingRule.class)
    })
    private final List<RecurRuleBase> rules = Lists.newArrayList();

    public RecurrenceInfo() {
    }

    public static RecurrenceInfo create(RecurRuleBase rule) {
        RecurrenceInfo ri = new RecurrenceInfo();
        ri.addRule(rule);
        return ri;
    }

    public void setRules(Iterable <RecurRuleBase> rules) {
        this.rules.clear();
        if (rules != null) {
            Iterables.addAll(this.rules,rules);
        }
    }

    public RecurrenceInfo addRule(RecurRuleBase rule) {
        this.rules.add(rule);
        return this;
    }

    public List<RecurRuleBase> getRules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public void setRuleInterfaces(Iterable<RecurRuleBaseInterface> rules) {
        setRules(RecurrenceInfo.fromInterfaces(rules));
    }

    @Override
    public void addRuleInterface(RecurRuleBaseInterface rule) {
        addRule((RecurRuleBase) rule);
    }

    @Override
    public List<RecurRuleBaseInterface> getRuleInterfaces() {
        return RecurrenceInfo.toInterfaces(rules);
    }

    public static Iterable <RecurRuleBase> fromInterfaces(
                    Iterable <RecurRuleBaseInterface> params) {
        if (params == null)
            return null;
        List <RecurRuleBase> newList = Lists.newArrayList();
        for (RecurRuleBaseInterface param : params) {
            newList.add((RecurRuleBase) param);
        }
        return newList;
    }

    public static List <RecurRuleBaseInterface> toInterfaces(
            Iterable <RecurRuleBase> params) {
        if (params == null)
            return null;
        List <RecurRuleBaseInterface> newList = Lists.newArrayList();
        for (RecurRuleBase param : params) {
            newList.add((RecurRuleBaseInterface) param);
        }
        return newList;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("rules", rules);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
    
    public void setRulesAdd(List<AddRecurrenceInfo> addRecurrenceInfos) {
        addRules(addRecurrenceInfos);
    }
    public List<AddRecurrenceInfo> getRulesAdd() {
        return rules.stream()
            .filter(r -> (r instanceof AddRecurrenceInfo))
            .map(r -> (AddRecurrenceInfo) r)
            .collect(Collectors.toList());
    }
    public void setRulesExclude(List<ExcludeRecurrenceInfo> excludeRecurrenceInfos) {
        addRules(excludeRecurrenceInfos);
    }
    public List<ExcludeRecurrenceInfo> getRulesExclude() {
        return rules.stream()
            .filter(r -> (r instanceof ExcludeRecurrenceInfo))
            .map(r -> (ExcludeRecurrenceInfo) r)
            .collect(Collectors.toList());
    }
    public void setRulesExcept(List<ExceptionRuleInfo> exceptionRuleInfos) {
        addRules(exceptionRuleInfos);
    }
    public List<ExceptionRuleInfo> getRulesExcept() {
        return rules.stream()
            .filter(r -> (r instanceof ExceptionRuleInfo))
            .map(r -> (ExceptionRuleInfo) r)
            .collect(Collectors.toList());
    }
    public void setRulesCancel(List<CancelRuleInfo> cancelRuleInfos) {
        addRules(cancelRuleInfos);
    }
    public List<CancelRuleInfo> getRulesCancel() {
        return rules.stream()
            .filter(r -> (r instanceof CancelRuleInfo))
            .map(r -> (CancelRuleInfo) r)
            .collect(Collectors.toList());
    }
    public void setRulesDates(List<SingleDates> singleDates) {
        addRules(singleDates);
    }
    public List<SingleDates> getRulesDates() {
        return rules.stream()
            .filter(r -> (r instanceof SingleDates))
            .map(r -> (SingleDates) r)
            .collect(Collectors.toList());
    }
    public void setRulesSimple(List<SimpleRepeatingRule> simpleRepeatingRules) {
        addRules(simpleRepeatingRules);
    }
    public List<SimpleRepeatingRule> getRulesSimple() {
        return rules.stream()
            .filter(r -> (r instanceof SimpleRepeatingRule))
            .map(r -> (SimpleRepeatingRule) r)
            .collect(Collectors.toList());
    }
    private void addRules(Iterable<? extends RecurRuleBase> recurrenceRule) {
        if (recurrenceRule != null) {
            recurrenceRule.iterator().forEachRemaining(i -> addRule(i));
        }
    }
}
