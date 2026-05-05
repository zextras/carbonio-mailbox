// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

public class HalfwidthKanaVoicedMappingFilter extends MappingCharFilter {
    private static final NormalizeCharMap normMap;
    static {
        NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        builder.add("\uff76\uff9e","\u30ac");
        builder.add("\uff77\uff9e","\u30ae");
        builder.add("\uff78\uff9e","\u30b0");
        builder.add("\uff79\uff9e","\u30b2");
        builder.add("\uff7a\uff9e","\u30b4");
        builder.add("\uff7b\uff9e","\u30b6");
        builder.add("\uff7c\uff9e","\u30b8");
        builder.add("\uff7d\uff9e","\u30ba");
        builder.add("\uff7e\uff9e","\u30bc");
        builder.add("\uff7f\uff9e","\u30be");
        builder.add("\uff80\uff9e","\u30c0");
        builder.add("\uff81\uff9e","\u30c2");
        builder.add("\uff82\uff9e","\u30c5");
        builder.add("\uff83\uff9e","\u30c7");
        builder.add("\uff84\uff9e","\u30c9");
        builder.add("\uff8a\uff9f","\u30d1");
        builder.add("\uff8b\uff9f","\u30d4");
        builder.add("\uff8c\uff9f","\u30d7");
        builder.add("\uff8d\uff9f","\u30da");
        builder.add("\uff8e\uff9f","\u30dd");
        builder.add("\uff8a\uff9e","\u30d0");
        builder.add("\uff8b\uff9e","\u30d3");
        builder.add("\uff8c\uff9e","\u30d6");
        builder.add("\uff8d\uff9e","\u30d9");
        builder.add("\uff8e\uff9e","\u30dc");
        builder.add("\uff73\uff9e","\u30f4");
        builder.add("\uff9c\uff9e","\u30f7");
        builder.add("\uff66\uff9e","\u30fa");
        normMap = builder.build();
    }

    public HalfwidthKanaVoicedMappingFilter(Reader in) {
        super(normMap, in);
    }
}
