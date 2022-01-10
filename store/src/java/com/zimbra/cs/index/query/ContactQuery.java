// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.index.LuceneQueryOperation;
import com.zimbra.cs.index.NoTermQueryOperation;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.index.analysis.AddrCharTokenizer;
import com.zimbra.cs.index.analysis.ContactTokenFilter;
import com.zimbra.cs.index.analysis.HalfwidthKanaVoicedMappingFilter;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * Special text query to search contacts.
 *
 * @author ysasaki
 */
public final class ContactQuery extends Query {
    private final List<String> tokens = new ArrayList<String>();

    public ContactQuery(String text) {
        TokenStream stream = new ContactTokenFilter(new AddrCharTokenizer(new HalfwidthKanaVoicedMappingFilter(new StringReader(text))));
        CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
        try {
            stream.reset();
            while (stream.incrementToken()) {
                tokens.add(CharMatcher.is('*').trimTrailingFrom(termAttr)); // remove trailing wildcard characters
            }
            stream.end();
            stream.close();
        } catch (IOException e) { // should never happen
            ZimbraLog.search.error("Failed to tokenize text=%s", text);
        }
    }

    @Override
    public boolean hasTextOperation() {
        return true;
    }

    @Override
    public QueryOperation compile(Mailbox mbox, boolean bool) throws ServiceException {
        switch (tokens.size()) {
            case 0:
                return new NoTermQueryOperation();
            case 1: {
                LuceneQueryOperation op = new LuceneQueryOperation();
                PrefixQuery query = new PrefixQuery(new Term(LuceneFields.L_CONTACT_DATA, tokens.get(0)));
                op.addClause("contact:" +  tokens.get(0), query, evalBool(bool));
                return op;
            }
            default: {
                LuceneQueryOperation op = new LuceneQueryOperation();
                LuceneQueryOperation.LazyMultiPhraseQuery query = new LuceneQueryOperation.LazyMultiPhraseQuery();
                for (String token : tokens) {
                    query.expand(new Term(LuceneFields.L_CONTACT_DATA, token)); // expand later
                }
                op.addClause("contact:\"" + Joiner.on(' ').join(tokens) + "\"", query, evalBool(bool));
                return op;
            }
        }
    }

    @Override
    void dump(StringBuilder out) {
        out.append("CONTACT:");
        Joiner.on(',').appendTo(out, tokens);
    }
    
    @Override
    void sanitizedDump(StringBuilder out) {
        out.append("CONTACT:");
        out.append(Strings.repeat("$TEXT,", tokens.size()));
        if (out.charAt(out.length()-1) == ',') {
            out.deleteCharAt(out.length()-1);
        }
    }

}
