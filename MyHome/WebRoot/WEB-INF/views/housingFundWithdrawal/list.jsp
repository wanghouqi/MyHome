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
				,toolbar: '#barDemo' //开启工具栏
			    ,cols: [[ //表头
			     {type: 'checkbox', field: 'id', title:'',  width:50}
			     ,{type:'numbers'}
			     ,{field: 'createDate', title:'创建日期',  width:130}
			     ,{field: 'amount', title:'金额',  width:110}
			     ,{field: 'desc', title:'描述'}
			    ]]
			    ,data: <%=tvoReturn.toDataJSONArray()%>
			});
			
			
			//头工具栏事件
		  table.on('toolbar(dataEvent)', function(obj){
		    var checkStatus = table.checkStatus(obj.config.id);
		    switch(obj.event){
		      case 'del':
			layer.confirm('真的删除行么?', function(index){
		        var data = JSON.stringify(checkStatus.data);
		        var xqo = eval('(' + data + ')');
		        var delIds = "";
				for(var i in xqo){
				　　delIds += ","+xqo[i].id;
				}
				
			$.ajax({  
			    type : "post",  
			    url : contextPath+"/housingFundWithdrawal/delete",  
			    data : {delIds : delIds},  
			    async : false,  
			    dataType : "json"
		   	});

			// 刷新页面
			location.reload();
		   	});
		      break;
		      
		      case 'add':
		        // 新增公积金提取
		        /**
				*	弹出明细数据的新增Layer
				*/
				layer.open({
					  type: 2,
					  title: '新增提取公积金',
					  shadeClose: true,
					  shade: 0.8,
					  area: ['700px', '95%'],
					  content: contextPath+'/housingFundWithdrawal/add' //iframe的url
					  ,end: function(index, layero){ 
					  	if(needRefreshFlag){
					  		setTimeout(function(){location.reload();},500); // 如果明细有修改,则需要刷新页面.
					  	}
					  }  
					}); 
		      break;
		      
		      //自定义头工具栏右侧图标 - 提示
		      case 'LAYTABLE_TIPS':
		        layer.alert('这是工具栏右侧自定义的一个图标按钮');
		      break;
		    };
		  });
			
		});
		
	});
	
	
	/**
	*	弹出子页时,如果有修改记录则需要刷新
	*/
	var needRefreshFlag = false;
	function needRefresh(flag){
		needRefreshFlag = flag;
	}

		
</script>

<table style="width:100%;height:100%;border:0px;">
	<tr>
		<td height="20px"><div style='font-weight:bold;color:blue;margin:10px;'>公积金提现列表</div></td>
	</tr>
	<!-- 按钮 -->
	<tr>
		<td valign="top" height="30px" colspan="2"><script type="text/html" id="barDemo">
		<a class="layui-btn layui-btn-xs" lay-event="add">新增</a>
		<a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">删除</a>
	</script></td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<table class="layui-hide" id="dataTable" lay-filter="dataEvent"></table>
		</td>
	</tr>
</table>