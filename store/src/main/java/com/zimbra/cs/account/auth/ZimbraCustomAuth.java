// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import java.util.Objects;

public abstract class ZimbraCustomAuth {
    
    private static Map<String, ZimbraCustomAuth> mHandlers;
    
    static {
        /*
         * register known custom auth 
         */
        ZimbraCustomAuth.register("hosted", new HostedAuth());
    }

    /*
     * Register a custom auth handler.
     * It should be invoked from the init() method of ZimbraExtension.
     */
    public static synchronized void register(String handlerName, ZimbraCustomAuth handler) {
        
        if (mHandlers == null)
            mHandlers = new HashMap<>();
        else {
            //  sanity check
            ZimbraCustomAuth obj = mHandlers.get(handlerName);
            if (obj != null) {
                ZimbraLog.account.warn("handler name " + handlerName + " is already registered, " +
                                       "registering of " + obj.getClass().getCanonicalName() + " is ignored");
                return;
            }    
        }
        mHandlers.put(handlerName, handler);
    }

    /**
     * Returns if handler is registered.
     * Given name must not be null + must be registered.
     *
     * @param handlerName name of the handler
     * @return if handler registered
     */
    public static boolean handlerIsRegistered(String handlerName) {
        return !Objects.isNull(handlerName) && mHandlers.containsKey(handlerName);
    }

    public static synchronized ZimbraCustomAuth getHandler(String handlerName) {
        if (mHandlers == null)
            return null;
        else    
            return mHandlers.get(handlerName);
    }
    
    /*
     * Method invoked by the framework to handle authentication requests.
     * A custom auth implementation must implement this abstract method.
     * 
     * @param account: The account object of the principal to be authenticated
     *                 all attributes of the account can be retrieved from this object.
     *                   
     * @param password: Clear-text password.
     * 
     * @param context: Map containing context information.  
     *                 A list of context data is defined in com.zimbra.cs.account.AuthContext
     * 
     * @param args: Arguments specified in the zimbraAuthMech attribute
     * 
     * @return Returning from this function indicating the authentication has succeeded. 
     *  
     * @throws Exception.  If authentication failed, an Exception should be thrown.
     */
    public abstract void authenticate(Account acct, String password, Map<String, Object> context, List<String> args) throws Exception;
    
    /*
     * This function is called by the framework after a successful authenticate.  If 
     * authenticate failed this method won't be invoked.
     *  
     * Overwrite this method to indicate to the framework if checking for password aging 
     * is desired in the custom auth.   Default is false.
     * 
     * It only makes sense to return true for a custom auth if the password is stored in 
     * the Zimbra directory.  
     * 
     * If checkPasswordAging returns false, password aging check will be completely skipped in the framework.
     * If checkPasswordAging returns true, password aging will be executed by the framework if enabled.
     */
    public boolean checkPasswordAging() {
        return false;
    }
}
