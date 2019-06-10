
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="0">
<%
	String path = request.getContextPath();
%>
<title>My Home</title>
<style>
a, address, b, big, blockquote, body, center, cite, code, dd, del, div,
	dl, dt, em, fieldset, font, form, h1, h2, h3, h4, h5, h6, html, i,
	iframe, img, ins, label, legend, li, ol, p, pre, small, span, strong, u,
	ul, var {
	margin: 0;
	padding: 0
}

body {
	overflow-y: hidden;
}

td {
	border: 0px;
}

.layui-form-checkbox i{
	margin-top: 6px;
}
</style>

<script>
	/**
	 * 提交当前页面数据到action
	 */
	function formSubimt(action) {
		if (action) {
			$("#sysForm").attr("action", action);
		}
		$("#sysForm").submit();
	}
</script>
</head>



<body class="layui-layout-body">
	<div class="layui-layout layui-layout-admin">
		<div class="layui-header">
			<!-- 页头 -->
			<t:insertAttribute name="javaScriptLib" ignore="true" />
			<t:insertAttribute name="header" ignore="true" />
		</div>

		
		<div class="layui-body" style="left:0px;">
			<!-- 页面内容 -->
			<form class="layui-form" id="sysForm" name="sysForm" action="" method="post">
				<t:insertAttribute name="body" />
			</form>
		</div>

		<div class="layui-footer" style="left:0px;">
			<!-- 页脚 -->
			<t:insertAttribute name="footer" ignore="true" />
		</div>
	</div>
</body>

</html>