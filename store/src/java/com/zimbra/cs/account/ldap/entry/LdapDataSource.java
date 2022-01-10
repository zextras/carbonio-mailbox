// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.IAttributes.CheckBinary;
import com.zimbra.cs.ldap.LdapException;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.soap.admin.type.DataSourceType;

/**
 * 
 * @author pshao
 *
 */
public class LdapDataSource extends DataSource implements LdapEntry {

	private String mDn;

	public LdapDataSource(Account acct, String dn, ZAttributes attrs, Provisioning prov) 
	throws LdapException, ServiceException {
		super(acct, getObjectType(attrs),
		        attrs.getAttrString(Provisioning.A_zimbraDataSourceName),
		        attrs.getAttrString(Provisioning.A_zimbraDataSourceId),                
		        attrs.getAttrs(), 
		        prov);
		mDn = dn;
	}
	
	public String getDN() {
		return mDn;
	}

    public static String getObjectClass(DataSourceType type) {
        switch (type) {
            case pop3:
                return AttributeClass.OC_zimbraPop3DataSource;
            case imap:
                return AttributeClass.OC_zimbraImapDataSource;
            case rss:
                return AttributeClass.OC_zimbraRssDataSource;
            case gal:
                return AttributeClass.OC_zimbraGalDataSource;
            default: 
                /*
                 * All DataSource objects that are not pop3, imap, rss or gal are considered 'generic' 
                 * and are represented by 'dataSource' objectClass in LDAP. 
                 * WARNING: avoid adding more LDAP object classes for new implementations of data sources. Use dataSource object class
                 * instead and keep all specifics of implementation in DataImport. 
                 * Any configuration that is not covered by existing attributes can be stored in zimbraDataSourceAttribute or outside of LDAP.
                 */
                return AttributeClass.OC_zimbraDataSource;
        }
    }

    static DataSourceType getObjectType(ZAttributes attrs) throws ServiceException {
        try {
            String dsType = attrs.getAttrString(Provisioning.A_zimbraDataSourceType);
            if (dsType != null)
                return DataSourceType.fromString(dsType);
        } catch (LdapException e) {
            ZimbraLog.datasource.error("cannot get DataSource type", e);
        }
        
        List<String> attr = attrs.getMultiAttrStringAsList(Provisioning.A_objectClass, CheckBinary.NOCHECK);
        if (attr.contains(AttributeClass.OC_zimbraPop3DataSource)) { 
            return DataSourceType.pop3;
        } else if (attr.contains(AttributeClass.OC_zimbraImapDataSource)) {
            return DataSourceType.imap;
        } else if (attr.contains(AttributeClass.OC_zimbraRssDataSource)) {
            return DataSourceType.rss;
        } else if (attr.contains(AttributeClass.OC_zimbraGalDataSource)) {
            return DataSourceType.gal;
        } else if (attr.contains(AttributeClass.OC_zimbraDataSource)) {
            return DataSourceType.unknown;
        } else {
            throw ServiceException.FAILURE("unable to determine data source type from object class", null);
        }
    }
}
