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
	// 日历空间
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
				window.location.href = contextPath + "/generaledger?yearMonthRange="+value
			}
		});
	});
	// 数据表格
	layui.use('table', function(){
	  var table = layui.table;
	  table.render({
	    elem: '#dataTable'
	    ,width: $("#dataTD").width()
	    ,cols: [[ //表头
	      {field: 'type',  width:90, fixed: 'left'}
	     <%for (CellVO cvoHead : rvoHead.toCellVOs()) {
				out.print(",{field: '" + cvoHead.getValue() + "', title: '" + cvoHead.getValue()
						+ "', width:110, align:'right', }");
			}%>
	    ]]
	    ,data: <%=tvoCount.toDataJSONArray()%>
	  });
	 
	});

	/**
	*	弹出明细数据的修改Layer
	*	@param typeKey : 当前行类型的key
	*	@param yearMonth : 当前列的年月 201812
	*/
	function modifyDetailData(typeKey, yearMonth){
		layer.open({
		  type: 2,
		  title: '明细数据维护',
		  shadeClose: true,
		  shade: 0.8,
		  area: ['750px', '90%'],
		  content: contextPath+'/generaledger/modifyDetailData?typeKey='+typeKey+"&yearMonth="+yearMonth //iframe的url
		  ,end: function(index, layero){ 
		  	if(needRefreshFlag){
		  		setTimeout(function(){location.reload();},500); // 如果明细有修改,则需要刷新页面.
		  	}
		  }  
		}); 
	}

	/**
	*	弹出当月结余明细数据的Layer
	*	@param yearMonth : 当前列的年月 201812
	*/
	function surplusMonthDetail(yearMonth){
		layer.open({
		  type: 2,
		  title: '明细数据维护',
		  shadeClose: true,
		  shade: 0.8,
		  area: ['845px', '90%'],
		  content: contextPath+'/generaledger/surplusMonthDetail?yearMonth='+yearMonth //iframe的url
		  ,end: function(index, layero){ 
		  	if(needRefreshFlag){
		  		setTimeout(function(){location.reload();},500); // 如果明细有修改,则需要刷新页面.
		  	}
		  }  
		}); 
	}
	
	/**
	*	弹出明细数据的新增Layer
	*/
	function newDetailData(){
		layer.open({
		  type: 2,
		  title: '明细数据新增',
		  shadeClose: true,
		  shade: 0.8,
		  area: ['700px', '95%'],
		  content: contextPath+'/generaledger/newDetailData' //iframe的url
		  ,end: function(index, layero){ 
		  	if(needRefreshFlag){
		  		setTimeout(function(){location.reload();},500); // 如果明细有修改,则需要刷新页面.
		  	}
		  }  
		}); 
	}
	
	/**
	*	弹出子页时,如果有修改记录则需要刷新
	*/
	var needRefreshFlag = false;
	function needRefresh(flag){
		needRefreshFlag = flag;
	}
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
			<button class="layui-btn layui-btn-xs" type="button" onclick="newDetailData()">添加收支</button>
		</td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table id="dataTable"></table>
		</td>
	</tr>
</table>