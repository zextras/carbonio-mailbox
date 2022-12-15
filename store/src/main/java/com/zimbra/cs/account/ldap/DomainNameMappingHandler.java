// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.EntryCacheDataKey;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.extension.ExtensionUtil;

/**
 * 
 * @author pshao
 *
 */
public abstract class DomainNameMappingHandler {
    
    private static Map<String, HandlerInfo> sHandlers = new ConcurrentHashMap<String,HandlerInfo>();
    private static Log sLog = LogFactory.getLog(DomainNameMappingHandler.class);
    
    
    /**
     * Given a foreign name and params, return a zimbra account name
     * 
     * @param foreignName
     * @param params
     * @param zimbraDomainName
     * @return
     */
    public abstract String mapName(String foreignName, String params, String zimbraDomainName) throws ServiceException;
    
    
    private static class HandlerInfo {
        Class<? extends DomainNameMappingHandler> mClass;

        public DomainNameMappingHandler getInstance() {
            DomainNameMappingHandler handler;
            try {
                handler = mClass.newInstance();
            } catch (InstantiationException e) {
                handler = new UnknownDomainNameMappingHandler();
            } catch (IllegalAccessException e) {
                handler = new UnknownDomainNameMappingHandler();
            }
            return handler;
        }
    }
    
    private static HandlerInfo loadHandler(HandlerConfig handlerConfig) {
        HandlerInfo handlerInfo = new HandlerInfo();
        String className = handlerConfig.getClassName();

        try {
            handlerInfo.mClass = ExtensionUtil.findClass(className).asSubclass(DomainNameMappingHandler.class);
        } catch (ClassNotFoundException e) {
            // miss configuration or the extension is disabled
            sLog.warn("Domain name mapping handler %s for application %s not found",
                    className, handlerConfig.getApplicaiton());
            // Fall back to UnknownDomainNameMappingHandler
            handlerInfo.mClass = UnknownDomainNameMappingHandler.class;
        }
        return handlerInfo;
    }
    
    private static DomainNameMappingHandler getHandler(HandlerConfig handlerConfig) {
        String key = handlerConfig.getClassName();
        HandlerInfo handlerInfo = sHandlers.get(key);
        
        if (handlerInfo == null) {
            handlerInfo = loadHandler(handlerConfig);
            sHandlers.put(key, handlerInfo);
        }
        
        return handlerInfo.getInstance();
    }
    
    private static class UnknownDomainNameMappingHandler extends DomainNameMappingHandler {
        @Override
        public String mapName(String foreignName, String params, String zimbraDomainName) {
            return null; // should never be called
        }
    }
    
    public static class HandlerConfig {
        String mApplication;
        String mClassName;
        String mParams;
        
        private HandlerConfig(String application, String className, String params) {
            mApplication = application;
            mClassName = className;
            mParams =params;
        }
        
        private String getApplicaiton() {
            return mApplication;
        }
        
        private String getClassName() {
            return mClassName;
        }
        
        private String getParams() {
            return mParams;
        }
    }
    
    public static HandlerConfig getHandlerConfig(Domain domain, String application) {
        Map<String, HandlerConfig> handlers = 
            (Map<String, HandlerConfig>)domain.getCachedData(EntryCacheDataKey.DOMAIN_FOREIGN_NAME_HANDLERS.getKeyName());
        
        if (handlers == null) {
            handlers = new HashMap<String, HandlerConfig>();
            
            String[] handlersRaw = domain.getForeignNameHandler();
            for (String handlerRaw : handlersRaw) {
                // handlers are in the format of {application}:{class name}[:{params}]
                int idx1 = handlerRaw.indexOf(":");
                if (idx1 != -1) {
                    String app = handlerRaw.substring(0, idx1);
                    String className = handlerRaw.substring(idx1+1);
                    String params = null;
                    int idx2 = className.indexOf(":");
                    if (idx2 != -1) {
                        params = className.substring(idx2+1);
                        className = className.substring(0, idx2);
                    }
                    handlers.put(app, new HandlerConfig(app, className, params));
                }
            }
            
            handlers = Collections.unmodifiableMap(handlers);
            domain.setCachedData(EntryCacheDataKey.DOMAIN_FOREIGN_NAME_HANDLERS.getKeyName(), handlers);
        }
        
        return handlers.get(application);
    }

    public static String mapName(HandlerConfig handlerConfig, String foreignName, String zimbraDomainName) throws ServiceException {
        DomainNameMappingHandler handler = getHandler(handlerConfig);
        
        if (handler instanceof UnknownDomainNameMappingHandler)
            throw ServiceException.FAILURE("unable to load domain name mapping handler " + 
                    handlerConfig.getClassName() + " for application:" + handlerConfig.getApplicaiton(), null);

        return handler.mapName(foreignName, handlerConfig.getParams(), zimbraDomainName);
    }


    static class DummyHandler extends DomainNameMappingHandler {
        public String mapName(String foreignName, String params, String zimbraDomainName) throws ServiceException{
            return "user2@phoebe.mbp";
        }
    }
    
    public static void main(String[] args) throws Exception {

        Provisioning prov = Provisioning.getInstance();
        
        Domain domain = prov.get(Key.DomainBy.name, "phoebe.mbp");
        domain.addForeignName("app1:name1");
        domain.addForeignName("app2:name2");
        domain.addForeignName("app3:name3");
        
        domain.addForeignNameHandler("app1:com.zimbra.cs.account.ldap.DomainNameMappingHandler$DummyHandler:p1, p2, p3");
        
        Account acct;
        
        acct = prov.getAccountByForeignName("user1@name1", "app1", null);
        acct = prov.getAccountByForeignName("user1@name1", "app1", null); // test cache
        System.out.println(acct.getName() + "(expecting user2@phoebe.mbp)");
        
        acct = prov.getAccountByForeignName("user1@name2", "app2", null);
        System.out.println(acct.getName() + "(expecting user1@phoebe.mbp)");

        acct = prov.getAccountByForeignName("user1", "app3", domain);  // with a supplied domain
        System.out.println(acct.getName() + "(expecting user1@phoebe.mbp)");
        
        acct = prov.getAccountByForeignName("user1@name3", "app2", null);
        System.out.println(acct + "(expecting null)");
        
    }
}
