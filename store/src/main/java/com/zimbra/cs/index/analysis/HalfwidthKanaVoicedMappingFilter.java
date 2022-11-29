// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.MappingCharFilter;
import org.apache.lucene.analysis.NormalizeCharMap;
import org.apache.lucene.analysis.CharStream;

public class HalfwidthKanaVoicedMappingFilter extends MappingCharFilter {
    private static NormalizeCharMap normMap = new NormalizeCharMap();
    static {
        normMap.add("\uff76\uff9e","\u30ac");
        normMap.add("\uff77\uff9e","\u30ae");
        normMap.add("\uff78\uff9e","\u30b0");
        normMap.add("\uff79\uff9e","\u30b2");
        normMap.add("\uff7a\uff9e","\u30b4");
        normMap.add("\uff7b\uff9e","\u30b6");
        normMap.add("\uff7c\uff9e","\u30b8");
        normMap.add("\uff7d\uff9e","\u30ba");
        normMap.add("\uff7e\uff9e","\u30bc");
        normMap.add("\uff7f\uff9e","\u30be");
        normMap.add("\uff80\uff9e","\u30c0");
        normMap.add("\uff81\uff9e","\u30c2");
        normMap.add("\uff82\uff9e","\u30c5");
        normMap.add("\uff83\uff9e","\u30c7");
        normMap.add("\uff84\uff9e","\u30c9");
        normMap.add("\uff8a\uff9f","\u30d1");
        normMap.add("\uff8b\uff9f","\u30d4");
        normMap.add("\uff8c\uff9f","\u30d7");
        normMap.add("\uff8d\uff9f","\u30da");
        normMap.add("\uff8e\uff9f","\u30dd");
        normMap.add("\uff8a\uff9e","\u30d0");
        normMap.add("\uff8b\uff9e","\u30d3");
        normMap.add("\uff8c\uff9e","\u30d6");
        normMap.add("\uff8d\uff9e","\u30d9");
        normMap.add("\uff8e\uff9e","\u30dc");
        normMap.add("\uff73\uff9e","\u30f4");
        normMap.add("\uff9c\uff9e","\u30f7");
        normMap.add("\uff66\uff9e","\u30fa");
    }

    public HalfwidthKanaVoicedMappingFilter(Reader in) {
        super(normMap, in);
    }

    public HalfwidthKanaVoicedMappingFilter(CharStream in) {
        super(normMap, in);
    }
}
