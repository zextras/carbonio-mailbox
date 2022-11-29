<#--
SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>

SPDX-License-Identifier: AGPL-3.0-only
-->

<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" class="js">

<head>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type">
<link href="images/favicon.ico" rel="shortcut icon">
<title>Zimbra SOAP API : Change Log</title>
</head>

<body>

<div class="super-nav">
    <div class="container">
        <ul>
            <li><a href="https://www.zextras.com">zextras.com</a></li>
            <li><a href="https://community.zextras.com">forums</a></li>
            <li class="last"><a href="https://community.zextras.com/zextras-carbonio/">blog</a></li>
        </ul>
    </div>
</div>

<div class="wrap">

    <div class="container header">
        <img src="images/logo.png" width="168" height="40" border="0" alt="Zimbra" title="Zimbra">
        <h2>ZCS ${comparison.buildVersion} : Zimbra SOAP API : Change Log</h2>
    </div>

    <div class="content-wrap">
    <p>
    <b>Current:</b> ${comparison.buildVersion} (${comparison.buildDate})
    <br />
    <b>Baseline:</b> ${baseline.buildVersion} (${baseline.buildDate})
    </p>
        <h3>Added Commands</h3>
        <div style="padding-left:25px">
        <ul>
            <#if addedCommands?size == 0>
                <li>None</li>
            </#if>
            <#list addedCommands as command>
                <li><a href="../api-reference/${command.docApiLinkFragment}">${command.name} (${command.namespace})</a></li>
            </#list>
        </ul>
        </div>
        <h3>Removed Commands</h3>
        <div style="padding-left:25px">
        <ul>
            <#if removedCommands?size == 0>
                <li>None</li>
            </#if>
            <#list removedCommands as command>
                <li><a href="../../${baseline.buildVersion}/api-reference/${command.docApiLinkFragment}">${command.name} (${command.namespace})</a></li>
            </#list>
        </ul>
        </div>
        <h3>Modified Commands</h3>
        <div style="padding-left:25px">
            <#if modifiedCommands?size == 0>
                <ul>
                    <li>None</li>
                </ul>
            </#if>
            <#list modifiedCommands as modifiedCommand>
                <ul>
                    <li>
                        <h4><a href="../api-reference/${modifiedCommand.docApiLinkFragment}">${modifiedCommand.name} (${modifiedCommand.namespace})</a></h4>
                        <div style="padding-left:25px">
                        <ul>
                            <#if modifiedCommand.newAttrs?size != 0>
                            <li><b>ADDED ATTRIBUTES</b>
                                <ul style="padding-left:15px">
                                <#list modifiedCommand.newAttrs as apiAttrib>
                                    <li><i>${apiAttrib.xpath}</i></li>
                                </#list>
                                </ul>
                            </li>
                            </#if>
                            <#if modifiedCommand.deletedAttrs?size != 0>
                            <li><b>REMOVED ATTRIBUTES</b>
                                <ul style="padding-left:15px">
                                <#list modifiedCommand.deletedAttrs as apiAttrib>
                                    <li><i>${apiAttrib.xpath}</i></li>
                                </#list>
                                </ul>
                            </li>
                            </#if>
                            <#if modifiedCommand.modifiedAttrs?size != 0>
                            <li><b>MODIFIED ATTRIBUTES</b>
                                <ul style="padding-left:15px">
                                <#list modifiedCommand.modifiedAttrs as apiAttrib>
                                    <li><i>${apiAttrib.xpath}</i>
                                    <br />
                                    <table>
                                    <tr>
                                    <td> <b>Comparison value:</b> </td><td> ${apiAttrib.currentRepresentation} </td>
                                    </tr>
                                    <tr>
                                    <td> <b>Baseline value:</b> </td><td> ${apiAttrib.baselineRepresentation} </td>
                                    </tr>
                                    </table>
                                </#list>
                                </ul>
                            </li>
                            </#if>
                            <#if modifiedCommand.newElems?size != 0>
                            <li><b>ADDED ELEMENTS</b>
                                <ul style="padding-left:15px">
                                <#list modifiedCommand.newElems as apiElem>
                                    <li><i>${apiElem.xpath}</i></li>
                                </#list>
                                </ul>
                            </li>
                            </#if>
                            <#if modifiedCommand.deletedElems?size != 0>
                            <li><b>REMOVED ELEMENTS</b>
                                <ul style="padding-left:15px">
                                <#list modifiedCommand.deletedElems as apiElem>
                                    <li><i>${apiElem.xpath}</i></li>
                                </#list>
                                </ul>
                            </li>
                            </#if>
                            <#if modifiedCommand.modifiedElements?size != 0>
                            <li><b>MODIFIED ELEMENT VALUES</b>
                                <ul style="padding-left:15px">
                                <#list modifiedCommand.modifiedElements as apiElem>
                                    <li><i>${apiElem.xpath}</i>
                                    <br />
                                    <table>
                                    <tr>
                                    <td> <b>Comparison value:</b> </td><td> ${apiElem.currentRepresentation} </td>
                                    </tr>
                                    <tr>
                                    <td> <b>Baseline value:</b> </td><td> ${apiElem.baselineRepresentation} </td>
                                    </tr>
                                    </table>
                                </#list>
                                </ul>
                            </li>
                            </#if>
                        </ul>
                    </li>
                </ul>
            </#list>
        </div>
    </div>
</div>

<div class="container footer">
    <div class="span-12">
        <p>Copyright &copy; 2012 Zimbra, Inc. All rights reserved</p>
    </div>
    <div class="span-12 last right">
        <p><a href="https://community.zextras.com/">Forums</a> | <a href="https://www.zextras.com/about/">About</a> | <a href="https://www.zextras.com/legal.html#copyright">Copyright</a> | <a href="https://www.zextras.com/privacy.html">Privacy</a> | <a href="https://www.zextras.com/licenses/">License</a> | <a href="https://www.zextras.com/legal.html">Trademarks</a></p>
    </div>
</div>
</body
></html>
