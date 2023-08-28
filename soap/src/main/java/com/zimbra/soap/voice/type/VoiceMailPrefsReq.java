// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.AccountConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMailPrefsReq
implements PhoneVoiceFeaturesSpec.CallFeatureReq {

    /**
     * @zm-api-field-tag pref
     * @zm-api-field-description Preferences
     */
    @XmlElement(name=AccountConstants.E_PREF /* pref */, required=false)
    private List<VoiceMailPrefName> prefs = Lists.newArrayList();

    public VoiceMailPrefsReq() {
    }

    public void setPrefs(Iterable <VoiceMailPrefName> prefs) {
        this.prefs.clear();
        if (prefs != null) {
            Iterables.addAll(this.prefs, prefs);
        }
    }

    public void addPref(VoiceMailPrefName pref) {
        this.prefs.add(pref);
    }

    public List<VoiceMailPrefName> getPrefs() {
        return prefs;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("prefs", prefs);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
