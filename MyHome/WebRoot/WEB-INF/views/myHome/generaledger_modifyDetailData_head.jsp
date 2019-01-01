<%@page import="hq.mydb.utils.MyDBHelper"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="hq.mydb.data.*"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%
	String path = request.getContextPath();
	String type = (String) request.getAttribute("type");
	String yearMonth = (String) request.getAttribute("yearMonth");
%>
<table valign="middle">
	<tr>
		<td height="10px">&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td width="20px">&nbsp;</td>
		<td><span style='font-weight:bold;color:red;'><%=type%></span><span style='color:#FFF;'> - <%=yearMonth%></span></td>
	</tr>
</table>
