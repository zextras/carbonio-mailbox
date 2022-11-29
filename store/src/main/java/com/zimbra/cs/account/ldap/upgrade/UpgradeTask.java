// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;

public enum UpgradeTask {

    BUG_11562(BUG_11562.class),
    BUG_14531(BUG_14531.class),
    BUG_18277(BUG_18277.class),
    BUG_22033(BUG_22033.class),
    BUG_27075(BUG_27075.class), // e.g. -b 27075 5.0.12
    BUG_29978(BUG_29978.class),
    // BUG_31284(ZimbraPrefFromDisplay.class),
    BUG_31694(BUG_31694.class),
    BUG_32557(BUG_32557.class),
    BUG_32719(BUG_32719.class),
    BUG_33814(BUG_33814.class),
    BUG_41000(BUG_41000.class),
    BUG_42828(BUG_42828.class),
    BUG_42877(BUG_42877.class),
    BUG_42896(BUG_42896.class),
    BUG_43147(BUG_43147.class),
    BUG_43779(BUG_43779.class),
    BUG_46297(BUG_46297.class),
    BUG_46883(BUG_46883.class),
    BUG_46961(BUG_46961.class),
    BUG_47934(BUG_47934.class),
    BUG_50258(BUG_50258.class),
    BUG_50458(BUG_50458.class),
    BUG_50465(BUG_50465.class),
    BUG_53745(BUG_53745.class),
    BUG_55649(BUG_55649.class),
    BUG_57039(BUG_57039.class),
    BUG_57205(BUG_57205.class),
    BUG_57425(BUG_57425.class),
    BUG_57855(BUG_57855.class),
    BUG_57866(BUG_57866.class),
    BUG_57875(BUG_57875.class),
    BUG_58084(BUG_58084.class),
    BUG_58481(BUG_58481.class),
    BUG_58514(BUG_58514.class),
    BUG_59720(BUG_59720.class),
    BUG_60640(BUG_60640.class),
    BUG_63475(BUG_63475.class),
    BUG_63722(BUG_63722.class),
    BUG_64380(BUG_64380_63587.class),
    BUG_65070(BUG_65070.class),
    BUG_66001(BUG_66001.class),
    BUG_66387(BUG_66387.class),
    BUG_67237(BUG_67237.class),
    BUG_68190(BUG_68190.class),
    BUG_68394(BUG_68394.class),
    BUG_68831(BUG_68831.class),
    BUG_68891(BUG_68891.class),
    BUG_72007(BUG_72007.class),
    BUG_75450(BUG_75450.class),
    BUG_75650(BUG_75650.class),
    BUG_76427(BUG_76427.class),
    BUG_81385(BUG_81385.class),
    BUG_85224(BUG_85224.class),
    BUG_87674(BUG_87674.class),
    BUG_88098(BUG_88098.class),
    BUG_88766(BUG_88766.class);

    private static final String NAME_PREFIX = "BUG_";

    private Class<? extends UpgradeOp> upgradeOpClass;

    private UpgradeTask(Class<? extends UpgradeOp> upgradeOpClass) {
        this.upgradeOpClass = upgradeOpClass;
    }

    UpgradeOp getUpgradeOp() throws ServiceException {
        try {
            return upgradeOpClass.newInstance();
        } catch (IllegalAccessException e) {
            throw ServiceException.FAILURE("IllegalAccessException: " + upgradeOpClass.getCanonicalName(), e);
        } catch (InstantiationException e) {
            throw ServiceException.FAILURE("InstantiationException: " + upgradeOpClass.getCanonicalName(), e);
        }
    }

    static UpgradeTask getTaskByBug(String bugNumber) throws ServiceException {
        String bug = NAME_PREFIX + bugNumber;

        try {
            return UpgradeTask.valueOf(bug);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // from unittest
    public String getBug() {
        return name().substring(NAME_PREFIX.length());
    }
}
