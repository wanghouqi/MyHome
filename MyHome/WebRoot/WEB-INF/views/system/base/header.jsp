<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="hq.myhome.utils.Definition"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="hq.mydb.data.*"%>
<%
	String path = request.getContextPath();
	FormVO fvoUser = (FormVO) session.getAttribute(Definition.SESSION_ATTR_KEY_USER);
%>

<div class="layui-logo">My Home</div>
<!-- 头部区域（可配合layui已有的水平导航） -->
<ul class="layui-nav layui-layout-left">
	<li class="layui-nav-item"><a href="<%= path %>/generaledger">总账</a></li>
	<li class="layui-nav-item"><a href="">收支类型管理</a></li>
</ul>
<ul class="layui-nav layui-layout-right">
	<li class="layui-nav-item">
	<a href="javascript:;">
		<img src="<%= path + fvoUser.getCellVOValue("CN_ICO") %>" class="layui-nav-img"> <%= fvoUser.getCellVOValue("CN_NAME") %>
	</a>
		<dl class="layui-nav-child">
			<dd>
				<a href="">基本资料</a>
			</dd>
			<dd>
				<a href="">安全设置</a>
			</dd>
		</dl></li>
	<li class="layui-nav-item"><a href="<%= path %>/logout">退出</a></li>
</ul>
