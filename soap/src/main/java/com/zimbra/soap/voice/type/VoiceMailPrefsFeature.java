// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AccountConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMailPrefsFeature extends CallFeatureInfo {

    /**
     * @zm-api-field-description Preferences
     */
    @XmlElement(name=AccountConstants.E_PREF /* pref */, required=false)
    private List<PrefInfo> prefs = Lists.newArrayList();

    public VoiceMailPrefsFeature() {
    }

    public void setPrefs(Iterable <PrefInfo> prefs) {
        this.prefs.clear();
        if (prefs != null) {
            Iterables.addAll(this.prefs, prefs);
        }
    }

    public void addPref(PrefInfo pref) {
        this.prefs.add(pref);
    }

    public List<PrefInfo> getPrefs() {
        return prefs;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("prefs", prefs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
