// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntrySearchFilter {

    public enum AndOr {
        and,
        or
    }

    public enum Operator {
        eq,         // exact match (=val; string or integer)
        has,        // substring (=*val*; string only)
        startswith, // starts with (=val*; string only)
        endswith,   // ends with (=*val; string only)
        ge,         // greater than or equal to (>=val; integer only)
        le,         // less then or equal to (<=val; integer only)
        gt,         // greater than (>val; integer only)
        lt          // less than (<val; integer only)
    }

    public interface Visitor {
        void visitSingle(Single term);
        void enterMulti(Multi term);
        void leaveMulti(Multi term);
    }

    public abstract static class Term {
        private boolean mNegation;
        public boolean isNegation() { return mNegation; }
        public void setNegation(boolean negation) { mNegation = negation; }
        protected abstract void visit(Visitor visitor);
    }

    public static class Multi extends Term {
        private AndOr mAndOr;
        private List<Term> mTerms;

        public Multi(boolean negation, AndOr andOr) {
            setNegation(negation);
            mAndOr = andOr;
            mTerms = new ArrayList<>();
        }

			public List<Term> getTerms() { return mTerms; }
        public boolean isAnd() { return AndOr.and.equals(mAndOr); }
        public void add(Term t) { mTerms.add(t); }

        protected void visit(Visitor visitor) {
            visitor.enterMulti(this);
            for (Term t : mTerms) {
                t.visit(visitor);
            }
            visitor.leaveMulti(this);
        }
    }

    public static class Single extends Term {
        private String mLhs;
        private Operator mOp;
        private String mRhs;

        public Single(boolean negation, String lhs, String op, String rhs)
        throws IllegalArgumentException {
            // throws IllegalArgumentException if comp is unknown
            this(negation, lhs, Operator.valueOf(op), rhs);
        }

        public Single(boolean negation, String lhs, Operator op, String rhs) {
            setNegation(negation);
            mLhs = lhs; mOp = op; mRhs = rhs;
        }

        public String getLhs() { return mLhs; }
        public Operator getOperator()  { return mOp; }
        public String getRhs() { return mRhs; }

        protected void visit(Visitor visitor) {
            visitor.visitSingle(this);
        }
    }

    private static Set<String> sIndexedAttrs = new HashSet<>();
    static {
        // list all indexed attributes
        sIndexedAttrs.add(Provisioning.A_objectClass.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraForeignPrincipal.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraId.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraMailCatchAllAddress.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraMailDeliveryAddress.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraMailForwardingAddress.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraMailAlias.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraDomainName.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_uid.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_mail.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_cn.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_sn.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_gn.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_displayName.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraCalResSite.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraCalResBuilding.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraCalResFloor.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraCalResRoom.toLowerCase());
        sIndexedAttrs.add(Provisioning.A_zimbraCalResCapacity.toLowerCase());
        //sIndexedAttrs.add(Provisioning.A_street.toLowerCase());
        //sIndexedAttrs.add(Provisioning.A_l.toLowerCase());
        //sIndexedAttrs.add(Provisioning.A_st.toLowerCase());
        //sIndexedAttrs.add(Provisioning.A_postalCode.toLowerCase());
        //sIndexedAttrs.add(Provisioning.A_co.toLowerCase());
    }

    private Term mTerm;

    public EntrySearchFilter(Term term) {
        mTerm = term;
    }

	public void traverse(Visitor visitor) {
        mTerm.visit(visitor);
    }

}
