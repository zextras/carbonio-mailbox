package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.TargetType;

class RightArgs {
  String mTargetType;
  String mTargetIdOrName;
  String mGranteeType;
  String mGranteeIdOrName;
  String mSecret;
  String mRight;
  RightModifier mRightModifier;

  String[] mArgs;
  int mCurPos = 1;

  RightArgs(String[] args) {
    mArgs = args;
    mCurPos = 1;
  }

  static void getRightArgsTarget(RightArgs ra) throws ServiceException, ArgException {
    if (ra.mCurPos >= ra.mArgs.length) {
      throw new ArgException("not enough arguments");
    }
    ra.mTargetType = ra.mArgs[ra.mCurPos++];
    TargetType tt = TargetType.fromCode(ra.mTargetType);
    if (tt.needsTargetIdentity()) {
      if (ra.mCurPos >= ra.mArgs.length) {
        throw new ArgException("not enough arguments");
      }
      ra.mTargetIdOrName = ra.mArgs[ra.mCurPos++];
    } else {
      ra.mTargetIdOrName = null;
    }
  }

  static void getRightArgsGrantee(RightArgs ra, boolean needGranteeType, boolean needSecret)
          throws ServiceException, ArgException {
    if (ra.mCurPos >= ra.mArgs.length) {
      throw new ArgException("not enough arguments");
    }
    GranteeType gt = null;
    if (needGranteeType) {
      ra.mGranteeType = ra.mArgs[ra.mCurPos++];
      gt = GranteeType.fromCode(ra.mGranteeType);
    } else {
      ra.mGranteeType = null;
    }
    if (gt == GranteeType.GT_AUTHUSER || gt == GranteeType.GT_PUBLIC) {
      return;
    }
    if (ra.mCurPos >= ra.mArgs.length) {
      throw new ArgException("not enough arguments");
    }
    ra.mGranteeIdOrName = ra.mArgs[ra.mCurPos++];

    if (needSecret && gt != null) {
      if (gt.allowSecret()) {
        if (ra.mCurPos >= ra.mArgs.length) {
          throw new ArgException("not enough arguments");
        }
        ra.mSecret = ra.mArgs[ra.mCurPos++];
      }
    }
  }

  static void getRightArgsRight(RightArgs ra) throws ServiceException, ArgException {
    if (ra.mCurPos >= ra.mArgs.length) {
      throw new ArgException("not enough arguments");
    }

    ra.mRight = ra.mArgs[ra.mCurPos++];
    ra.mRightModifier = RightModifier.fromChar(ra.mRight.charAt(0));
    if (ra.mRightModifier != null) {
      ra.mRight = ra.mRight.substring(1);
    }
  }

  static void getRightArgs(RightArgs ra, boolean needGranteeType, boolean needSecret)
          throws ServiceException, ArgException {
    getRightArgsTarget(ra);
    getRightArgsGrantee(ra, needGranteeType, needSecret);
    getRightArgsRight(ra);
  }

  String getNextArg() throws ServiceException {
    if (hasNext()) {
      return mArgs[mCurPos++];
    } else {
      throw ServiceException.INVALID_REQUEST("not enough arguments", null);
    }
  }

  boolean hasNext() {
    return (mCurPos < mArgs.length);
  }
}
