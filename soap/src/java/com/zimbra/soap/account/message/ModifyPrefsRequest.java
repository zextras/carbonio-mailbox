// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.Pref;
import com.zimbra.soap.json.jackson.annotate.ZimbraKeyValuePairs;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Modify Preferences
 * <br />
 * Notes:
 * <br />
 * For multi-value prefs, just add the same attribute with 'n' different values:
 * <pre>
 *      &lt;ModifyPrefsRequest>
 *          &lt;pref name="foo">value1&lt;/pref>
 *          &lt;pref name="foo">value2&lt;/pref>
 *          .
 *          .
 *      &lt;/ModifyPrefsRequest>
 * </pre>
 * <br />
 * You can also add/subtract single values to/from a multi-value pref by prefixing the preference name with 
 * a '+' or '-', respectively in the same way you do when using zmprov. For example:
 * <pre>
 *      &lt;ModifyPrefsRequest>
 *          &lt;pref name="+foo">value1&lt;/pref>
 *          &lt;pref name="-foo">value2&lt;/pref>
 *          .
 *          .
 *      &lt;/ModifyPrefsRequest>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_MODIFY_PREFS_REQUEST)
public class ModifyPrefsRequest {

    /**
     * @zm-api-field-description Specify the preferences to be modified
     */
    @ZimbraKeyValuePairs
    @XmlElement(name=AccountConstants.E_PREF, required=false)
    private List<Pref> prefs = Lists.newArrayList();

    public ModifyPrefsRequest() {
    }

    public void setPrefs(Iterable <Pref> prefs) {
        this.prefs.clear();
        if (prefs != null) {
            Iterables.addAll(this.prefs,prefs);
        }
    }

    public ModifyPrefsRequest addPref(Pref pref) {
        this.prefs.add(pref);
        return this;
    }

    public List<Pref> getPrefs() {
        return Collections.unmodifiableList(prefs);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("prefs", prefs)
            .toString();
    }
}
