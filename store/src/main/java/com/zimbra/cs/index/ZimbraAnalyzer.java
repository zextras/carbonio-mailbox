// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.annotations.VisibleForTesting;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.google.common.base.Strings;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.index.analysis.AddrCharTokenizer;
import com.zimbra.cs.index.analysis.ContactTokenFilter;
import com.zimbra.cs.index.analysis.FilenameTokenizer;
import com.zimbra.cs.index.analysis.HalfwidthKanaVoicedMappingFilter;
import com.zimbra.cs.index.analysis.NormalizeTokenFilter;
import com.zimbra.cs.index.analysis.NumberTokenizer;
import com.zimbra.cs.index.analysis.UniversalAnalyzer;
import com.zimbra.cs.index.LuceneIndex;

/***
 * Global analyzer wrapper for Zimbra Indexer.
 * <p>
 * You DO NOT need to instantiate multiple copies of this class -- just call {@link #getInstance()} whenever you need
 * an instance of this class.
 *
 * @since Apr 26, 2004
 * @author tim
 * @author ysasaki
 */
public final class ZimbraAnalyzer extends Analyzer {
    private static ZimbraAnalyzer SINGLETON = new ZimbraAnalyzer();
    private static final Map<String, Analyzer> ANALYZERS = new ConcurrentHashMap<>();
    static {
        ANALYZERS.put("StandardAnalyzer", new ForwardingAnalyzer(new StandardAnalyzer()));
    }

    private final Analyzer defaultAnalyzer = new UniversalAnalyzer();

    public ZimbraAnalyzer() {
    }

    @VisibleForTesting
    public static void setInstance(ZimbraAnalyzer instance) {
        SINGLETON = instance;
    }

    /***
     * Extension analyzers.
     * <p>
     * Extension analyzers must call {@link #registerAnalyzer(String, Analyzer)} on startup.
     */
    public static Analyzer getAnalyzer(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return SINGLETON;
        }
        Analyzer result = ANALYZERS.get(name);
        return result != null ? result : SINGLETON;
    }

    /**
     * We maintain a single global instance for our default analyzer, since it is completely thread safe.
     *
     * @return singleton
     */
    public static Analyzer getInstance() {
        return SINGLETON;
    }

    /**
     * A custom Lucene Analyzer is registered with this API, usually by a Zimbra Extension.
     * <p>
     * Accounts are configured to use a particular analyzer by setting the "zimbraTextAnalyzer" key in the Account or
     * COS setting.
     *
     * The custom analyzer is assumed to be a stateless single instance (although it can and probably should return a
     * new TokenStream instance from it's APIs)
     *
     * @param name a unique name identifying the Analyzer, it is referenced by Account or COS settings in LDAP
     * @param analyzer a Lucene analyzer instance which can be used by accounts that are so configured.
     */
    public static void registerAnalyzer(String name, Analyzer analyzer) throws ServiceException {
        if (ANALYZERS.containsKey(name)) {
            throw ServiceException.FAILURE("Cannot register analyzer: " + name +
                    " because there is one already registered with that name.", null);
        }
        ANALYZERS.put(name, analyzer);
    }

    /**
     * Remove a previously-registered custom Analyzer from the system.
     */
    public static void unregisterAnalyzer(String name) {
        ANALYZERS.remove(name);
    }

    public static String getAllTokensConcatenated(String fieldName, String text) {
        return getAllTokensConcatenated(fieldName, new StringReader(text));
    }

    public static String getAllTokensConcatenated(String fieldName, Reader reader) {
        StringBuilder toReturn = new StringBuilder();

        TokenStream stream = SINGLETON.tokenStream(fieldName, reader);
        CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);

        try {
            stream.reset();
            while (stream.incrementToken()) {
                toReturn.append(term);
                toReturn.append(' ');
            }
            stream.end();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace(); //otherwise eat it
        }

        return toReturn.toString();
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return createComponents(fieldName, defaultAnalyzer);
    }

    private TokenStreamComponents createComponents(String fieldName, Analyzer analyzer) {
        if (fieldName.equals(LuceneFields.L_H_MESSAGE_ID)) {
            KeywordTokenizer tokenizer = new KeywordTokenizer();
            return new TokenStreamComponents(tokenizer, tokenizer);
        } else if (fieldName.equals(LuceneFields.L_ATTACHMENTS) || fieldName.equals(LuceneFields.L_MIMETYPE)) {
            throw new IllegalArgumentException("Use MimeTypeTokenStream");
        } else if (fieldName.equals(LuceneFields.L_SORT_SIZE)) {
            NumberTokenizer tokenizer = new NumberTokenizer();
            return new TokenStreamComponents(tokenizer, tokenizer);
        } else if (fieldName.equals(LuceneFields.L_H_FROM)
                || fieldName.equals(LuceneFields.L_H_TO)
                || fieldName.equals(LuceneFields.L_H_CC)
                || fieldName.equals(LuceneFields.L_H_X_ENV_FROM)
                || fieldName.equals(LuceneFields.L_H_X_ENV_TO)) {
            // This is only for search. We don't need address-aware tokenization
            // because we put all possible forms of address while indexing.
            // Use RFC822AddressTokenStream for indexing.
            // NormalizeTokenFilter is applied as a CharFilter to handle accent/case folding.
            AddrCharTokenizer tokenizer = new AddrCharTokenizer();
            return new TokenStreamComponents(
                reader -> tokenizer.setReader(new NormalizeTokenFilter(reader)),
                tokenizer);
        } else if (fieldName.equals(LuceneFields.L_CONTACT_DATA)) {
            AddrCharTokenizer tokenizer = new AddrCharTokenizer();
            TokenStream result = new ContactTokenFilter(tokenizer); // for bug 48146
            return new TokenStreamComponents(
                reader -> tokenizer.setReader(new NormalizeTokenFilter(reader)),
                result);
        } else if (fieldName.equals(LuceneFields.L_FILENAME)) {
            FilenameTokenizer tokenizer = new FilenameTokenizer();
            return new TokenStreamComponents(
                reader -> tokenizer.setReader(new NormalizeTokenFilter(reader)),
                tokenizer);
        } else {
            // For default analyzer, wrap with HalfwidthKanaVoicedMappingFilter
            // Create a custom tokenizer that applies the CharFilter
            WrappedTokenizer tokenizer = new WrappedTokenizer(analyzer);
            return new TokenStreamComponents(tokenizer, tokenizer);
        }
    }

    /**
     * Wrapper tokenizer that applies HalfwidthKanaVoicedMappingFilter to the input
     * before delegating to the underlying analyzer's token stream.
     * <p>
     * This is used for the default field case where we want to normalize Japanese
     * half-width kana before running the underlying analyzer.
     */
    private static class WrappedTokenizer extends Tokenizer {
        private final Analyzer analyzer;
        private TokenStream delegateStream;
        private CharTermAttribute delegateTermAttr;
        private CharTermAttribute myTermAttr;

        WrappedTokenizer(Analyzer analyzer) {
            this.analyzer = analyzer;
            this.myTermAttr = addAttribute(CharTermAttribute.class);
        }

        @Override
        public void reset() throws IOException {
            super.reset();
            // Apply HalfwidthKanaVoicedMappingFilter to the input reader
            Reader filtered = new HalfwidthKanaVoicedMappingFilter(input);
            // Use tokenStream() which is public API
            delegateStream = analyzer.tokenStream("default", filtered);
            delegateTermAttr = delegateStream.addAttribute(CharTermAttribute.class);
            delegateStream.reset();
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (delegateStream == null) {
                return false;
            }
            clearAttributes();
            if (delegateStream.incrementToken()) {
                myTermAttr.copyBuffer(delegateTermAttr.buffer(), 0, delegateTermAttr.length());
                return true;
            }
            return false;
        }

        @Override
        public void end() throws IOException {
            if (delegateStream != null) {
                delegateStream.end();
            }
            super.end();
        }

        @Override
        public void close() throws IOException {
            if (delegateStream != null) {
                delegateStream.close();
                delegateStream = null;
            }
            super.close();
        }
    }

    public static final TokenStream getTokenStream(String field, Reader reader) {
        return SINGLETON.tokenStream(field, reader);
    }

    private static final class ForwardingAnalyzer extends Analyzer {
        private final Analyzer forwarding;

        ForwardingAnalyzer(Analyzer analyzer) {
            forwarding = analyzer;
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            return SINGLETON.createComponents(fieldName, forwarding);
        }
    }

}
