// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.jetty.ee9.nested.Authentication;
import org.eclipse.jetty.ee9.nested.ServletConstraint;
import org.eclipse.jetty.ee9.security.ServerAuthException;
import org.eclipse.jetty.http.pathmap.PathMappings;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;



/**
 * @author zimbra
 *
 */
public class EwsAuthenticator extends ZimbraAuthenticator {

    protected String urlPattern = "";

    private static boolean matchesPath(String pattern, String uri) {
        PathMappings<Boolean> mappings = new PathMappings<>();
        mappings.put(new ServletPathSpec(pattern), Boolean.TRUE);
        return mappings.getMatched(uri) != null;
    }

    /**
     * @return the urlPattern
     */
    @Override
    public String getUrlPattern() {
        return urlPattern;
    }

    /**
     * @param urlPattern the urlPattern to set
     */
    @Override
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern == null ? "/service/extension/zimbraews/*" : urlPattern.replace("//","/");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jetty.security.Authenticator#getAuthMethod()
     */
    @Override
    public String getAuthMethod() {
        return ServletConstraint.NONE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jetty.security.Authenticator#validateRequest(javax.servlet.ServletRequest, javax.servlet.ServletResponse, boolean)
     */
    @Override
    public Authentication validateRequest(ServletRequest request, ServletResponse response,
        boolean mandatory) throws ServerAuthException {


        HttpServletRequest httpReq = (HttpServletRequest) request;
        if (matchesPath(urlPattern, httpReq.getRequestURI())) {
            //We want the Authentication to be set to Unauthenticated so that Spengo Service is not
            // invoked for EWS, returning null will set it to UnAuthenticated.
            return null;
        } else {
            return super.validateRequest(request, response, mandatory);
        }

    }

}
