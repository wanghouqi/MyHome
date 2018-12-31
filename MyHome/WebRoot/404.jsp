<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<!DOCTYPE html>
<html>
<head>
<title>404.html</title>

<meta name="keywords" content="keyword1,keyword2,keyword3">
<meta name="description" content="this is my page">
<meta name="content-type" content="text/html; charset=UTF-8">

<!--<link rel="stylesheet" type="text/css" href="./styles.css">-->

</head>
<%
	String path = request.getContextPath();
%>

<body>
	页面私奔了.正在努力抓捕.请先访问其它资源.
	<br>                   
	<form id="sysForm" name="sysForm" action="<%= path %>/back/userCSave" method="post">
		<input type="submit" name="asdfasdf">
	</form>
</body>
</html>
