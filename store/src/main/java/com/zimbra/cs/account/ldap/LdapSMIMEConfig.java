// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.extension.ExtensionUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

public abstract class LdapSMIMEConfig {

  public static LdapSMIMEConfig getInstance() throws ServiceException {
    return getInstance(null);
  }

  public static LdapSMIMEConfig getInstance(Entry entry) throws ServiceException {
    String className = "com.zimbra.cs.account.ldap.LdapSMIMEConfigImpl";
    LdapSMIMEConfig instance = null;

    try {
      if (entry == null) {
        instance = (LdapSMIMEConfig) ExtensionUtil.findClass(className).newInstance();
      } else {
        instance =
            (LdapSMIMEConfig)
                ExtensionUtil.findClass(className).getConstructor(Entry.class).newInstance(entry);
      }
    } catch (ClassNotFoundException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    } catch (IllegalArgumentException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    } catch (SecurityException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    } catch (InstantiationException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    } catch (IllegalAccessException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    } catch (InvocationTargetException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    } catch (NoSuchMethodException e) {
      throw ServiceException.FAILURE("cannot instantiate " + className, e);
    }

    if (instance == null) {
      throw ServiceException.FAILURE("cannot instantiate " + className, null);
    }

    return instance;
  }

  protected LdapSMIMEConfig() {}

  public abstract Set<String> getAllSMIMEAttributes();

  public abstract Map<String, Map<String, Object>> get(String configName) throws ServiceException;

  public abstract void modify(String configName, Map<String, Object> attrs) throws ServiceException;

  public abstract void remove(String configName) throws ServiceException;

  public interface ResultCallback {
    public void add(String field, String cert);

    public boolean continueWithNextConfig();
  }

  public abstract void lookupPublicKeys(Account acct, String email, ResultCallback resultCallback)
      throws ServiceException;
}
;
