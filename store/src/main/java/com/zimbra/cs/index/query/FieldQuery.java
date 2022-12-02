// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.util.regex.Pattern;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.NumericUtils;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.index.LuceneQueryOperation;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.index.analysis.FieldTokenStream;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * Structured field query.
 *
 * @see LuceneFields#L_FIELD
 * @author ysasaki
 */
public final class FieldQuery extends TextQuery {

    private static final Pattern NUMERIC_QUERY_REGEX = Pattern.compile("(<|>|<=|>=)?-?\\d+");

    private FieldQuery(String name, String value) {
        super(new FieldTokenStream(name, value), LuceneFields.L_FIELD,
                String.format("%s:%s", name, value == null ? "" : value));
    }

    /**
     * Factory to create a {@link FieldQuery}.
     * <p>
     * If it's a numeric range query, {@link NumericRangeFieldQuery} is created instead.
     *
     * @param mbox mailbox
     * @param name structured field name
     * @param value field value
     * @return new {@link FieldQuery} or {@link NumericRangeFieldQuery}
     * @throws ServiceException error on wildcard expansion
     */
    public static Query create(Mailbox mbox, String name, String value) throws ServiceException {
        if (NUMERIC_QUERY_REGEX.matcher(value).matches()) {
            try {
                return new NumericRangeFieldQuery(name, value);
            } catch (NumberFormatException e) { // fall back to text query
            }
        }
        return new FieldQuery(name, value);
    }

    private static final class NumericRangeFieldQuery extends Query {
        private enum Range {
            EQ(""), GT(">"), GT_EQ(">="), LT("<"), LT_EQ("<=");

            private final String sign;

            private Range(String sign) {
                this.sign = sign;
            }

            @Override
            public String toString() {
                return sign;
            }
        }

        private final String name;
        private final int number;
        private final Range range;

        NumericRangeFieldQuery(String name, String value) throws NumberFormatException {
            this.name = name.toLowerCase();
            if (value.startsWith("<")) {
                if (value.startsWith("=", 1)) {
                    range = Range.LT_EQ;
                    number = Integer.parseInt(value.substring(2));
                } else {
                    range = Range.LT;
                    number = Integer.parseInt(value.substring(1));
                }
            } else if (value.startsWith(">")) {
                if (value.startsWith("=", 1)) {
                    range = Range.GT_EQ;
                    number = Integer.parseInt(value.substring(2));
                } else {
                    range = Range.GT;
                    number = Integer.parseInt(value.substring(1));
                }
            } else {
                range = Range.EQ;
                number = Integer.parseInt(value);
            }
        }

        @Override
        public boolean hasTextOperation() {
            return true;
        }

        @Override
        public QueryOperation compile(Mailbox mbox, boolean bool) {
            org.apache.lucene.search.Query query = null;
            switch (range) {
                case EQ:
                    query = new TermQuery(new Term(LuceneFields.L_FIELD,
                            name + "#:" + NumericUtils.intToPrefixCoded(number)));
                    break;
                case GT:
                    query = new TermRangeQuery(LuceneFields.L_FIELD,
                            name + "#:" + NumericUtils.intToPrefixCoded(number),
                            name + "#:" + NumericUtils.intToPrefixCoded(Integer.MAX_VALUE),
                            false, true);
                    break;
                case GT_EQ:
                    query = new TermRangeQuery(LuceneFields.L_FIELD,
                            name + "#:" + NumericUtils.intToPrefixCoded(number),
                            name + "#:" + NumericUtils.intToPrefixCoded(Integer.MAX_VALUE),
                            true, true);
                    break;
                case LT:
                    query = new TermRangeQuery(LuceneFields.L_FIELD,
                            name + "#:" + NumericUtils.intToPrefixCoded(Integer.MIN_VALUE),
                            name + "#:" + NumericUtils.intToPrefixCoded(number),
                            true, false);
                    break;
                case LT_EQ:
                    query = new TermRangeQuery(LuceneFields.L_FIELD,
                            name + "#:" + NumericUtils.intToPrefixCoded(Integer.MIN_VALUE),
                            name + "#:" + NumericUtils.intToPrefixCoded(number),
                            true, true);
                    break;
                default:
                    assert false;
            }

            LuceneQueryOperation op = new LuceneQueryOperation();
            op.addClause(toQueryString(LuceneFields.L_FIELD, range.toString() + number), query, evalBool(bool));
            return op;
        }

        @Override
        void dump(StringBuilder out) {
            out.append('#');
            out.append(name);
            out.append("#:");
            out.append(range);
            out.append(number);
        }

        @Override
        void sanitizedDump(StringBuilder out) {
            out.append('#');
            out.append(name);
            out.append("#:");
            out.append(range);
            out.append("$NUM");
        }
    }

}
