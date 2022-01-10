// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.concurrent.Callable;

public interface ScheduledTaskCallback<V> {

    void afterTaskRun(Callable<V> task, V lastResult);
}
