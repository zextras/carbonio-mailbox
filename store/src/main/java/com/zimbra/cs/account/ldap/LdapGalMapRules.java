// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.mailbox.ContactConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.grouphandler.GroupHandler;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.ILdapContext;


/*
 * maps LDAP attrs into contact attrs. 
 */
public class LdapGalMapRules {
    
    private List<LdapGalMapRule> mRules;
    private List<String> mLdapAttrs;
    private Set<String> mBinaryLdapAttrs; // attrs need to be set in JNDI "java.naming.ldap.attributes.binary" environment property
    private Map<String, LdapGalValueMap> mValueMaps;
    private GroupHandler mGroupHandler;
    private boolean mFetchGroupMembers;
    private boolean mNeedSMIMECerts;
    private static final String OLD_DEFAULT_GROUPHANDLER = "com.zimbra.cs.gal.ADGalGroupHandler";
    private static final String CURRENT_DEFAULT_GROUPHANDLER = "com.zimbra.cs.account.grouphandler.ADGroupHandler";

    public LdapGalMapRules(String[] rules, String[] valueMaps, String groupHandlerClass) {
        init(rules, valueMaps, groupHandlerClass);
    }
    
    public LdapGalMapRules(Config config, boolean isZimbraGal) {
        init(config, isZimbraGal);
    }
    
    public LdapGalMapRules(Domain domain, boolean isZimbraGal) {
        init(domain, isZimbraGal);
    }

    private void init(Entry entry, boolean isZimbraGal) {
        String groupHanlderClass = null;
        if (!isZimbraGal)
            groupHanlderClass = entry.getAttr(Provisioning.A_zimbraGalLdapGroupHandlerClass);
        
        init(entry.getMultiAttr(Provisioning.A_zimbraGalLdapAttrMap),
             entry.getMultiAttr(Provisioning.A_zimbraGalLdapValueMap), 
             groupHanlderClass);
    }
    
    private void init(String[] rules, String[] valueMaps, String groupHandlerClass) {
        if (valueMaps !=  null) {
            mValueMaps = new HashMap<String, LdapGalValueMap>(valueMaps.length);
            for (String valueMap : valueMaps) {
                LdapGalValueMap vMap = new LdapGalValueMap(valueMap);
                mValueMaps.put(vMap.getFieldName(), vMap);
            }
        }
        
        mRules = new ArrayList<LdapGalMapRule>(rules.length);
        mLdapAttrs = new ArrayList<String>();
        mBinaryLdapAttrs = new HashSet<String>();
        for (String rule: rules)
            add(rule);
        // load the correct default group handler class (bug 78755)
        if (StringUtil.equal(groupHandlerClass, OLD_DEFAULT_GROUPHANDLER)) {
            groupHandlerClass = CURRENT_DEFAULT_GROUPHANDLER;
        }
        mGroupHandler = GroupHandler.getHandler(groupHandlerClass);
        ZimbraLog.gal.debug("groupHandlerClass=" + groupHandlerClass + ", handler instantiated=" + mGroupHandler.getClass().getCanonicalName());
    }
    
    public void setFetchGroupMembers(boolean fetchGroupMembers) {
        mFetchGroupMembers = fetchGroupMembers;
    }
    
    public void setNeedSMIMECerts(boolean needSMIMECerts) {
        mNeedSMIMECerts = needSMIMECerts;
    }
    
    public String[] getLdapAttrs() {
        return mLdapAttrs.toArray(new String[mLdapAttrs.size()]);
    }
    
    // attrs need to be set in JNDI "java.naming.ldap.attributes.binary" environment property
    public Set<String> getBinaryLdapAttrs() {
        return mBinaryLdapAttrs;
    }
    
    public Map<String, Object> apply(ILdapContext ldapContext, String searchBase, String entryDN, IAttributes ldapAttrs) {
         
        HashMap<String,Object> contactAttrs = new HashMap<String, Object>();        
        for (LdapGalMapRule rule: mRules) {
        	if (!mNeedSMIMECerts && rule.isSMIMECertificate()) {
        		continue;
        	}
            rule.apply(ldapAttrs, contactAttrs);
        }
        
        if (mGroupHandler.isGroup(ldapAttrs)) {
            try {
                if (mFetchGroupMembers) {
                    contactAttrs.put(ContactConstants.A_member, mGroupHandler.getMembers(ldapContext, searchBase, entryDN, ldapAttrs));
                } else {
                    // for internal LDAP, all members are on the DL entry and have been fetched/mapped
                    // delete it.
                    contactAttrs.remove(ContactConstants.A_member);
                }
                contactAttrs.put(ContactConstants.A_type, ContactConstants.TYPE_GROUP);
            } catch (ServiceException e) {
                ZimbraLog.gal.warn("unable to retrieve group members ", e);
            }
        }
        
        return contactAttrs;
    }
    
    public void add(String rule) {
        LdapGalMapRule lgmr = new LdapGalMapRule(rule, mValueMaps);
        mRules.add(lgmr);
        for (String ldapattr: lgmr.getLdapAttrs()) {
            mLdapAttrs.add(ldapattr);
            
            if (lgmr.isBinary()) {
                mBinaryLdapAttrs.add(ldapattr);
            }
        }
    }
}
