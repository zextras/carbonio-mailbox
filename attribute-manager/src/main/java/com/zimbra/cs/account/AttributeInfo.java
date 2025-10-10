// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.cs.account.util.DateUtil;
import com.zimbra.cs.account.util.StringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AttributeInfo {

    private static final Logger logger = Logger.getLogger(AttributeInfo.class.getName());

    public static String DURATION_PATTERN_DOC =
        "Must be in valid duration format: {digits}{time-unit}.  " +
        "digits: 0-9, time-unit: [hmsd]|ms.  " +
        "h - hours, m - minutes, s - seconds, d - days, ms - milliseconds.  " +
        "If time unit is not specified, the default is s(seconds).";

    /** attribute name */
    protected String mName;

    /** attribute type */
    protected AttributeType mType;

    /** sort order */
    private final AttributeOrder mOrder;

    // LinkedHashSet used to increase predictability of generated source files

    public LinkedHashSet<String> getmEnumSet() {
        return mEnumSet;
    }

    /** for enums */
    private LinkedHashSet<String> mEnumSet;

    public Pattern getmRegex() {
        return mRegex;
    }

    /** for regex */
    private Pattern mRegex;

    /** for holding initial value string */
    private final String mValue;

    /** attribute callback */
    private final String mCallbackClassName;

    public boolean ismImmutable() {
        return mImmutable;
    }

    /** whether this attribute can be modified directly */
    private final boolean mImmutable;

    private final AttributeCardinality mCardinality;

    private final Set<AttributeClass> mRequiredInClasses;

    private final Set<AttributeClass> mOptionalInClasses;

    private final Set<AttributeFlag> mFlags;

    private final List<String> mGlobalConfigValues;

    private final List<String> mGlobalConfigValuesUpgrade;

    protected List<String> mDefaultCOSValues;

    private final List<String> mDefaultExternalCOSValues;

    private final List<String> mDefaultCOSValuesUpgrade;

    public long getmMin() {
        return mMin;
    }

    private long mMin = Long.MIN_VALUE;

    public long getmMax() {
        return mMax;
    }

    private long mMax = Long.MAX_VALUE;

    public String getmMinDuration() {
        return mMinDuration;
    }

    public static String getDurationPatternDoc() {
        return DURATION_PATTERN_DOC;
    }

    private String mMinDuration = null;

    public String getmMaxDuration() {
        return mMaxDuration;
    }

    private String mMaxDuration = null;

    private final int mId;

    private final String mParentOid;

    private final int mGroupId;

    private final String mDescription;

    private final List<AttributeServerType> mRequiresRestart;

    private final List<AttributeVersion> mSince;

    private final AttributeVersion mDeprecatedSince;

    private static Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long parseLong(String attrName, String propName, String value, long defaultValue) {
        if (!StringUtil.isNullOrEmpty(value)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to %d.",
                        value, propName, attrName, defaultValue));
                return defaultValue;
            }
        } else
            return defaultValue;
    }


    @VisibleForTesting
    public AttributeInfo(
            String attrName, int id, String parentId, int groupId,
            String callbackClassName, AttributeType type, AttributeOrder order,
            String value, boolean immutable, String min, String max,
            AttributeCardinality cardinality, Set<AttributeClass> requiredIn,
            Set<AttributeClass> optionalIn, Set<AttributeFlag> flags,
            List<String> globalConfigValues, List<String> defaultCOSValues,
            List<String> defaultExternalCOSValues, List<String> globalConfigValuesUpgrade,
            List<String> defaultCOSValuesUpgrade, String description, List<AttributeServerType> requiresRestart,
            List<AttributeVersion> since, AttributeVersion deprecatedSince) {
        mName = attrName;
			mCallbackClassName = callbackClassName;
			mImmutable = immutable;
        mType = type;
        mOrder = order;
        mValue = value;
        mId = id;
        mParentOid = parentId;
        mGroupId = groupId;
        mCardinality = cardinality;
        mRequiredInClasses = requiredIn;
        mOptionalInClasses = optionalIn;
        mFlags = flags;
        mGlobalConfigValues = globalConfigValues;
        mGlobalConfigValuesUpgrade = globalConfigValuesUpgrade;
        mDefaultCOSValues = defaultCOSValues;
        mDefaultExternalCOSValues = defaultExternalCOSValues;
        mDefaultCOSValuesUpgrade = defaultCOSValuesUpgrade;
        mDescription = description;
        mRequiresRestart = requiresRestart;
        mSince = since;
        if (mSince != null && mSince.size() > 1) {
            //just in case someone specifies order incorrectly
            Collections.sort(mSince);
        }
        mDeprecatedSince = deprecatedSince;

        mMin = parseLong(attrName, AttributeManager.A_MIN, min, Long.MIN_VALUE);
        mMax = parseLong(attrName, AttributeManager.A_MAX, max, Long.MAX_VALUE);

        switch (mType) {
        case TYPE_INTEGER:
            mMin = Integer.MIN_VALUE;
            mMax = Integer.MAX_VALUE;

            if (!StringUtil.isNullOrEmpty(min)) {
                Integer i = parseInt(min);
                if (i == null) {
                    logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to %d.",
                        min, AttributeManager.A_MIN, attrName, mMin));
                } else {
                    mMin = i;
                }
            }
            if (!StringUtil.isNullOrEmpty(max)) {
                Integer i = parseInt(max);
                if (i == null) {
                    logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to %d.",
                        max, AttributeManager.A_MAX, attrName, mMax));
                } else {
                    mMax = i;
                }
            }
            break;
        case TYPE_LONG:
            mMin = Long.MIN_VALUE;
            mMax = Long.MAX_VALUE;

            if (!StringUtil.isNullOrEmpty(min)) {
                Long l = parseLong(min);
                if (l == null) {
                    logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to %d.",
                        min, AttributeManager.A_MIN, attrName, mMin));
                } else {
                    mMin = l;
                }
            }
            if (!StringUtil.isNullOrEmpty(max)) {
                Long l = parseLong(max);
                if (l == null) {
                    logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to %d.",
                        max, AttributeManager.A_MAX, attrName, mMax));
                } else {
                    mMax = l;
                }
            }
            break;
        case TYPE_ENUM:
            String[] enums = value.split(",");
            mEnumSet = new LinkedHashSet<>(enums.length);
          mEnumSet.addAll(Arrays.asList(enums));
            break;
        case TYPE_REGEX:
            mRegex = Pattern.compile(value);
            break;
        case TYPE_DURATION:
            mMin = 0;
            mMax = Long.MAX_VALUE;
            mMinDuration = "0";
            mMaxDuration = Long.toString(mMax);

            if (!StringUtil.isNullOrEmpty(min)) {
                mMin = DateUtil.getTimeInterval(min, -1);
                if (mMin < 0) {
                    mMin = 0;
                    logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to 0.",
                        min, AttributeManager.A_MIN, attrName));
                } else {
                    mMinDuration = min;
                }
            }
            if (!StringUtil.isNullOrEmpty(max)) {
                mMax = DateUtil.getTimeInterval(max, -1);
                if (mMax < 0) {
                    mMax = Long.MAX_VALUE;
                    logger.warning(String.format("Invalid value '%s' for property %s of attribute %s.  Defaulting to %d.",
                        max, AttributeManager.A_MAX, attrName, mMax));
                } else {
                    mMaxDuration = max;
                }
            }
            break;
        }
    }

    public int getEnumValueMaxLength() {
        assert(mType == AttributeType.TYPE_ENUM);
        int max = 0;
        for (String s : mEnumSet) {
            int l = s.length();
            if (l > max) {
                max = l;
            }
        }
        return max;
    }

    public String getCallbackClassName() {
        return mCallbackClassName;
    }

    public String getName() {
        return mName;
    }

    public boolean hasFlag(AttributeFlag flag) {
        if (mFlags == null) {
            return false;
        }
        boolean result = mFlags.contains(flag);
        return result;
    }

    public int getId() {
        return mId;
    }

    Set<String> getEnumSet() {
        return mEnumSet;
    }

    String getParentOid() {
        return mParentOid;
    }

    int getGroupId() {
        return mGroupId;
    }

    public AttributeType getType() {
        return mType;
    }

    AttributeOrder getOrder() {
        return mOrder;
    }

    public String getDescription() {
        if (AttributeType.TYPE_DURATION == getType())
            return mDescription + ".  " + DURATION_PATTERN_DOC;
        else
            return mDescription;
    }

    public long getMax() {
        return mMax;
    }

    public long getMin() {
        return mMin;
    }

    boolean requiredInClass(AttributeClass cls) {
        return mRequiredInClasses != null && mRequiredInClasses.contains(cls);
    }

    boolean optionalInClass(AttributeClass cls) {
        return mOptionalInClasses != null && mOptionalInClasses.contains(cls);
    }

    public Set<AttributeClass> getRequiredIn() {
        return mRequiredInClasses;
    }

    public Set<AttributeClass> getOptionalIn() {
        return mOptionalInClasses;
    }

    public AttributeCardinality getCardinality() {
        return mCardinality;
    }

    public List<String> getGlobalConfigValues() {
        return mGlobalConfigValues;
    }

    public List<String> getGlobalConfigValuesUpgrade() {
        return mGlobalConfigValuesUpgrade;
    }

    public List<String> getDefaultCosValues() {
        return mDefaultCOSValues;
    }

    public List<String> getDefaultExternalCosValues() {
        return mDefaultExternalCOSValues;
    }

    public List<String> getDefaultCosValuesUpgrade() {
        return mDefaultCOSValuesUpgrade;
    }

    public boolean isImmutable() {
        return mImmutable;
    }

    public String getValue() {
        return mValue;
    }

    public List<AttributeServerType> getRequiresRestart() {
        return mRequiresRestart;
    }

    public List<AttributeVersion> getSince() {
        return mSince;
    }

    public AttributeVersion getDeprecatedSince() {
        return mDeprecatedSince;
    }

    public boolean isDeprecated() {
        return getDeprecatedSince() != null;
    }

    /**
     * only for string types
     */
    public boolean isCaseInsensitive() {
        return AttributeType.TYPE_STRING == mType || AttributeType.TYPE_ASTRING == mType;
    }

    public Boolean isEphemeral() {
        return hasFlag(AttributeFlag.ephemeral);
    }

    public Boolean isDynamic() {
        return hasFlag(AttributeFlag.dynamic);
    }

    public Boolean isExpirable() {
        return hasFlag(AttributeFlag.expirable);
    }
}
