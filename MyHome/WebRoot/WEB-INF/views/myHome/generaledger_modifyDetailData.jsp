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
			     ,{type: 'checkbox', width:40}
			     ,{field: 'typeName', title:'类型',  width:110}
			     ,{field: 'createDate', title:'日期',  width:130}
			     ,{field: 'amount', title:'金额',  width:110}
			     ,{field: 'desc', title:'描述',  width:310}
			    ]]
			    ,data: <%=tvoReturn.toDataJSONArray()%>
			});
		});
		
		 // 日历控件
		layui.use('laydate', function() {
			var laydate = layui.laydate;
			//执行一个laydate实例
			//年月范围
			laydate.render({
				elem : '[id^="createDate_"]'
			});
		});
		
		
		// 保存按钮
		$("#saveBtn").click(function(){
			layer.confirm('确认保存当前修改?', function(index){
				var saveJSONArray = [];
				$("#dataTD").find("tr").each(function(){
					var $tr = $(this);
					var saveJSONObject = {};
					saveJSONObject.id=$tr.find("[name^='id_']").val();
					saveJSONObject.createDate=$tr.find("[name^='createDate_']").val();
					saveJSONObject.amount=$tr.find("[name^='amount_']").val();
					if(saveJSONObject.id != null && saveJSONObject.id != undefined){
						saveJSONArray.push(saveJSONObject);
					}
			    });
			    $.post(contextPath + "/generaledger/modifyDetailData/save",
			    {
			        saveJSONArray: JSON.stringify(saveJSONArray)
			    });
			    window.parent.window.needRefresh(true);// 标识[总账]页面需要刷新
				parent.layer.closeAll();
			});
		});
		
		// 删除按钮
		$("#delBtn").click(function(){
			layer.confirm('真的删除行么?', function(index){
				var delIdJSONArray = [];
				$("#dataTD").find("input:checkbox:checked").each(function(){
					var $tr = $(this).closest("tr");
					delIdJSONArray.push($tr.find("[name^='id_']").val());
					$tr.remove();
			    });
			    $.post(contextPath + "/generaledger/modifyDetailData/delete",
			    {
			        delIdJSONArray:delIdJSONArray.join(',')
			    });
			    window.parent.window.needRefresh(true);// 标识[总账]页面需要刷新
			    layer.close(index);
			});
		});
 
	});
	
	
	
</script>

<table style="width:100%;height:100%;border:0px;">
	<!-- 表格 -->
	<tr>
		<td valign="top" height="30px">
			<div style="margin-top:5px;margin-left:15px;">
				<button type="button" class="layui-btn layui-btn-sm" id="delBtn">删除</button>
				<button type="button" class="layui-btn layui-btn-sm" id="saveBtn">保存</button>
			</div>
		</td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table class="layui-hide" id="dataTable" lay-filter="dataEvent"></table>
		</td>
	</tr>
</table>