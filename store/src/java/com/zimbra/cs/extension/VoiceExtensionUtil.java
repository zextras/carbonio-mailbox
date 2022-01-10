// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

import java.lang.reflect.Method;
import java.util.Set;

import com.zimbra.common.util.ZimbraLog;

public class VoiceExtensionUtil {

    /*
     * VoiceStore itself is in an extension(voice).  Each voice service provider is 
     * also an extension.  Voice service providers must register their VoiceStore 
     * implementation class with VoiceStore.register().  
     * 
     * This helper method facilitates the class loading complications.
     */
    @SuppressWarnings("unchecked")
    public static void registerVoiceProvider(String extension, String providerName,
            String className, Set<String> applicableAttrs) {
        try {
            Class vsClass = ExtensionUtil.findClass("com.zimbra.cs.voice.VoiceStore");
            Method method = vsClass.getMethod("register", String.class, String.class,
                    String.class, Set.class);
            method.invoke(vsClass, extension, providerName, className, applicableAttrs);
        } catch (Exception e) {
            ZimbraLog.extensions.error("unable to register VoiceStore: extension=" + 
                    extension + ", className=" + className, e);
        }
    }
}
