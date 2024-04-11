// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.ArrayList;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.util.Zimbra;

public abstract class ProvisioningExt {
    
    public abstract static class ProvExt {
        public abstract boolean serverOnly();
        
        public boolean enabled() {
            // skip if the listener needs to run inside the server 
            // and we are not inside the server
            return !(serverOnly() && !Zimbra.started());
        }
    }

    public abstract static class PostCreateAccountListener extends ProvExt {
        public abstract void handle(Account acct) throws ServiceException;
    }
    
    private static final ArrayList<PostCreateAccountListener> 
                postCreateAccountListeners = new ArrayList<>();
    
    public static void addPostCreateAccountListener(PostCreateAccountListener listener) {
        synchronized (postCreateAccountListeners) {
            postCreateAccountListeners.add(listener);
        }
    }
    
    public static ArrayList<PostCreateAccountListener> getPostCreateAccountListeners() {
        return postCreateAccountListeners;
    }
}
