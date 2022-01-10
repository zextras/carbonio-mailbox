/* -*- Mode: c; c-basic-offset: 4 -*- */

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

#ifndef _ZIMBRA_NATIVE_UTIL_H
#define _ZIMBRA_NATIVE_UTIL_H

#ifdef __cplusplus
extern "C" {
#endif

void
ZimbraThrowNPE(JNIEnv *env, const char *msg);

void
ZimbraThrowIAE(JNIEnv *env, const char *msg);

void
ZimbraThrowIOE(JNIEnv *env, const char *msg);

void
ZimbraThrowFNFE(JNIEnv *env, const char *msg);

void
ZimbraThrowOFE(JNIEnv *env, const char *msg);

#ifdef __cplusplus
}
#endif
#endif
