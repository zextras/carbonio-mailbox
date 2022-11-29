// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.account.Key;
import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;

public abstract class CheckRight {
    // input to the class
    protected Entry mTarget;
    protected Right mRightNeeded;
    protected boolean mCanDelegateNeeded;
    
    protected Provisioning mProv; 
    protected TargetType mTargetType;
    
    protected CheckRight(Entry target, Right rightNeeded, boolean canDelegateNeeded) 
    throws ServiceException {
        
        mProv = Provisioning.getInstance();

        // This path is called from AccessManager, the target object can be a 
        // DistributionList obtained from prov.get(DistributionListBy).  
        // We require one from getDLBasic(DistributionListBy) here, because when group 
        // members are added/removed, the upward membership cache is cleared on the cached
        // entry.
        if ((target instanceof DistributionList) && !(target instanceof PseudoTarget.PseudoDistributionList)) {
            target = mProv.getDLBasic(Key.DistributionListBy.id, ((DistributionList)target).getId());
        }
        mTarget = target;
        
        mRightNeeded = rightNeeded;
        mCanDelegateNeeded = canDelegateNeeded;
    }
     
    // bug 46840
    // master control to enable/disable group targets
    // TODO: - check all callsites of Right.grantableOnTargetType and 
    //         RightChecker.rightApplicableOnTargetType
    //         see if they can be optimized
    //
    public static boolean allowGroupTarget(Right rightNeeded) throws ServiceException {
        // group target is only supported for admin rights
        boolean allowed = !rightNeeded.isUserRight();
        
        if (rightNeeded.isUserRight()) {
            // for perf reason, for user rights, groups target is supported 
            // only if target type of the right is not account.
            // i.e. account right cannot be granted on groups
            
            if (rightNeeded.getTargetType() == TargetType.account) {
                allowed = false;
            } else {
                allowed = true;
            }
            
        } else {
            // group targets can be turned off for admin rights by a localconfig key
            allowed = !DebugConfig.disableGroupTargetForAdminRight;
        }
        return allowed;
    }

    
    /*
     * check if rightNeeded is applicable on target type 
     */
    static boolean rightApplicableOnTargetType(TargetType targetType, 
            Right rightNeeded, boolean canDelegateNeeded) {
        if (canDelegateNeeded) {
            // check if the right is grantable on the target
            if (!rightNeeded.grantableOnTargetType(targetType)) {
                return false;
            }
        } else {
            // check if the right is executable on the target
            if (!rightNeeded.executableOnTargetType(targetType)) {
                return false;
            }
        }
        return true;
    }
}
