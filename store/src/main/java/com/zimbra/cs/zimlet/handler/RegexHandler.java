// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet.handler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.zimlet.ZimletConf;
import com.zimbra.cs.zimlet.ZimletConfig;
import com.zimbra.cs.zimlet.ZimletException;
import com.zimbra.cs.zimlet.ZimletHandler;

/**
 * @author schemers
 *
 * Generic object handler that gets its regex from the handler config.
 *
 */
public class RegexHandler implements ZimletHandler {

    private Pattern mPattern;

    @Override
    public String[] match(String text, ZimletConf config) throws ZimletException {
        if (mPattern == null) {
            String handlerConfig = config.getGlobalConf(ZimletConfig.CONFIG_REGEX_VALUE);
            if (handlerConfig == null) {
                throw ZimletException.ZIMLET_HANDLER_ERROR("null regex value");
            }
            mPattern = Pattern.compile(handlerConfig);
            ZimbraLog.zimlet.debug("RegexHandler %s=%s (for config=%s)", ZimletConfig.CONFIG_REGEX_VALUE,
                    handlerConfig, config.getClass().getName());
        }
        Matcher m = mPattern.matcher(text);
        List<String> l = Lists.newArrayList();
        while (m.find()) {
            l.add(text.substring(m.start(), m.end()));
            ZimbraLog.zimlet.trace("RegexHandler matcher found match=[%s] for pattern=[%s]",
                    text.substring(m.start(), m.end()), mPattern.pattern());
        }
        return l.toArray(new String[0]);
    }
}
