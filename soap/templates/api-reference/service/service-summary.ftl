<#--
SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>

SPDX-License-Identifier: GPL-2.0-only
-->

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

<!-- Generated by ${build.version} on ${build.date} -->
<title>
Carbonio SOAP API Reference ${build.version}
</title>

<link rel="stylesheet" href="../pure-min.css" integrity="sha384-X38yfunGUhNzHpBaEBsWLO+A0HDYOQi8ufWDkZ0k9e0eXz/tH3II7uKZ9msv++Ls" crossorigin="anonymous">

<meta name="viewport" content="width=device-width, initial-scale=1">

<LINK REL ="stylesheet" TYPE="text/css" HREF="../stylesheet.css" TITLE="Style">

<script type="text/javascript">
function windowTitle()
{
    parent.document.title="Service ${service.name} (Carbonio SOAP API Reference ${build.version})";
}
</script>

</head>

<BODY onload="windowTitle();">

<table class="pure-table no-border-table overview-nav-top" cellspacing="3" cellpadding="0" border="0" summary=""  >
  <tbody>
    <tr valign="top" align="center">
    <td   class="NavBarCell1"> <a href="../overview-summary.html"><b>Overview</b></a></td>
    <td   class="NavBarCell1Rev"><b>Service</b></td>
    <td   class="NavBarCell1">Command</td>
    </tr>
  </tbody>
</table>

<div style="margin-top: 62px;">
  <H2>Service ${service.name}</H2>
</div>


<TABLE class="pure-table pure-table-bordered pure-table-striped" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
<TR CLASS="TableHeadingColor" >
  <td nowrap><b>Commands</b></td>
  <td nowrap><b>Description</b></td>
</TR>

<#list service.commands as command>
<TR   CLASS="TableRowColor">
<TD WIDTH="20%"><B><A HREF="./${command.name}.html">${command.name}</A></B></TD>
<TD>${command.shortDescription}</TD>
</TR>
</#list>
</TABLE>


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
