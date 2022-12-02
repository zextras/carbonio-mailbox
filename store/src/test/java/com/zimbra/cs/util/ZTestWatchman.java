// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * @author zimbra
 *
 */
public class ZTestWatchman  extends TestWatchman{

    @Override
    public void failed(Throwable e, FrameworkMethod method) {
        System.out.println(method.getName() + " " + e.getClass().getSimpleName() + " " + e.getMessage());
        e.printStackTrace();
    }
    

}
