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
			     ,{field: 'name', title:'名称',  width:110}
			     ,{field: 'periodicFlag', title:'周期性',  width:130}
			     ,{field: 'effectiveDay', title:'生效日',  width:110}
			     ,{field: 'amount', title:'金额',  width:110}
			     ,{field: 'frozenFlag', title:'冻结资金',  width:310}
			    ]]
			    ,data: <%=tvoReturn.toDataJSONArray()%>
			});

		});
	});

</script>

<table style="width:100%;height:100%;border:0px;">
	<tr>
		<td height="20px"><div style='font-weight:bold;color:green;margin:10px;'>收入类型列表</div></td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table class="layui-hide" id="dataTable" lay-filter="dataEvent" ></table>
		</td>
	</tr>
</table>