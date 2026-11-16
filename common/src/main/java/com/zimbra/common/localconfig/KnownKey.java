// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.localconfig;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public final class KnownKey {

    private static final Map<String, KnownKey> ALL = new LinkedHashMap<>();

    static {
        // Since all the known keys are actually defined in another class, we
        // need to make sure that class' static initializer is run.
        LC.init();
    }

    /**
     * Factory method with string default value.
     *
     * @param defaultValue default value
     * @return new instance
     */
    public static KnownKey newKey(String defaultValue) {
        return new KnownKey().setDefault(defaultValue);
    }

    /**
     * Factory method with boolean default value.
     *
     * @param defaultValue default value
     * @return new instance
     */
    public static KnownKey newKey(boolean defaultValue) {
        return new KnownKey().setDefault(String.valueOf(defaultValue));
    }

    /**
     * Factory method with integer default value.
     *
     * @param defaultValue default value
     * @return new instance
     */
    public static KnownKey newKey(int defaultValue) {
        return new KnownKey().setDefault(String.valueOf(defaultValue));
    }

    /**
     * Factory method with long default value.
     *
     * @param defaultValue default value
     * @return new instance
     */
    public static KnownKey newKey(long defaultValue) {
        return new KnownKey().setDefault(String.valueOf(defaultValue));
    }

    /**
     * Factory method with float default value.
     *
     * @param defaultValue default value
     * @return new instance
     */
    public static KnownKey newKey(float defaultValue) {
        return new KnownKey().setDefault(String.valueOf(defaultValue));
    }

    static String[] getAll() {
        return ALL.keySet().toArray(new String[0]);
    }

    static boolean isKnown(String key) {
        return ALL.containsKey(key);
    }

    static KnownKey get(String key) {
        return ALL.get(key);
    }

    static String getDefaultValue(String key) {
        KnownKey kk = ALL.get(key);
        if (kk == null) {
            return null;
        }
        return kk.defaultValue;
    }

    static void expandAll(LocalConfig lc) throws ConfigException {
        String[] keys = KnownKey.getAll();
        for (String key : keys) {
            KnownKey kk = ALL.get(key);
            kk.expand(lc);
        }
    }

    static String getValue(String key) throws ConfigException {
        KnownKey kk = ALL.get(key);
        if (kk == null) {
            return null;
        }
        if (kk.value == null) {
            kk.expand(LocalConfig.getInstance());
        }
        return kk.value;
    }

    public static boolean needForceToEdit(String key) {
        KnownKey kk = ALL.get(key);
        if (kk == null) {
            return false;
        }
        return kk.forceToEdit;
    }

    private String key;
    private String defaultValue;
    private String value; //cached value after expansion
    private boolean forceToEdit;

    /**
     * The only public method here.  If you have a KnownKey object, this
     * is a shortcut to get it's value.
     *
     * @see LC#get
     */
    public String value() {
        assert key != null;
        return LC.get(key);
    }

    public boolean booleanValue() {
        assert key != null;
        String s = LC.get(key);
        if (Strings.isNullOrEmpty(s)) {
            s = defaultValue;  // fallback to the default value
        }
        return Boolean.valueOf(s);
    }

    public int intValue() {
        assert key != null;
        String s = LC.get(key);
        if (Strings.isNullOrEmpty(s)) {
            s = defaultValue; // fallback to the default value
        }
        return Integer.parseInt(s);
    }

    /**
     * Returns the value of this KnownKey as an int, but forces it to be within
     * the range of minValue <= RETURN <= maxValue
     */
    public int intValueWithinRange(int minValue, int maxValue) {
        int result = intValue();
        if (result < minValue) {
            return minValue;
        } else {
            return Math.min(result, maxValue);
        }
    }

    public long longValue() {
        assert key != null;
        String s = LC.get(key);
        if (Strings.isNullOrEmpty(s)) {
            s = defaultValue; // fallback to the default value
        }
        return Long.parseLong(s);
    }

    /**
     * Returns the value of this KnownKey as a long, but forces it to be within
     * the range of minValue <= RETURN <= maxValue
     */
    public long longValueWithinRange(long minValue, long maxValue) {
        long result = longValue();
        if (result < minValue) {
            return minValue;
        } else {
            return Math.min(result, maxValue);
        }
    }

    public String key() {
        return key;
    }

    public void setKey(String name) {
        assert key == null : name;
        assert !ALL.containsKey(name) : name;
        key = name;
        ALL.put(name, this);
    }

    /**
     * You must call {@link #setKey(String)} before using this {@link KnownKey}.
     */
    KnownKey() {
    }

    public KnownKey(String key) {
        this(key, null);
    }



    public KnownKey(String key, String defaultValue) {
        this.key = key;
        if (ALL.containsKey(key)) {
            assert false : "duplicate key: " + key;
        }
        setDefault(defaultValue);
        ALL.put(key, this);
    }

    public KnownKey setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
        this.value = null;
        return this;
    }

    public KnownKey setDefault(long defaultValue) {
        return setDefault(String.valueOf(defaultValue));
    }

    public KnownKey setDefault(boolean defaultValue) {
        return setDefault(String.valueOf(defaultValue));
    }

    public KnownKey setForceToEdit(boolean value) {
        forceToEdit = value;
        return this;
    }

    @VisibleForTesting
    void setValue(String value) {
        this.value = value;
    }

    KnownKey protect() {
        forceToEdit = true;
        return this;
    }



    private void expand(LocalConfig lc) throws ConfigException {
        try {
            value = lc.expand(key, defaultValue);
        } catch (ConfigException e) {
            Logging.error("Can't expand config key " + key + "=" + defaultValue, e);
            throw e;
        }
    }

}
