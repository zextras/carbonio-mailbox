<#--
SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
SPDX-License-Identifier: GPL-2.0-only
-->
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" class="js">
   <head>
      <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
      <LINK REL ="stylesheet" TYPE="text/css" HREF="../api-changelog/stylesheet.css" TITLE="Style">
      <link href="images/favicon.ico" rel="shortcut icon">
      <title>Carbonio SOAP API : Change Log</title>
   </head>
   <body>
      <#-- start header-->
      <div class="header">
         <div class="pure-menu pure-menu-horizontal">
            <ul class="pure-menu-list">
               <li class="pure-menu-item">
                  <a href="https://www.zextras.com" target="_blank" class="pure-menu-link">Zextras</a>
               </li>
               <li class="pure-menu-item">
                  <a href="https://community.zextras.com" target="_blank" class="pure-menu-link">Forums</a>
               </li>
               <li class="pure-menu-item">
                  <a href="https://community.zextras.com/zextras-carbonio/" target="_blank" class="pure-menu-link">Blogs</a>
               </li>
               <li class="pure-menu-item pure-menu-has-children pure-menu-allow-hover">
                    <a href="#" id="menuLink1" class="pure-menu-link">Carbonio-CE SOAP API</a>
                    <ul class="pure-menu-children">
                        <li class="pure-menu-item">
                            <a href="../api-reference/index.html" class="pure-menu-link">API Reference</a>
                        </li>
                        <li class="pure-menu-item pure-menu-item pure-menu-selected">
                            <a href="index.html" class="pure-menu-link">API Changelog</a>
                        </li>
                    </ul>
                </li>
            </ul>
         </div>
      </div>
      <#-- end header-->

      <#-- start content-->
      <div class="content">
      <div class="command-intro2">
         <h3 style="margin-top: 0px;">${comparison.buildVersion} : Carbonio SOAP API : Change Log</h3>
         <table class="pure-table pure-table-bordered" cellspacing="0" cellpadding="5" border="1">
            <tbody>
               <tr>
                  <td> <b>Current Version</b> </td>
                  <td> ${comparison.buildVersion} (${comparison.buildDate}) </td>
               </tr>
               <tr>
                  <td> <b>Baseline Version</b> </td>
                  <td> ${baseline.buildVersion} (${baseline.buildDate}) </td>
               </tr>
            </tbody>
         </table>
      </div>
      <div class="content-wrap">
         <div class="code2" style="padding-left:25px">
            <h3>Added Commands</h3>
            <ul>
               <#if addedCommands?size == 0>
               <li>None</li>
               </#if>
               <#list addedCommands as command>
               <li><a href="../api-reference/${command.docApiLinkFragment}">${command.name} (${command.namespace})</a></li>
               </#list>
            </ul>
         </div>
         <div class="code2" style="padding-left:25px">
            <h3>Removed Commands</h3>
            <ul>
               <#if removedCommands?size == 0>
               <li>None</li>
               </#if>
               <#list removedCommands as command>
               <li><a href="../../${baseline.buildVersion}/api-reference/${command.docApiLinkFragment}">${command.name} (${command.namespace})</a></li>
               </#list>
            </ul>
         </div>
         <div class="code2" style="padding-left:25px">
            <h3>Modified Commands</h3>
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
                        <li>
                           <b>ADDED ATTRIBUTES</b>
                           <ul style="padding-left:15px">
                              <#list modifiedCommand.newAttrs as apiAttrib>
                              <li><i>${apiAttrib.xpath}</i></li>
                              </#list>
                           </ul>
                        </li>
                        </#if>
                        <#if modifiedCommand.deletedAttrs?size != 0>
                        <li>
                           <b>REMOVED ATTRIBUTES</b>
                           <ul style="padding-left:15px">
                              <#list modifiedCommand.deletedAttrs as apiAttrib>
                              <li><i>${apiAttrib.xpath}</i></li>
                              </#list>
                           </ul>
                        </li>
                        </#if>
                        <#if modifiedCommand.modifiedAttrs?size != 0>
                        <li>
                           <b>MODIFIED ATTRIBUTES</b>
                           <ul style="padding-left:15px">
                              <#list modifiedCommand.modifiedAttrs as apiAttrib>
                              <li>
                                 <i>${apiAttrib.xpath}</i>
                                 <br />
                                 <table>
                                    <tr>
                                       <td> <b>Comparison value:</b> </td>
                                       <td> ${apiAttrib.currentRepresentation} </td>
                                    </tr>
                                    <tr>
                                       <td> <b>Baseline value:</b> </td>
                                       <td> ${apiAttrib.baselineRepresentation} </td>
                                    </tr>
                                 </table>
                                 </#list>
                           </ul>
                        </li>
                        </#if>
                        <#if modifiedCommand.newElems?size != 0>
                        <li>
                           <b>ADDED ELEMENTS</b>
                           <ul style="padding-left:15px">
                              <#list modifiedCommand.newElems as apiElem>
                              <li><i>${apiElem.xpath}</i></li>
                              </#list>
                           </ul>
                        </li>
                        </#if>
                        <#if modifiedCommand.deletedElems?size != 0>
                        <li>
                           <b>REMOVED ELEMENTS</b>
                           <ul style="padding-left:15px">
                              <#list modifiedCommand.deletedElems as apiElem>
                              <li><i>${apiElem.xpath}</i></li>
                              </#list>
                           </ul>
                        </li>
                        </#if>
                        <#if modifiedCommand.modifiedElements?size != 0>
                        <li>
                           <b>MODIFIED ELEMENT VALUES</b>
                           <ul style="padding-left:15px">
                              <#list modifiedCommand.modifiedElements as apiElem>
                              <li>
                                 <i>${apiElem.xpath}</i>
                                 <br />
                                 <table>
                                    <tr>
                                       <td> <b>Comparison value:</b> </td>
                                       <td> ${apiElem.currentRepresentation} </td>
                                    </tr>
                                    <tr>
                                       <td> <b>Baseline value:</b> </td>
                                       <td> ${apiElem.baselineRepresentation} </td>
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
      <#-- end content-->

      <#-- start footer-->
      <div class="last-element">&nbsp</div>
      <div class="footer">
         &copy Copyright <span id="year"></span>, The Zextras Team. All rights reserved.
      </div>
      <script>
         document.getElementById("year").innerHTML = new Date().getFullYear();
      </script>
      <#-- end footer-->
   </body>
</html>
