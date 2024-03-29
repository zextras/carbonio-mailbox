// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap;

import java.util.Map;
import java.util.Set;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning.SearchGalResult;
import com.zimbra.cs.account.gal.GalOp;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;


public class SearchLdapOptions {

    public static final int SIZE_UNLIMITED  = 0;
    public static final String[] RETURN_ALL_ATTRS = null;

    /*
    public static interface SearchLdapVisitor {
        public void visit(String dn, Map<String, Object> attrs, IAttributes ldapAttrs);
    }
    */


    public static class StopIteratingException extends Exception {
    }

    public abstract static class SearchLdapVisitor {
        // whether IAttributes (native data from SDK) should be mapped
        // to Map<String, Object> when the visit method is called.
        //
        // Callsites don't need the Map should set this to false to save the processing.
        private boolean wantAttrMapOnVisit = true;

        protected SearchLdapVisitor() {
        }

        protected SearchLdapVisitor(boolean wantAttrMapOnVisit) {
            this.wantAttrMapOnVisit = wantAttrMapOnVisit;
        }

        public final boolean wantAttrMapOnVisit() {
            return wantAttrMapOnVisit;
        }

        /**
         * invoked for each hit returned from the LDAP server, when wantAttrMapOnVisit
         * is true.
         *
         * @param dn
         * @param attrs
         * @param ldapAttrs
         * @throws StopIteratingException Indicating to the search result iterator to stop
         *                                iterating the remaining LDAP search result and
         *                                calling the visit method.
         */
        public void visit(String dn, Map<String, Object> attrs, IAttributes ldapAttrs)
        throws StopIteratingException {
            assert(false);
            ZimbraLog.ldap.warn("default implementation of SearchLdapVisitor.visit is invoked");
            throw new StopIteratingException();
        }

        /**
         * invoked for each hit returned from the LDAP server, when wantAttrMapOnVisit
         * is false.
         *
         * @param dn
         * @param attrs
         * @param ldapAttrs
         * @throws StopIteratingException Indicating to the search result iterator to stop
         *                                iterating the remaining LDAP search result and
         *                                calling the visit method.
         */
        public void visit(String dn, IAttributes ldapAttrs)
        throws StopIteratingException {
            assert(false);
            ZimbraLog.ldap.warn("default implementation of SearchLdapVisitor.visit is invoked");
            throw new StopIteratingException();
        }
    }


    private static final int DEFAULT_RESULT_PAGE_SIZE = 1000;

    private String searchBase;
    // private String filter;
    private ZLdapFilter filter;
    private String[] returnAttrs = RETURN_ALL_ATTRS;
    private int maxResults = SIZE_UNLIMITED;
    private Set<String> binaryAttrs;
    private int resultPageSize  = DEFAULT_RESULT_PAGE_SIZE;
    private ZSearchScope searchScope;
    private SearchLdapOptions.SearchLdapVisitor visitor;
    private boolean isUseControl = true;
    private boolean isManageDSAit = false;
    private SearchGalResult searchGalResult;
    private GalOp galOp;

    // TODO: retire this
    public SearchLdapOptions(String searchbase, String filterStr,
            String[] returnAttrs, int maxResults, Set<String> binaryAttrs,
            ZSearchScope searchScope, SearchLdapOptions.SearchLdapVisitor visitor)
    throws LdapException {

        this(searchbase,
                ZLdapFilterFactory.getInstance().fromFilterString(FilterId.TODO, filterStr),
                returnAttrs, maxResults, binaryAttrs, searchScope, visitor);
    }

    // TODO: use only this
    public SearchLdapOptions(String searchbase, ZLdapFilter filter,
            String[] returnAttrs, int maxResults, Set<String> binaryAttrs,
            ZSearchScope searchScope, SearchLdapOptions.SearchLdapVisitor visitor) {
        setSearchBase(searchbase);
        setFilter(filter);
        setReturnAttrs(returnAttrs);
        setMaxResults(maxResults);
        setBinaryAttrs(binaryAttrs);
        setSearchScope(searchScope);
        setVisitor(visitor);
    }

    public GalOp getGalOp() {
        return galOp;
    }

    public String getSearchBase() {
        return searchBase;
    }
    public ZLdapFilter getFilter() {
        return filter;
    }

    public String[] getReturnAttrs() {
        return returnAttrs;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public Set<String> getBinaryAttrs() {
        return binaryAttrs;
    }

    public int getResultPageSize() {
        return resultPageSize;
    }

    public ZSearchScope getSearchScope() {
        return searchScope;
    }

    public SearchLdapOptions.SearchLdapVisitor getVisitor() {
        return visitor;
    }

    public SearchGalResult getSearchGalResult() {
        return searchGalResult;
    }

    public void setGalOp(GalOp galOp) {
        this.galOp = galOp;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public void setFilter(ZLdapFilter filter) {
        this.filter = filter;
    }

    public void setReturnAttrs(String[] returnAttrs) {
        this.returnAttrs = returnAttrs;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setBinaryAttrs(Set<String> binaryAttrs) {
        this.binaryAttrs = binaryAttrs;
    }

    public void setResultPageSize(int resultPageSize) {
        this.resultPageSize = resultPageSize;
    }

    public void setSearchScope(ZSearchScope searchScope) {
        this.searchScope = searchScope;
    }

    public void setVisitor(SearchLdapOptions.SearchLdapVisitor visitor) {
        this.visitor = visitor;
    }

    public boolean isUseControl() {
        return isUseControl;
    }

    public void setUseControl(boolean isUseControl) {
        this.isUseControl = isUseControl;
    }

    public boolean isManageDSAit() {
        return isManageDSAit;
    }

    public void setManageDSAit(boolean isManageDSAit) {
        this.isManageDSAit = isManageDSAit;
    }

    public void setSearchGalResult(SearchGalResult result) {
        this.searchGalResult = result;
    }
}
