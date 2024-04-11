// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumBiMap;
import com.zimbra.client.ZFolder.View;
import com.zimbra.common.account.Key;
import com.zimbra.soap.account.type.Identity;
import com.zimbra.soap.account.type.Signature;
import com.zimbra.soap.mail.type.Folder;
import com.zimbra.soap.type.AccountBy;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Converts between {@code com.zimbra.soap} objects and {@code com.zimbra.client} objects.
 */
public class SoapConverter {

  private SoapConverter() {
    throw new IllegalStateException("Utility class");
  }

  private static final BiMap<Folder.View, View> VIEW_MAP;
    private static final BiMap<AccountBy, Key.AccountBy> ACCOUNT_BY_MAP;
    
    static {
        VIEW_MAP = EnumBiMap.create(Folder.View.class, View.class);
        VIEW_MAP.put(Folder.View.UNKNOWN, View.unknown);
        VIEW_MAP.put(Folder.View.APPOINTMENT, View.appointment);
        VIEW_MAP.put(Folder.View.CHAT, View.chat);
        VIEW_MAP.put(Folder.View.CONTACT, View.contact);
        VIEW_MAP.put(Folder.View.CONVERSATION, View.conversation);
        VIEW_MAP.put(Folder.View.MESSAGE, View.message);
        VIEW_MAP.put(Folder.View.REMOTE_FOLDER, View.remote);
        VIEW_MAP.put(Folder.View.SEARCH_FOLDER, View.search);
        VIEW_MAP.put(Folder.View.TASK, View.task);
        
        ACCOUNT_BY_MAP = EnumBiMap.create(AccountBy.class, Key.AccountBy.class);
        ACCOUNT_BY_MAP.put(AccountBy.name , Key.AccountBy.name);
        ACCOUNT_BY_MAP.put(AccountBy.id, Key.AccountBy.id);
        ACCOUNT_BY_MAP.put(AccountBy.adminName, Key.AccountBy.adminName);
        ACCOUNT_BY_MAP.put(AccountBy.foreignPrincipal, Key.AccountBy.foreignPrincipal);
        ACCOUNT_BY_MAP.put(AccountBy.krb5Principal, Key.AccountBy.krb5Principal);
        ACCOUNT_BY_MAP.put(AccountBy.appAdminName, Key.AccountBy.appAdminName);
    }
    
    public static java.util.function.Function<Folder.View, @Nullable View> FROM_SOAP_VIEW = new Function<>() {
      @Override
      public View apply(Folder.View from) {
        View to = VIEW_MAP.get(from);
        return (to != null ? to : View.unknown);
      }
    };
    
    public static java.util.function.Function<View, Folder.@Nullable View> TO_SOAP_VIEW = new Function<>() {
      @Override
      public Folder.View apply(View from) {
        Folder.View to = VIEW_MAP.inverse().get(from);
        return (to != null ? to : Folder.View.UNKNOWN);
      }
    };

    public static java.util.function.Function<Identity, @Nullable ZIdentity> FROM_SOAP_IDENTITY = ZIdentity::new;

    public static java.util.function.Function<ZIdentity, @Nullable Identity> TO_SOAP_IDENTITY = ZIdentity::getData;

    public static java.util.function.Function<Signature, @Nullable ZSignature> FROM_SOAP_SIGNATURE = ZSignature::new;

    public static java.util.function.Function<ZSignature, @Nullable Signature> TO_SOAP_SIGNATURE = ZSignature::getData;
    
    public static java.util.function.Function<AccountBy, Key.@Nullable AccountBy> FROM_SOAP_ACCOUNT_BY =
        new Function<>() {
          @Override
          public Key.AccountBy apply(AccountBy by) {
            return ACCOUNT_BY_MAP.get(by);
          }
        };
    
    public static java.util.function.Function<Key.AccountBy, @Nullable AccountBy> TO_SOAP_ACCOUNT_BY =
        new Function<>() {
          @Override
          public AccountBy apply(Key.AccountBy by) {
            return ACCOUNT_BY_MAP.inverse().get(by);
          }
        };
}
