// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.io;

public interface FileCopierCallback {

    /**
     * Callback that is called when a file operation begins.
     * 
     * If this method returns true, the file operation is allowed to run and
     * the fileCopierCallbackEnd() callback is guaranteed to be called later.
     * 
     * If this method returns false, the file operation is rejected and the
     * fileCopierCallbackEnd() callback is guaranteed not to be called.
     * 
     * (These guarantees are actually made by FileCopier implementations.)
     * 
     * @param cbarg
     * @return true if operation may proceed; false if operation should not
     *         proceed, usually due to an error in earlier operation
     */
    public boolean fileCopierCallbackBegin(Object cbarg);

    /**
     * Callback that is called when a file operation completes
     * @param cbarg
     * @param err null if successful; non-null if there was an error
     */
    public void fileCopierCallbackEnd(Object cbarg, Throwable err);
}
