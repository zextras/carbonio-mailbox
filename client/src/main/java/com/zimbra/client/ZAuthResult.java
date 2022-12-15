// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.util.List;
import java.util.Map;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.util.MapUtil;
import com.zimbra.soap.account.message.AuthResponse;
import com.zimbra.soap.account.type.Session;
import com.zimbra.soap.type.ZmBoolean;

public class ZAuthResult {

    private long expires;
    private long trustExpires;
    private boolean twoFactorAuthRequired;
    private boolean trustedDevicesEnabled;
    private AuthResponse data;

    /*
    public ZAuthResult(Element e) throws ServiceException {
        mAuthToken = new ZAuthToken(e.getElement(AccountConstants.E_AUTH_TOKEN), false);

        mLifetime = e.getAttributeLong(AccountConstants.E_LIFETIME);
        mExpires = System.currentTimeMillis() + mLifetime;
        mRefer = e.getAttribute(AccountConstants.E_REFERRAL, null);
        mAttrs = ZGetInfoResult.getMap(e, AccountConstants.E_ATTRS, AccountConstants.E_ATTR);
        mPrefs = ZGetInfoResult.getMap(e, AccountConstants.E_PREFS, AccountConstants.E_PREF);
        mSkin = e.getAttribute(AccountConstants.E_SKIN, null);
    }
    */

    public ZAuthResult(AuthResponse res) {
        data = res;
        expires = data.getLifetime() + System.currentTimeMillis();
        if (data.getTrustedToken() != null) {
            trustExpires = data.getTrustLifetime() + System.currentTimeMillis();
        }
        twoFactorAuthRequired = ZmBoolean.toBool(data.getTwoFactorAuthRequired(), false);
        trustedDevicesEnabled = ZmBoolean.toBool(data.getTrustedDevicesEnabled(), false);
    }

    public ZAuthToken getAuthToken() {
        return new ZAuthToken(data.getAuthToken());
    }

    public String getSessionId() {
        Session session = data.getSession();
        if (session == null) {
            return null;
        }
        return session.getId();
    }

    void setSessionId(String id) {
        Session session = data.getSession();
        if (session == null) {
            session = new Session();
            data.setSession(session);
        }
        session.setId(id);
    }

    public long getExpires() {
        return expires;
    }

    public long getLifetime() {
        return data.getLifetime();
    }

    public String getRefer() {
        return data.getRefer();
    }

    public Map<String, List<String>> getAttrs() {
        return MapUtil.multimapToMapOfLists(data.getAttrsMultimap());
    }

    public Map<String, List<String>> getPrefs() {
        return MapUtil.multimapToMapOfLists(data.getPrefsMultimap());
    }

    public String getSkin() {
        return data.getSkin();
    }

    public String getCsrfToken() {
        return data.getCsrfToken();
    }

    public String getTrustedToken() {
        return data.getTrustedToken();
    }

    public long getTrustLifetime() {
        return trustExpires;
    }

    public String getDeviceId() {
        return data.getDeviceId();
    }

    public boolean getTwoFactorAuthRequired() {
        return twoFactorAuthRequired;
    }

    public boolean getTrustedDevicesEnabled() {
        return trustedDevicesEnabled;
    }
}
