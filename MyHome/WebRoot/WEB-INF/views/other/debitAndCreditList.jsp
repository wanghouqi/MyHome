<%@page import="hq.mydb.utils.MyDBHelper"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="hq.mydb.data.*"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%
	String path = request.getContextPath();
	TableVO tvoReturn = (TableVO) request.getAttribute("tvoReturn");
%>
<script>
	$(document).ready(function(){
		// 表格控件
		layui.use('table', function() {
			var table = layui.table;	
			table.render({
				elem : '#dataTable'
				,title : '明细数据维护'
			    ,cols: [[ //表头
			      {type:'numbers'}
			     ,{field: 'inFlag', title:'借入',  width:110}
			     ,{field: 'createDate', title:'创建日期',  width:130}
			     ,{field: 'amount', title:'金额',  width:110}
			     ,{field: 'desc', title:'描述'}
			    ]]
			    ,data: <%=tvoReturn.toDataJSONArray()%>
			});
		});
	});

</script>

<table style="width:100%;height:100%;border:0px;">
	<tr>
		<td height="20px"><div style='font-weight:bold;color:blue;margin:10px;'>借贷列表</div></td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table class="layui-hide" id="dataTable" lay-filter="dataEvent" ></table>
		</td>
	</tr>
</table>