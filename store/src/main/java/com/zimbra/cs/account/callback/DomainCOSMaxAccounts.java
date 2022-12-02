// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;

public class DomainCOSMaxAccounts extends AttributeCallback {

    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue,
            Map attrsToModify, Entry entry)
    throws ServiceException {
        
        String attr = Provisioning.A_zimbraDomainCOSMaxAccounts;
        String addAttr = "+" + attr;
        String delAttr = "-" + attr;
            
        Map<String, String> cur = new HashMap<String, String>();
        
        // parse existing values, if ther are any
        if (entry != null) {
            Set<String> curValues = entry.getMultiAttrSet(attr);
            for (String v : curValues) {
                // if somehow there is a bad existing value, ignore it
                Pair<String, String> parsed = parse(v, false);
                if (parsed != null)
                    cur.put(parsed.getFirst(), parsed.getSecond());
            }
        }
            
        // go through the attrsToModify once first to check all removing ones
        for (Object e : attrsToModify.entrySet()) {       
            Map.Entry<String, Object> keyVal = (Map.Entry<String, Object>)e; 
            String aName = keyVal.getKey();
            List<String> vals = getMultiValue(keyVal.getValue());
                
            if (delAttr.equals(aName)) {
                // removing
                for (String v : vals) {
                    if (v.length() == 0)
                        continue;
                    
                    Pair<String, String> parsed = parse(v, true);
                    cur.remove(parsed.getFirst());
                }
            }
        }
        
        // go through the attrsToModify again to check dups for replacing and adding
        for (Object e : attrsToModify.entrySet()) {       
            Map.Entry<String, Object> keyVal = (Map.Entry<String, Object>)e; 
            String aName = keyVal.getKey();
            List<String> vals = getMultiValue(keyVal.getValue());
                
            if (attr.equals(aName)) {
                // replacing
                checkDup(new HashMap<String, String>(), vals);
            } else if (addAttr.equals(aName)) {
                // adding
                checkDup(cur, vals);
            } 
        }
    }
    
    private void checkDup(Map<String, String> curVals, List<String> newVals) throws ServiceException {
        for (String v : newVals) {
            if (v.length() == 0)
                continue;
            
            Pair<String, String> parsed = parse(v, true);
            String other = curVals.get(parsed.getFirst());
            if (other != null)
                throw ServiceException.INVALID_REQUEST("cannot contain multiple values for the same cos " + parsed.getFirst() + 
                        ": " + parsed.getSecond() + ", " + other, null);
            else
                curVals.put(parsed.getFirst(), parsed.getSecond());
        }
    }
    
    private Pair<String, String> parse(String value, boolean throwOnError) throws ServiceException {
        String[] parts = value.split(":");
        if (parts.length != 2) {
            if (throwOnError)
                throw ServiceException.INVALID_REQUEST("invalid format", null);
            else
                return null;
        }
        return new Pair<String, String>(parts[0], parts[1]);
    }

    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }

}
