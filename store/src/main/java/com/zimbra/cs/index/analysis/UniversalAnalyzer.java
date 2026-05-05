// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import com.google.common.base.CharMatcher;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;

/**
 * Hybrid {@link Analyzer} of {@code StandardAnalyzer} and {@code CJKAnalyzer}.
 * <p>
 * This {@link Analyzer} may not be perfect compared to ones that are optimized
 * for a specific language, which requires to switch analyzers depending on the
 * language, but does a decent job for most languages and even mixed text just
 * by this single analyzer. The implementation is based on {@code StandardAnalyzer},
 * and applies bigram tokenization to CJK unicode blocks.
 *
 * @author ysasaki
 */
public final class UniversalAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        UniversalTokenizer tokenizer = new UniversalTokenizer();
        TokenStream result = new UniversalTokenFilter(tokenizer);
        CharArraySet stopWords = CharArraySet.copy(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        try {
            Set<?> ldapStopWords = Provisioning.getInstance().getConfig().getMultiAttrSet(Provisioning.A_zimbraDefaultAnalyzerStopWords);
            if (ldapStopWords != null && !ldapStopWords.isEmpty()) {
                stopWords = new CharArraySet(ldapStopWords, true);
            }
        } catch (ServiceException e) {
        	ZimbraLog.index.error("Failed to retrieve stop words from LDAP", e);
        }
        // In Lucene 9.x, position increments are always enabled
        StopFilter stopFilter = new StopFilter(result, stopWords);
        return new TokenStreamComponents(tokenizer, stopFilter);
    }

    private static class UniversalTokenFilter extends TokenFilter {
        private CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
        private TypeAttribute typeAttr = addAttribute(TypeAttribute.class);

        UniversalTokenFilter(TokenStream in) {
            super(in);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (!input.incrementToken()) {
                return false;
            }

            String type = typeAttr.type();
            if (type == UniversalTokenizer.TokenType.APOSTROPHE.name()) {
                // endsWith "'s"
                int len = termAttr.length();
                if (len >= 2 && termAttr.charAt(len - 1) == 's' && termAttr.charAt(len - 2) == '\'') {
                    // remove 's from possessions
                    termAttr.setLength(len - 2);
                }
            } else if (type == UniversalTokenizer.TokenType.ACRONYM.name()) {
                // remove dots from acronyms
                String replace = CharMatcher.is('.').removeFrom(termAttr);
                termAttr.setEmpty().append(replace);
            }

            return true;
        }
    }

}
