<%@page import="hq.myhome.utils.Definition"%>
<%@page import="hq.mydb.utils.MyDBHelper"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="hq.mydb.data.*"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%
	String path = request.getContextPath();
%>
<script>
	$(document).ready(function() {
		layui.use([ 'form', 'layedit', 'laydate' ], function() {
			var form = layui.form,
				layer = layui.layer,
				laydate = layui.laydate;

			//日期
			laydate.render({
				elem : '#createDate',
				value : '<%=MyDBHelper.formatDate(new Date(), "yyyy-MM-dd")%>'
			});

			form.render();
		});

		// 保存按钮
		$("#saveBtn").click(function() {
			var amount = $("#amount").val();
			if (amount == "") {
				layer.alert('金额不能为空!', {
					icon : 2,
					skin : 'layer-ext-moon'
				})
				return;
			}
			layer.confirm('确认保存当前修改?', function(index) {
				formSubimt(contextPath + "/housingFundWithdrawal/save");
				window.parent.window.needRefresh(true); // 标识[总账]页面需要刷新
				// 关闭confirm和新增明细Layer
				parent.layer.closeAll();
			});
		});
	});
</script>

<table style="width:100%;height:100%;border:0px;">
	<!-- 表格 -->
	<tr>
		<td valign="top" height="30px" colspan="2">
			<div style="margin-top:5px;margin-left:15px;">
				<button type="button" class="layui-btn layui-btn-sm" id="saveBtn">保存</button>
			</div>
		</td>
	</tr>
	<!-- 表格 -->
	<tr>
		<td valign="top" id="dataTD">
			<div class="layui-form-item">
				<label class="layui-form-label">创建日期</label>
				<div class="layui-input-block">
					<input type="text" name="createDate" id="createDate" autocomplete="off" class="layui-input">
				</div>
			</div>
			<div class="layui-form-item">
				<label class="layui-form-label">金额</label>
				<div class="layui-input-block">
					<input type="text" name="amount" id="amount" autocomplete="off" class="layui-input" placeholder="￥">
				</div>
			</div>
			<div class="layui-form-item">
				<label class="layui-form-label">描述</label>
				<div class="layui-input-block">
					<textarea name="desc" placeholder="本次提取说明" class="layui-textarea"></textarea>
				</div>
			</div>
		</td>
		<td width="50px">&nbsp;</td>
	</tr>
</table>