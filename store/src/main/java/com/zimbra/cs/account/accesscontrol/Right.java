// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.Arrays;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.accesscontrol.generated.RightConsts;

public abstract class Right extends RightConsts implements Comparable<Right> {

    public enum RightType {
        preset,
        getAttrs,
        setAttrs,
        combo;

        public static RightType fromString(String s) throws ServiceException {
            try {
                return RightType.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR("unknown right type: " + s, e);
            }
        }

        public boolean isUserDefinable() {
            return this != preset;
        }
    }

    private static final int NOT_CACHEABLE = -1;
    private static int sMaxCacheIndex = 0;

    private final String mName;
    protected RightType mRightType;
    private String mDesc;  // a brief description
    private Help mHelp;
    private UI mUI;
    private Boolean mDefault;
    protected TargetType mTargetType;
    protected TargetType mGrantTargetType;
    private CheckRightFallback mFallback;
    int mCacheIndex = NOT_CACHEABLE;

    static void init(RightManager rm) throws ServiceException {
        UserRight.init(rm);
        AdminRight.init(rm);
    }

    Right(String name, RightType rightType) {
        mRightType = rightType;
        mName = name;
    }

    String dump(StringBuilder sb) {
        if (sb == null)
            sb = new StringBuilder();

        sb.append("name         = " + mName + "\n");
        sb.append("type         = " + mRightType.name() + "\n");
        sb.append("desc         = " + mDesc + "\n");
        sb.append("help         = " + (mHelp==null ? "null" : mHelp.getName()  ) + "\n");
        sb.append("ui           = " + (mUI==null ? "null" : mUI.getDesc()  ) + "\n");
        sb.append("default      = " + mDefault + "\n");
        sb.append("target Type  = " + mTargetType + "\n");

        return sb.toString();
    }

    /*
     * for sorting for RightManager.genAdminDocs()
     */
    @Override
    public int compareTo(Right other) {
        return mName.compareTo(other.mName);
    }


    /**
     * returns if this right overlaps the other right
     * @param other
     * @return
     */
    abstract boolean overlaps(Right other) throws ServiceException;

    public boolean isUserRight() {
        return false;
    }

    public boolean isPresetRight() {
        return false;
    }

    public boolean isAttrRight() {
        return false;
    }

    public boolean isComboRight() {
        return false;
    }

    public RightType getRightType() {
        return mRightType;
    }

    public RightClass getRightClass() {
        return (isUserRight() ? RightClass.USER : RightClass.ADMIN);
    }

    /**
     * - right name stored in zimbraACE.
     * - right name appear in XML
     * - right name displayed by CLI
     *
     * @return
     */
    public String getName() {
        return mName;
    }

    public String getDesc() {
        return mDesc;
    }

    void setDesc(String desc) {
        mDesc = desc;
    }

    public Help getHelp() {
        return mHelp;
    }

    public UI getUI() {
        return mUI;
    }

    void setHelp(Help help) {
        mHelp = help;
    }

    void setUI(UI ui) {
        mUI = ui;
    }

    public Boolean getDefault() {
        return mDefault;
    }

    void setDefault(Boolean defaultValue) {
        mDefault = defaultValue;
    }

    public CheckRightFallback getFallback() {
        return mFallback;
    }

    void setFallback(CheckRightFallback fallback) {
        mFallback = fallback;
    }

    boolean executableOnTargetType(TargetType targetType) {
        return (mTargetType == targetType);
    }

    boolean isValidTargetForCustomDynamicGroup() {
        return (mTargetType == TargetType.group);
    }

    abstract boolean grantableOnTargetType(TargetType targetType);

    abstract Set<TargetType> getGrantableTargetTypes();

    /**
     * returns if the subDomain modifier can be specified for the right
     * @return
     */
    boolean allowSubDomainModifier() {
        return executableOnTargetType(TargetType.domain);
    }

    /**
     * returns if the disinheritSubGroups modifier can be specified for the right
     * @return
     */
    boolean allowDisinheritSubGroupsModifier() {
        return executableOnTargetType(TargetType.dl) ||
               executableOnTargetType(TargetType.account) ||
               executableOnTargetType(TargetType.calresource);
    }

    /*
     * overriden only in InlineAttrRight
     */
    boolean isTheSameRight(Right other) {
        return this == other;
    }

    /*
     * for reporting granting error
     */
    final String reportGrantableTargetTypes() {
        Set<TargetType> targetTypes = getGrantableTargetTypes();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (TargetType tt : targetTypes) {
            if (!first) {
                sb.append(" or ");
            } else {
                first = false;
            }
            sb.append(tt.getCode());
        }
        return sb.toString();
    }

    void setTargetType(TargetType targetType) throws ServiceException {
        if (mTargetType != null) {
            throw ServiceException.PARSE_ERROR("target type already set", null);
        };

        mTargetType = targetType;
    }

    void setGrantTargetType(TargetType targetType) throws ServiceException {
        if (!isUserRight()) {
            throw ServiceException.PARSE_ERROR("grant target type is only supported on user rights", null);
        }

        if (!grantableOnTargetType(targetType)) {
            throw ServiceException.PARSE_ERROR(
                    String.format("invalid grant target type: %s, valid grant target type for right %s are: %s",
                    targetType.getCode(), getName(),
                    Arrays.deepToString(getGrantableTargetTypes().toArray())),
                    null);
        }

        mGrantTargetType = targetType;
    }

    void verifyTargetType() throws ServiceException {
        if (mTargetType == null) {
            throw ServiceException.PARSE_ERROR("missing target type", null);
        }
    }

    public TargetType getTargetType() throws ServiceException {
        return mTargetType;
    }

    public TargetType getGrantTargetType() {
        return mGrantTargetType;
    }

    // for SOAP response only
    public String getTargetTypeStr() {
        return mTargetType.getCode();
    }

    public String getGrantTargetTypeStr() {
        if (mGrantTargetType == null) {
            return null;
        } else {
            return mGrantTargetType.getCode();
        }
    }

    /*
     * - verify that all things are well with this object, catch loose ends
     *   that were not catched during paring
     *
     * - populate internal aux data structures
     *
     * - after this method is called for an right object, no change should be done
     *   to the object.
     */
    void completeRight() throws ServiceException {
        if (getDesc() == null)
            throw ServiceException.PARSE_ERROR("missing description", null);
        verifyTargetType();
    }

    void setCacheable() {
        mCacheIndex = getNextCacheIndex();
    }

    boolean isCacheable() {
        return mCacheIndex != NOT_CACHEABLE;
    }

    public int getCacheIndex() {
        return mCacheIndex;
    }

    private static synchronized int getNextCacheIndex() {
        sMaxCacheIndex++;
        return sMaxCacheIndex - 1;
    }

    public static int getMaxCacheIndex() {
        return sMaxCacheIndex;
    }

    public static void main(String[] args) throws ServiceException {
        // init rights
        RightManager.getInstance();

        Right r1 = Rights.Admin.R_domainAdminAccountRights;
        Right r2 = Rights.Admin.R_domainAdminRights;
        boolean overlaps = r1.overlaps(r2);
        System.out.println(r1.getName() +  " " + r2.getName() + " => " + overlaps);

        r1 = Rights.Admin.R_modifyCos;
        overlaps = r1.overlaps(r2);
        System.out.println(r1.getName() +  " " + r2.getName() + " => " + overlaps);
    }
}
