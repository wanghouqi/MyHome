<%@page import="hq.mydb.utils.MyDBHelper"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="hq.mydb.data.*"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%
	String path = request.getContextPath();
	String yearMonthRange = request.getParameter("yearMonthRange");
	if (StringUtils.isEmpty(yearMonthRange)) {
		yearMonthRange = (String) request.getAttribute("yearMonthRange");
	}

	TableVO tvoCount = (TableVO) request.getAttribute("tvoCount");
	RowVO rvoHead = tvoCount.getHeadRowVO();
%>
<script>

	
	layui.use('laydate', function() {
		var laydate = layui.laydate;

		//执行一个laydate实例
		//年月范围
		laydate.render({
			elem : '#yearMonthRange',
			type : 'month',
			range : true,
			format : 'yyyy/MM',
			value : '<%=yearMonthRange%>',
			done: function(value, date, endDate){
				window.location.href = contentPath + "/generaledger?yearMonthRange="+value
			}
		});
	});
	layui.use('table', function(){
	  var table = layui.table;
	  table.render({
	    elem: '#dataTable'
	    ,width: $("#dataTD").width()
	    ,cols: [[ //表头
	      {field: 'type',  width:90, fixed: 'left'}
	     <%for (CellVO cvoHead : rvoHead.toCellVOs()) {
				out.print(",{field: '" + cvoHead.getValue() + "', title: '" + cvoHead.getValue() + "', width:100, align:'right'}");
			}%>
	    ]]
	    ,data: <%=tvoCount.toDataJSONArray()%>
	  });
	});
</script>

<table style="width:100%;height:100%;border:0px;">
	<!-- 日期选择 -->
	<tr>
		<td height="20px">
			<div class="layui-inline" style="height:20px;padding:10px 0px;">
				<label class="layui-form-label" style="height:20px;padding:0px 5px;font-size:12px;">年月范围</label>
				<div class="layui-input-inline" style="height:20px;">
					<input type="text" style="height:20px;" class="layui-input" id="yearMonthRange" placeholder=" - ">
				</div>
			</div>
			<button class="layui-btn layui-btn-xs" type="button">添加收支</button>
		</td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table id="dataTable"></table>
		</td>
	</tr>
</table>