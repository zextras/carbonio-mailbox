// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.service.ServiceException;
import org.json.JSONException;
import java.util.regex.Pattern;

public class ZPhone implements ToZJSONObject {

    public static final String INVALID_PHNUM_OWN_PHONE_NUMBER = "voice.INVALID_PHNUM_OWN_PHONE_NUMBER";
    public static final String INVALID_PHNUM_INTERNATIONAL_NUMBER = "voice.INVALID_PHNUM_INTERNATIONAL_NUMBER";
    public static final String INVALID_PHNUM_BAD_NPA = "voice.INVALID_PHNUM_BAD_NPA";
    public static final String INVALID_PHNUM_BAD_LINE = "voice.INVALID_PHNUM_BAD_LINE";
    public static final String INVALID_PHNUM_EMERGENCY_ASSISTANCE = "voice.INVALID_PHNUM_EMERGENCY_ASSISTANCE";
    public static final String INVALID_PHNUM_DIRECTORY_ASSISTANCE = "voice.INVALID_PHNUM_DIRECTORY_ASSISTANCE";
    public static final String INVALID_PHNUM_BAD_FORMAT = "voice.INVALID_PHNUM_BAD_FORMAT";
    public static final String VALID = "voice.OK";
	
    public static final Pattern CHECK_INTERNATIONAL = Pattern.compile("^0\\d*");
    public static final Pattern CHECK_NPA = Pattern.compile("^1?(900)|(500)|(700)|(976)");
    public static final Pattern CHECK_LINE = Pattern.compile("^(1?\\d{3})?555\\d*");
    public static final Pattern CHECK_EMERGENCY_ASSISTANCE = Pattern.compile("^1?911\\d*");
    public static final Pattern CHECK_DIRECTORY_ASSISTANCE = Pattern.compile("^1?411\\d*");
    public static final Pattern CHECK_FORMAT = Pattern.compile("^1?[2-9]\\d{9}$");
    public static final Pattern CHECK_INVALID_CHARS = Pattern.compile(".*[^\\d\\s\\(\\)\\-\\.].*");

    public static final Pattern LEADING_ONE = Pattern.compile("^1");

    private String mName;
    private String mCallerId;

    public ZPhone(String name, String callerId) throws ServiceException {
        mName = name;
        mCallerId = callerId != null && (callerId.equals(name) || callerId.equalsIgnoreCase("Unavailable")) ? null : callerId;
    }

    public ZPhone(String name) throws ServiceException {
        this(name, null);
    }

    public String getName() {
        return mName;
    }

    public String getFullName() {
        return ZPhone.getFullName(mName);
    }

    public String getNonFullName() {
        return ZPhone.getNonFullName(mName);
    }


    public String getDisplay() {
        return ZPhone.getDisplay(mName);
    }

    public String getValidity() {
        return ZPhone.validate(mName);
    }

    public String getCallerId() {
        return mCallerId;
    }

    public ZJSONObject toZJSONObject() throws JSONException {
        ZJSONObject zjo = new ZJSONObject();
        zjo.put("name", mName);
        return zjo;
    }

    public String toString() {
        return ZJSONObject.toString(this);
    }

    public static String getFullName(String number) {
        return ZPhone.LEADING_ONE.matcher(ZPhone.getName(number)).matches() ? number : "1"+number;
    }

    public static String getNonFullName(String number) {
        return ZPhone.LEADING_ONE.matcher(ZPhone.getName(number)).replaceAll("");
    }

    public static String getDisplay(String number) {
        // Handles familiar usa-style numbers only for now...
        if (number == null) {
            return number;
        }
	String name = ZPhone.getName(number);
        int offset = 0;
        boolean doIt = false;
        if (name.length() == 10) {
            doIt = true;
        } else if ((name.length() == 11) && (name.charAt(0) == '1')) {
            doIt = true;
            offset = 1;
        } else if (name.length() == 7) {
            doIt = true;
            offset = -3;
        }
        if (doIt) {
            StringBuilder builder = new StringBuilder();
            if (offset>0) {
                builder.append(name, 0, offset);
                builder.append("-");
            }
            if (offset>-3) {
                builder.append('(');
                builder.append(name, offset, offset + 3);
                builder.append(") ");
            }
            builder.append(name, offset + 3, offset + 6);
            builder.append('-');
            builder.append(name, offset + 6, offset + 10);
            return builder.toString();
        } else {
            return name;
        }
    }

    public static String getName(String display) {
        if (display == null) {
            return display;
        }
        StringBuilder builder = new StringBuilder(display.length());
        for (int i = 0, count = display.length(); i < count; i++) {
            char ch = display.charAt(i);
            if (Character.isDigit(ch)) {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    public static String validate(String number) {

	if (number == null || ZPhone.CHECK_INVALID_CHARS.matcher(number).matches()) {
	    return ZPhone.INVALID_PHNUM_BAD_FORMAT;
	}

        number = ZPhone.getName(number);

        if (number.length() == 0) {
            return ZPhone.INVALID_PHNUM_BAD_FORMAT;
        }

        if (ZPhone.CHECK_INTERNATIONAL.matcher(number).matches()) {
            return ZPhone.INVALID_PHNUM_INTERNATIONAL_NUMBER;
        }
	
        if (ZPhone.CHECK_NPA.matcher(number).matches()) {
            return ZPhone.INVALID_PHNUM_BAD_NPA;
        }
	
        if (ZPhone.CHECK_LINE.matcher(number).matches()) {
            return ZPhone.INVALID_PHNUM_BAD_LINE;
        }
	
        if (ZPhone.CHECK_EMERGENCY_ASSISTANCE.matcher(number).matches()) {
            return ZPhone.INVALID_PHNUM_EMERGENCY_ASSISTANCE;
        }
	
        if (ZPhone.CHECK_DIRECTORY_ASSISTANCE.matcher(number).matches()) {
            return ZPhone.INVALID_PHNUM_DIRECTORY_ASSISTANCE;
        }
	
        if (!ZPhone.CHECK_FORMAT.matcher(number).matches()) {
            return ZPhone.INVALID_PHNUM_BAD_FORMAT;
        }
        return ZPhone.VALID;
    }
}
