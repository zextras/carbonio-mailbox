// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Sep 23, 2004
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.account;

import com.zimbra.common.account.ZAttr;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.util.StringUtil;
import java.util.HashMap;
import java.util.Map;

/** AUTO-GENERATED. DO NOT EDIT. */
public abstract class ZAttrCos extends NamedEntry {

  protected ZAttrCos(String name, String id, Map<String, Object> attrs, Provisioning prov) {
    super(name, id, attrs, null, prov);
  }

  ///// BEGIN-AUTO-GEN-REPLACE

  /**
     * attribute constraints TODO: fill all the constraints
     *
     * @param zimbraConstraint new to add to existing values
     * @param attrs existing map to populate, or null to create a new map
     * @return populated map to pass into Provisioning.modifyAttrs
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=766)
    public Map<String,Object> addConstraint(String zimbraConstraint, Map<String,Object> attrs) {
        if (attrs == null) attrs = new HashMap<>();
        StringUtil.addToMultiMap(attrs, "+"  + ZAttrProvisioning.A_zimbraConstraint, zimbraConstraint);
        return attrs;
    }

  /**
     * attribute constraints TODO: fill all the constraints
     *
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 6.0.0_BETA1
     */
    @ZAttr(id=766)
    public void unsetConstraint() throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraConstraint, "");
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * Quota allotted to each data source
     *
     * @return zimbraDataSourceQuota, or 0 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2015)
    public long getDataSourceQuota() {
        return getLongAttr(ZAttrProvisioning.A_zimbraDataSourceQuota, 0L, true);
    }

  /**
     * Quota allotted to all data sources
     *
     * @return zimbraDataSourceTotalQuota, or 0 if unset
     *
     * @since ZCS 8.7.0,9.0.0
     */
    @ZAttr(id=2016)
    public long getDataSourceTotalQuota() {
        return getLongAttr(ZAttrProvisioning.A_zimbraDataSourceTotalQuota, 0L, true);
    }

  /**
     * Zimbra Systems Unique ID
     *
     * @return zimbraId, or null if unset
     */
    @ZAttr(id=1)
    public String getId() {
        return getAttr(ZAttrProvisioning.A_zimbraId, null, true);
    }

  /**
     * System purge policy, encoded as metadata. Users can apply these policy
     * elements to their folders and tags. If the system policy changes, user
     * settings are automatically updated with the change.
     *
     * @return zimbraMailPurgeSystemPolicy, or null if unset
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1239)
    public String getMailPurgeSystemPolicy() {
        return getAttr(ZAttrProvisioning.A_zimbraMailPurgeSystemPolicy, null, true);
    }

    /**
     * System purge policy, encoded as metadata. Users can apply these policy
     * elements to their folders and tags. If the system policy changes, user
     * settings are automatically updated with the change.
     *
     * @param zimbraMailPurgeSystemPolicy new value
     * @throws com.zimbra.common.service.ServiceException if error during update
     *
     * @since ZCS 8.0.0
     */
    @ZAttr(id=1239)
    public void setMailPurgeSystemPolicy(String zimbraMailPurgeSystemPolicy) throws com.zimbra.common.service.ServiceException {
        HashMap<String,Object> attrs = new HashMap<>();
        attrs.put(ZAttrProvisioning.A_zimbraMailPurgeSystemPolicy, zimbraMailPurgeSystemPolicy);
        getProvisioning().modifyAttrs(this, attrs);
    }

  /**
     * List of words to ignore when checking spelling. The word list of an
     * account includes the words specified for its cos and domain.
     *
     * @return zimbraPrefSpellIgnoreWord, or empty array if unset
     *
     * @since ZCS 6.0.5
     */
    @ZAttr(id=1073)
    public String[] getPrefSpellIgnoreWord() {
        String[] value = getMultiAttr(ZAttrProvisioning.A_zimbraPrefSpellIgnoreWord, true, true); return value.length > 0 ? value : new String[] {"blog"};
    }

  ///// END-AUTO-GEN-REPLACE

}
