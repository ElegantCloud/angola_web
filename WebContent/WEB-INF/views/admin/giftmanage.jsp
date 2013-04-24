<%@page import="com.angolacall.constants.UUTalkConfigKeys"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh">
<head>
<title>赚话费管理</title>
<jsp:include page="common/_head.jsp"></jsp:include>
</head>
<body>
	<jsp:include page="common/afterlogin_navibar.jsp"></jsp:include>
	
	<%
			String regGiftValue = (String) request
					.getAttribute(UUTalkConfigKeys.reg_gift_value.name());
			String regGiftDesc = (String) request.getAttribute(UUTalkConfigKeys.reg_gift_desc_text.name());
			String inviteChargeGiftDesc = (String) request.getAttribute(UUTalkConfigKeys.invite_charge_invite_desc_text.name());
			String adClickGiftMoney = (String) request.getAttribute(UUTalkConfigKeys.ad_click_gift_money.name());
	%>
	
	<div class="container">
		<div class="row">
			<div class="span8 offset3 tabbable tabs-left">
				<ul class="nav nav-tabs">
					<li class="active">
						<a data-toggle="tab" href="#pane-reg-gift-config">好友注册赠送配置</a>
					</li>
					<li class="">
						<a data-toggle="tab" href="#pane-charge-gift-config">好友充值赠送配置</a>
					</li>
					<li class="">
						<a data-toggle="tab" href="#pane-ad-click-gift-config">广告点击赠送配置</a>
					</li>
				</ul>
				<div class="tab-content">	
                    <div id="pane-reg-gift-config" class="tab-pane active">
                        <h3>好友注册赠送配置</h3>
                        <hr>
						<div id="reg_gift_ctrlgroup" class="control-group">
							<label class="control-label" for="reg_gift_input">通过邀请成功注册赠送邀请者金额</label>
							<div class="controls">
								<div class="input-append float-left">
									<input id="reg_gift_input" class="span2" type="text"
										value="<%=regGiftValue%>" />
									<button id="edit_reg_gift_btn" class="btn" type="button">保存</button>
								</div>
								<span id="reg_gift_edit_text" class="help-inline"></span>
							</div>
						</div>
		
						<div id="reg_gift_desc_ctrlgroup" class="control-group">
							<label class="control-label" for="reg_gift_desc_ta">客户端显示信息</label>
							<div class="controls">
								<textarea id="reg_gift_desc_ta" wrap="virtual" rows="4" cols="10" ><%=regGiftDesc %></textarea>
								<span id="reg_gift_desc_text" class="help-inline"></span>
							</div>
							<button id="reg_gift_desc_button" class="btn" type="button">保存</button>
						</div>
					</div>  
                    
					<div id="pane-charge-gift-config" class="tab-pane">
						<h3>好友充值赠送配置</h3>
						<hr>
						<div id="invite_charge_gift_desc_ctrlgroup" class="control-group">
							<label class="control-label" for="invite_charge_gift_desc_ta">客户端显示信息</label>
							<div class="controls">
								<textarea id="invite_charge_gift_desc_ta" wrap="virtual" rows="4" cols="10" ><%=inviteChargeGiftDesc %></textarea>
								<span id="invite_charge_gift_desc_text" class="help-inline"></span>
							</div>
							<button id="invite_charge_gift_desc_button" class="btn" type="button">保存</button>
						</div>
					</div>
					<div id="pane-ad-click-gift-config" class="tab-pane">
						<h3>广告点击赠送配置</h3>
						<hr>
						<div id="ad_click_gift_ctrlgroup" class="control-group">
							<label class="control-label" for="ad_click_gift_ipt">客户端显示信息</label>
							<div class="controls">
								<div class="input-append float-left">
									<input id="ad_click_gift_ipt" class="span2" type="text"
										value="<%=adClickGiftMoney%>" />
									<button id="ad_click_gift_button" class="btn" type="button">保存</button>
								</div>
								<span id="ad_click_gift_edit_text" class="help-inline"></span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>


	</div>
	<jsp:include page="common/_footer.jsp"></jsp:include>


	<script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
	<script src="/uutalk/js/lib/bootstrap.min.js"></script>
	<script type="text/javascript">
		$("#edit_reg_gift_btn").click(function() {
			var giftValue = $("#reg_gift_input").val();
			if (giftValue == null || giftValue == "") {
				$("#reg_gift_ctrlgroup").addClass("warning");
				$("#reg_gift_edit_text").html("请输入赠送金额！");
				return false;
			}
			$.ajax({
				type : "post",
				url : "/uutalk/admin/giftmanage/editRegGiftValue",
				dataType : "json",
				data : {
					regGiftValue : giftValue
				},
				success : function(jqxhr, textStatus) {
						$("#reg_gift_ctrlgroup").removeClass("warning");
						$("#reg_gift_ctrlgroup").removeClass("error");
						$("#reg_gift_edit_text").html("金额修改成功！");
				},
				error : function(jqXHR, textStatus) {
					switch(jqXHR.status) {
					case 406: 
						$("#reg_gift_ctrlgroup").addClass("warning");
						$("#reg_gift_edit_text").html("请输入合法的金额数字（如1，1.0或1.00）！");
						break;
					default:
						$("#reg_gift_ctrlgroup").addClass("error");
						$("#reg_gift_edit_text").html("系统内部出错(STATUS CODE: " + jqXHR.status + ")");
						break;
					}
				}	
			});
		});
	
	
		$("#reg_gift_desc_button").click(function() {
			var descText = $("#reg_gift_desc_ta").val();
			$("#reg_gift_desc_ctrlgroup").removeClass("error");
			$("#reg_gift_desc_text").html("");
			$.ajax({
				type : "post",
				url : "/uutalk/admin/giftmanage/editRegGiftDesc",
				dataType : "json",
				data : {
					regGiftDesc : descText
				},
				success : function(jqxhr, textStatus) {
					$("#reg_gift_desc_text").html("信息保存成功");
				},
				error : function(jqXHR, textStatus) {
					$("#reg_gift_desc_ctrlgroup").addClass("error");
					$("#reg_gift_desc_text").html("系统内部出错(STATUS CODE: " + jqXHR.status + ")");
				}
			});
		});
		
		$("#invite_charge_gift_desc_button").click(function() {
			var descText = $("#invite_charge_gift_desc_ta").val();
			$("#invite_charge_gift_desc_ctrlgroup").removeClass("error");
			$("#invite_charge_gift_desc_text").html("");
			$.ajax({
				type : "post",
				url : "/uutalk/admin/giftmanage/editInviteChargeGiftDesc",
				dataType : "json",
				data : {
					inviteChargeGiftDesc : descText
				},
				success : function(jqxhr, textStatus) {
					$("#invite_charge_gift_desc_text").html("信息保存成功");
				},
				error : function(jqXHR, textStatus) {
					$("#invite_charge_gift_desc_ctrlgroup").addClass("error");
					$("#invite_charge_gift_desc_text").html("系统内部出错(STATUS CODE: " + jqXHR.status + ")");
				}
			});
		});
		
		$("#ad_click_gift_button").click(function() {
			var money = $("#ad_click_gift_ipt").val();
			if (giftValue == null || giftValue == "") {
				$("#ad_click_gift_ctrlgroup").addClass("warning");
				$("#ad_click_gift_edit_text").html("请输入金额！");
				return false;
			}
			$.ajax({
				type : "post",
				url : "/uutalk/admin/giftmanage/editAdClickGiftMoney",
				dataType : "json",
				data : {
					"money" : money
				},
				success : function(jqxhr, textStatus) {
						$("#ad_click_gift_ctrlgroup").removeClass("warning");
						$("#ad_click_gift_ctrlgroup").removeClass("error");
						$("#ad_click_gift_edit_text").html("金额修改成功！");
				},
				error : function(jqXHR, textStatus) {
					switch(jqXHR.status) {
					case 406: 
						$("#ad_click_gift_ctrlgroup").addClass("warning");
						$("#ad_click_gift_edit_text").html("请输入合法的金额数字（如1，1.0或1.00）！");
						break;
					default:
						$("#ad_click_gift_ctrlgroup").addClass("error");
						$("#ad_click_gift_edit_text").html("系统内部出错(STATUS CODE: " + jqXHR.status + ")");
						break;
					}
				}	
			});
		});
		
	</script>
</body>
</html>