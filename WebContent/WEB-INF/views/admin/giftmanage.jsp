<%@page import="com.angolacall.constants.UUTalkConfigKeys"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh">
<head>
<title>邀请赠送管理</title>
<jsp:include page="common/_head.jsp"></jsp:include>
</head>
<body>
	<jsp:include page="common/afterlogin_navibar.jsp"></jsp:include>
	
	<%
		String regGiftValue = (String) request.getAttribute(UUTalkConfigKeys.reg_gift_value.name());
	%>
	
	<div class="container">
		<div class="row">
			<div class="span8 offset3 tabbable tabs-left">
				<ul class="nav nav-tabs">
					<li class="active">
						<a data-toggle="tab" href="#pane-reg-gift-config">注册赠送配置</a>
					</li>
					<li class="">
						<a data-toggle="tab" href="#pane-charge-gift-config">充值赠送配置</a>
					</li>
				</ul>
				<div class="tab-content">	
                    <div id="pane-reg-gift-config" class="tab-pane active">
                        <h3>注册赠送配置</h3>
                        <hr>
                         <div id="reg_gift_ctrlgroup" class="control-group">
	                       	<label class="control-label" for="reg_gift_input">通过邀请成功注册赠送金额</label>
	                        <div class="controls">
		                         <div class="input-append float-left">
			                         <input id="reg_gift_input" class="span2" type="text" value="<%=regGiftValue %>"/>
			                         <button id="edit_reg_gift_btn" class="btn" type="button" >修改</button>
		                       	 </div>
		                       	 <span id="reg_gift_edit_text" class="help-inline"></span>
	                       	 </div>
                       	 </div>
                    </div>  
                    
					<div id="pane-charge-gift-config" class="tab-pane">
						<h3>充值赠送配置</h3>
						<hr>
					</div>

				</div>
			</div>
		</div>


	</div>
	<jsp:include page="common/_footer.jsp"></jsp:include>


	<script src="/angola/js/lib/jquery-1.8.0.min.js"></script>
	<script src="/angola/js/lib/bootstrap.min.js"></script>
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
				url : "/angola/admin/giftmanage/editRegGiftValue",
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
	
	</script>
</body>
</html>