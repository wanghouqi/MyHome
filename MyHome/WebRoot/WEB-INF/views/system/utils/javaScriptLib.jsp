<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="com.alibaba.fastjson.JSONObject"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="hq.myhome.utils.Definition"%>
<%
	String path = request.getContextPath();
	JSONObject userJSONObj = new JSONObject();
	String processingPleaseWait_alert = "";
%>


<script type="text/javascript" src="<%=path%>/resources/js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="<%=path%>/resources/js/jquery.base64.js"></script>
<script type="text/javascript" src="<%=path%>/resources/js/jquery.md5.js"></script>
<script type="text/javascript" src="<%=path%>/resources/js/event.js"></script>

<!-- FilterSelect -->
<link type="text/css" rel="stylesheet" href="<%=path%>/resources/lib/filterSelect/filterSelect.css">
<script type="text/javascript" src="<%=path%>/resources/lib/filterSelect/filterSelect.js"></script>

<!-- layui -->
<link type="text/css" rel="stylesheet" href="<%=path%>/resources/lib/layui/css/layui.css">
<script type="text/javascript" src="<%=path%>/resources/lib/layui/layui.js"></script>



<script>
	var contextPath = '<%=path%>';
</script>

<%
	// JS 中使用的宏定义
	JSONObject definitionJSONObj = new JSONObject();
	Field[] field = Definition.class.getFields();
	for (int i = 0; i < field.length; i++) {
		int mod = field[i].getModifiers();
		if (Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
			definitionJSONObj.put(field[i].getName(), field[i].get(field[i].getGenericType()));
		}
	}
%>
<script>
	var Definition = <%=definitionJSONObj.toString()%>;
</script>

