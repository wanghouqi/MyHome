<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="hq.myhome.utils.Definition"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%
	String path = request.getContextPath();
	String alertMsg = (String) session.getAttribute(Definition.SESSION_ATTR_KEY_ALERT_MSG);
	session.removeAttribute(Definition.SESSION_ATTR_KEY_ALERT_MSG);
%>
<script type="text/javascript">
<%if (StringUtils.isNotEmpty(alertMsg)) {%>
	alert("<%=alertMsg%>");
<%}%>
</script>


