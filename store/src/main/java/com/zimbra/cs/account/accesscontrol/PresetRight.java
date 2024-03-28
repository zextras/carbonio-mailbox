// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.util.HashSet;
import java.util.Set;

import com.zimbra.common.service.ServiceException;

public class PresetRight extends AdminRight {

    PresetRight(String name) {
        super(name, RightType.preset);
    }
    
    @Override
    public boolean isPresetRight() {
        return true;
    }
    
    //
    // TODO: disguise group target as dl for R_checkRightGrp right
    //
    
    @Override
    Set<TargetType> getGrantableTargetTypes() {
        Set<TargetType> targetTypes = new HashSet<>();
      targetTypes.addAll(mTargetType.inheritFrom());
        return targetTypes;
    }
    
    @Override
    boolean grantableOnTargetType(TargetType targetType) {
        return targetType.isInheritedBy(mTargetType);
    }
    
    @Override
    boolean overlaps(Right other) throws ServiceException {
        if (other.isPresetRight())
            return this==other;
        else if (other.isAttrRight())
            return false;
        else if (other.isComboRight())
            return ((ComboRight)other).containsPresetRight(this);
        else
            throw ServiceException.FAILURE("internal error", null);
    }

}
