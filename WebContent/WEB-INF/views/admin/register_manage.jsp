<%@page import="com.angolacall.constants.RegisterActivityStatus"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.angolacall.constants.UUTalkConfigKeys"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh">
<head>
<title>注册管理</title>
<jsp:include page="common/_head.jsp"></jsp:include>
</head>
<body>
	<jsp:include page="common/afterlogin_navibar.jsp"></jsp:include>

	<%
		String defaultRegMoney = (String) request.getAttribute(UUTalkConfigKeys.default_register_money.name());
		Map<String, Object> regActivityMap = (Map<String, Object>) request.getAttribute("register_activity");
		String defaultAngolaCallbackRegMoney = (String) request.getAttribute(UUTalkConfigKeys.ao_cb_register_money.name());
	%>
	<div class="container">
		<div class="row">
			<div class="span6 offset3">
				<div>
					<h4>注册默认赠送金额</h4>
					<div class="input-append float-left">
						<input id="default_register_money_ipt" class="span2" type="text"
							value="<%=defaultRegMoney %>" pattern="(\d+\.\d{2})|(\d+)" />
						<button type="button" class="btn" id="edit_reg_money_btn">保存</button>
					</div>
					<span class="help-inline" id="reg_money_edit_info"></span>
				</div>
				<br />
				<hr />

				<div>
					<h4>注册送话费活动管理</h4>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>开始时间</th>
								<th>结束时间</th>
								<th>赠送金额</th>
								<th>状态</th>
								<th>操作</th>
							</tr>
						</thead>
						<tbody id="reg_activity_body">
							<%
								if (regActivityMap != null) {
									Integer id = (Integer) regActivityMap.get("id");
									String startDate = String.valueOf(regActivityMap.get("start_date"));
									String endDate = String.valueOf(regActivityMap.get("end_date"));
									Float giftMoney = (Float) regActivityMap.get("gift_money");
									String status = (String) regActivityMap.get("status");
								
							%>
							<tr id="<%=id %>">
								<td class="start_date_td"><%=startDate %></td>
								<td class="end_date_td"><%=endDate %></td>
								<td class="gift_money_td"><%=String.format("%.2f", giftMoney) %></td>
								<%
										if (RegisterActivityStatus.open.name().equals(status)) {
									%>
								<td>已开启</td>
								<td><a title="关闭" class="op_close_bt" href="#"><i
										class="icon-stop"></i></a> <a title="编辑" class="op_edit_bt"
									href="#"><i class="icon-edit"></i></a></td>
								<%
										} else {
									%>
								<td>已关闭</td>
								<td><a title="开启" class="op_open_bt" href="#"><i
										class="icon-play"></i></a> <a title="编辑" class="op_edit_bt"
									href="#"><i class="icon-edit"></i></a></td>
								<%
										}
									%>
							</tr>
							<%
								}	
							%>
						</tbody>
					</table>
				</div>
				
				<br />
				<hr />
				<div>
					<h4>安哥拉回拨注册默认赠送金额</h4>
					<div class="input-append float-left">
						<input id="angola_callback_default_register_money_ipt" class="span2" type="text"
							value="<%=defaultAngolaCallbackRegMoney %>" pattern="(\d+\.\d{2})|(\d+)" />
						<button type="button" class="btn" id="edit_angola_callback_reg_money_btn">保存</button>
					</div>
					<span class="help-inline" id="angola_callback_reg_money_edit_info"></span>
				</div>
			</div>
		</div>


	</div>
	<jsp:include page="common/_footer.jsp"></jsp:include>

	<div id="edit_reg_activity_dlg" class="modal hide fade">
		<div class="modal-header">
			<h4>编辑注册送话费活动</h4>
		</div>
		<div class="modal-body">
			<form id="edit_reg_activity_form" class="span4 offset1" method="post"
				action="">
				<div>
					<input id="edit_reg_activity_id_ipt" type="hidden" /> 
					<label>开始时间:</label>
					<input id="start_date_ipt" class="span2" name="start_date"
						type="text" value="" onfocus="WdatePicker({dateFmt:'yyyy-MM-dd'})" />
					<label>结束时间:</label> 
					<input id="end_date_ipt" class="span2"
						name="end_date" type="text" value=""
						onfocus="WdatePicker({dateFmt:'yyyy-MM-dd', minDate:(document.getElementById('start_date_ipt')).value})" /> 
					<label>赠送金额:</label>
					<input id="gift_money_ipt" class="span2" name="gift_money"
						type="text" value="" pattern="(\d+\.\d{2})|(\d+)"/>
				</div>
			</form>
		</div>
		<div class="modal-footer">
			<a href="#" class="btn" data-dismiss="modal"
				id="edit_reg_activity_close">关闭</a> <a id="edit_reg_activity_save"
				href="#" class="btn btn-primary">保存</a>
		</div>
	</div>

	<script src="/angola/js/lib/jquery-1.8.0.min.js"></script>
	<script src="/angola/js/lib/bootstrap.min.js"></script>
	<script type="text/javascript"
		src="/angola/js/lib/My97DatePicker/WdatePicker.js"></script>
	<script type="text/javascript">
		$("#edit_reg_money_btn").click(
				function() {
					var money = $("#default_register_money_ipt").val();
					$("#reg_money_edit_info").html("");
					if (money == null || money == "") {
						$("#reg_money_edit_info").html("请输入金额！");
						return false;
					}

					$.ajax({
						type : "post",
						url : "/angola/admin/registermanage/editRegMoney",
						dataType : "json",
						data : {
							"money" : money
						},
						success : function(data, textStatus, jqxhr) {
							$("#reg_money_edit_info").html("金额修改成功！");
						},
						error : function(jqXHR, textStatus) {
							if (jqXHR.status == 406) {
								$("#reg_money_edit_info").html(
										"请输入正确的金额(格式:0.00)");
							} else if (jqXHR.status == 401) {
								location = "/angola/admin/registermanage";
							} else {
								$("#reg_money_edit_info").html(
										"系统内部错误（STATUS CODE: " + jqXHR.status
												+ ")");
							}
						}
					});
				});

		$("#reg_activity_body").delegate(".op_close_bt", "click", function() {
			var $tr = $(this).parent().parent();
			var id = $tr.attr('id');

			$.ajax({
				type : "post",
				url : "/angola/admin/registermanage/closeRegisterActivity",
				dataType : "json",
				data : {
					"id" : id
				},
				success : function(data, textStatus, jqxhr) {
					location = "/angola/admin/registermanage";
				},
				error : function(jqXHR, textStatus) {
					if (jqXHR.status == 401) {
						location = "/angola/admin/registermanage";
					} else {
						alert("系统内部错误（STATUS CODE: " + jqXHR.status + ")");
					}
				}
			});
		});

		$("#reg_activity_body").delegate(".op_open_bt", "click", function() {
			var $tr = $(this).parent().parent();
			var id = $tr.attr('id');

			$.ajax({
				type : "post",
				url : "/angola/admin/registermanage/openRegisterActivity",
				dataType : "json",
				data : {
					"id" : id
				},
				success : function(data, textStatus, jqxhr) {
					location = "/angola/admin/registermanage";
				},
				error : function(jqXHR, textStatus) {
					if (jqXHR.status == 401) {
						location = "/angola/admin/registermanage";
					} else {
						alert("系统内部错误（STATUS CODE: " + jqXHR.status + ")");
					}
				}
			});
		});

		$("#reg_activity_body").delegate(".op_edit_bt", "click", function() {
			var $tr = $(this).parent().parent();
			var id = $tr.attr('id');
			var startDate = ($tr.find(".start_date_td")).html();
			var endDate = ($tr.find(".end_date_td")).html();
			var giftMoney = ($tr.find(".gift_money_td")).html();
			
			$("#edit_reg_activity_id_ipt").val(id);
		 	$("#start_date_ipt").val(startDate);
			$("#end_date_ipt").val(endDate); 
			$("#gift_money_ipt").val(giftMoney);
			
			$("#edit_reg_activity_dlg").modal();
		});

		$("#edit_reg_activity_close").click(function() {
			$("#edit_reg_activity_id_ipt").val("");
			$("#start_date_ipt").val("");
			$("#end_date_ipt").val(""); 
			$("#gift_money_ipt").val("");
			return true;
		});
		
		$("#edit_reg_activity_save").click(function() {
			var id = $("#edit_reg_activity_id_ipt").val();
			var startDate = $("#start_date_ipt").val();
			var endDate = $("#end_date_ipt").val();
			var giftMoney = $("#gift_money_ipt").val();
			
			if (startDate == null || startDate == "") {
				alert("请输入开始时间！");
				return false;
			}
			
			if (endDate == null || endDate == "") {
				alert("请输入结束时间！");
				return false;
			}
			
			var startDateArray = startDate.split('-');
			var endDateArray = endDate.split('-');
			
			var start_date = new Date();
			start_date.setFullYear(startDateArray[0], startDateArray[1], startDateArray[2]);
			var end_date = new Date();
			end_date.setFullYear(endDateArray[0], endDateArray[1], endDateArray[2]);
			
			if (start_date > end_date) {
				alert("开始时间不能大于结束时间！");
				return false;
			}
			
			if (giftMoney == null || giftMoney == "") {
				alert("请输入赠送金额！");
				return false;
			}
			
			$.ajax({
				type : "post",
				url : "/angola/admin/registermanage/editRegisterActivity",
				dataType : "json",
				data : {
					"id" : id,
					"startDate" : startDate,
					"endDate" : endDate,
					"giftMoney" : giftMoney
				},
				success : function(data, textStatus, jqxhr) {
					location = "/angola/admin/registermanage";
				},
				error : function(jqXHR, textStatus) {
					if (jqXHR.status == 406) {
						alert("请输入正确的金额(格式:0.00)");
					} else if (jqXHR.status == 401) {
						location = "/angola/admin/registermanage";
					} else {
						alert("系统内部错误（STATUS CODE: " + jqXHR.status + ")");
					}
				}
			});
		});
		
		$("#start_date_ipt").blur(function() {
			$("#end_date_ipt").attr("onfocus", "WdatePicker({dateFmt:'yyyy-MM-dd', minDate:(document.getElementById('start_date_ipt')).value})");
		});
		
		$("#edit_angola_callback_reg_money_btn").click(function() {
			var money = $("#angola_callback_default_register_money_ipt").val();
			$("#angola_callback_reg_money_edit_info").html("");
			if (money == null || money == "") {
				$("#angola_callback_reg_money_edit_info").html("请输入金额！");
				return false;
			}

			$.ajax({
				type : "post",
				url : "/angola/admin/registermanage/editAngolaCallbackRegisterMoney",
				dataType : "json",
				data : {
					"money" : money
				},
				success : function(data, textStatus, jqxhr) {
					$("#angola_callback_reg_money_edit_info").html("金额修改成功！");
				},
				error : function(jqXHR, textStatus) {
					if (jqXHR.status == 406) {
						$("#angola_callback_reg_money_edit_info").html(
								"请输入正确的金额(格式:0.00)");
					} else if (jqXHR.status == 401) {
						location = "/angola/admin/registermanage";
					} else {
						$("#angola_callback_reg_money_edit_info").html(
								"系统内部错误（STATUS CODE: " + jqXHR.status
										+ ")");
					}
				}
			});
		});
	</script>
</body>
</html>