<!--
SPDX-FileCopyrightText: 2022 Synacor, Inc.

SPDX-License-Identifier: GPL-2.0-only
-->

<%@ page isErrorPage="true" import="com.zimbra.common.util.L10nUtil,com.zimbra.common.util.L10nUtil.MsgKey" %>
<HTML>
<HEAD>
    <%
        Object redirectUrl = request.getAttribute("spnego.redirect.url");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Object autoRedirect = request.getAttribute("spnego.auto.redirect");
        if (autoRedirect != null && redirectUrl != null && (Boolean) autoRedirect) {
    %>
        <meta http-equiv="refresh" content="0; URL=<%=redirectUrl%>"/>
    <%
        } else {
    %>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <%
        }
    %>
    <title>Spnego Authentication Failed.</title>
    <style type="text/css">
        .unstyled {
            list-style: none;
        }
        .box {
            border: 1px solid #000;
        }
        .box > SPAN:first-child {
            background-size: 2rem 1.7rem;
            float: left;
            width: 100%;
            color: #ff0000;
            font-size: 1.5rem;
        }
        .container {
            text-align: center;
            padding: 1.5rem;
        }
        .header {
            font-weight: bold;
        }
        SPAN > STRONG, EM {
            font-size: 0.9rem;
        }
        .newLine {
            float: left;
            width: 100%;
        }
    </style>
</HEAD>
<BODY>
    <div>
        <div class="box container">
            <%=L10nUtil.getMessage(MsgKey.spnego_401_error_message, request)%>
        </div>
        <div class="container">
            <span class="newLine">
                <a href="<%= redirectUrl %>">
                    <%=L10nUtil.getMessage(MsgKey.spnego_redirect_message, request)%>
                </a>
            </span>
            <span class="newLine"><br /><br /></span>
            <span class="newLine">
                <a href="<%=L10nUtil.getMessage(MsgKey.spnego_browser_setup_wiki, request)%>">
                    <%=L10nUtil.getMessage(MsgKey.spnego_browser_setup_message, request)%>
                </a>
            </span>
        </div>
    </div>
</BODY>
</HTML>
