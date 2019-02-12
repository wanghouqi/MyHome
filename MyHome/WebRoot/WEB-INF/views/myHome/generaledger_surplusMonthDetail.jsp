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
			      {type:'numbers',  width:35}
			     ,{field: 'inOut', title:'收支',  width:60}
			     ,{field: 'typeName', title:'类型',  width:110}
			     ,{field: 'createDate', title:'日期',  width:105}
			     ,{field: 'amount_plan', title:'金额(计划)',  width:110}
			     ,{field: 'amount_actual', title:'金额(实际)',  width:110}
			     ,{field: 'desc', title:'描述',  width:310}
			    ]]
			    ,data: <%=tvoReturn.toDataJSONArray()%>
			    ,limit: <%=tvoReturn.size()%>
			});

		});
	});

</script>

<table style="width:100%;height:100%;border:0px;">
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table class="layui-hide" id="dataTable" lay-filter="dataEvent" ></table>
		</td>
	</tr>
</table>