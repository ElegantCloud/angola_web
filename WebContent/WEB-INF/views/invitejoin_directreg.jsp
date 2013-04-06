<%@page import="com.angolacall.constants.AuthConstant"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@page import="com.angolacall.mvc.controller.UserController" %>
<%
  	String userName = (String) request.getAttribute(AuthConstant.username.name());
	String countryCode = (String) request.getAttribute(AuthConstant.countryCode.name());
%>    
<!DOCTYPE html>
<html lang="zh">
  <head>
    <title>注册新用户</title>
	<jsp:include page="common/_head.jsp"></jsp:include>
  </head>

  <body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="brand">注册新用户</a>
			</div>
		</div>
	</div>

	<div class="container">
    	<div class="row">
    		<div id="reg_success_div" class="span5 offset4 hidden">
				<h3>
					恭喜你，注册成功！<a href="/appvcenter/downloadapp/4/android">点此下载悠聊客户端</a>
				</h3>
			</div>
    	
	    	<form id="formSignup" class="im-form span6 offset3">
			    <h3>您的朋友邀请您加入悠聊</h3>
			    <hr>
			    <div id="divError" class="alert alert-error hidden">
			     <strong>错误：</strong><span id="error_text"></span>
			    </div>
			    
			    <input id="ipReferrer" type="hidden" name="referrer" value="<%=userName %>" />
			    <input id="ipReferrerCountryCode" type="hidden" name="referrerCountryCode" value="<%=countryCode %>"/>
			    
			    <div id="divCountryCode" class="control-group">
			    	<label class="control-label">国家代码</label>
			    	<div class="controls">
			    		<select id="countryCode" name="countryCode">
			    			<jsp:include page="common/countrycode_options.jsp"></jsp:include>
			    		</select>
			    		<span id="spanCountryCodeInfo" class="help-inline">
			    		</span>
			    	</div>
			    </div>
	    		<div id="divPhoneNumberCtrl" class="control-group">
	    		    <label class="control-label" for="iptPhoneNumber">手机号</label>
	    		    <div class="controls">
			    		<input type="text" name="phoneNumber" id="iptPhoneNumber" 
			    		pattern="[0-9]{11}" maxlength="11"  />
			    		<span id="spanPhoneNumberInfo" class="help-inline">
			    		
			    		</span>
		    		</div>
	    		</div>
		    	<div id="divPasswordCtrl" class="control-group ">
		    	    <label class="control-label" for="iptPassword">密码</label>
		    	    <div class="controls">
		    		    <input type="password" name="password" id="iptPassword" />
		    		    <span id="spanPasswordInfo" class="help-inline">
		    		    </span>
		    		</div>
		    	</div>
		    	<div id="divConfirmPwdCtrl" class="control-group ">
		    	    <label class="control-label" for="iptConfirmPassword">确认密码</label>
		    	    <div class="controls">
		    		    <input type="password" name="confirmPassword" id="iptConfirmPassword" />
		    		    <span id="spanConfirmPasswordInfo" class="help-inline">
		    		    </span>
		    		</div>
		    	</div>
		    	<hr>
		    	<div class="control-group">
		    	    <div class="controls">
			    	    <button type="submit" class="btn btn-primary">提交注册信息</button>
			    	</div>
		    	</div>
	    	</form>
    	</div>
 	
		<jsp:include page="common/_footer.jsp"></jsp:include>
    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
    <script src="/uutalk/js/lib/bootstrap.min.js"></script>
	<script type="text/javascript">
		$("#formSignup").submit(function() {
			var referrer = $("#ipReferrer").val();
			var referrer_country_code = $("#ipReferrerCountryCode").val();
			var country_code = $("#countryCode").val();
			var phone_number = $("#iptPhoneNumber").val();
			var pwd = $("#iptPassword").val();
			var confirmPwd = $("#iptConfirmPassword").val();
			
			$("#divError").addClass("hidden");
			$("#divCountryCode").removeClass("error");
			
			$("#spanCountryCodeInfo").html("");
			$("#spanPhoneNumberInfo").html("");
			$("#spanPasswordInfo").html("");
			$("#spanConfirmPasswordInfo").html("");
			
			if (country_code == "--") {
				$("#divCountryCode").addClass("error");
				$("#spanCountryCodeInfo").html("请选择您所在的国家");
				return false;
			}
			
			if (phone_number == null || phone_number == "") {
				$("#divPhoneNumberCtrl").addClass("error");
				$("#spanPhoneNumberInfo").html("请填入您的手机号码");
				return false;
			}
			$("#divPhoneNumberCtrl").removeClass("error");
			
			if (pwd == null || pwd == "") {
				$("#divPasswordCtrl").addClass("error");
				$("#spanPasswordInfo").html("请输入密码");
				return false;
			}
			$("#divPasswordCtrl").removeClass("error");
			
			if (confirmPwd == null || confirmPwd == "") {
				$("#divConfirmPwdCtrl").addClass("error");
				$("#spanConfirmPasswordInfo").html("请输入确认密码");
				return false;
			}
			$("#divConfirmPwdCtrl").removeClass("error");
			
			$.ajax({
				type : "post",
				url : "/uutalk/user/regViaInviteDirectReg",
				dataType : "json",
				data : {
					"referrer" : referrer,
					"referrerCountryCode" : referrer_country_code,
					"countryCode" : country_code,
					"phoneNumber" : phone_number,
					"password" : pwd,
					"confirmPassword" : confirmPwd
				},
			
				success : function(data) {
					switch (data.result) {
					case "ok":
						$("#formSignup").hide();
						$("#reg_success_div").removeClass("hidden");
						break;

					case "empty_phone":
						$("#divPhoneNumberCtrl").addClass("error");
						$("#spanPhoneNumberInfo").html("请填入您的手机号码");
						break;
						
					case "invalid_phone":
						$("#divPhoneNumberCtrl").addClass("error");
						$("#spanPhoneNumberInfo").html("不是有效的手机号码");
						break;
						
					case "phone_cant_start_with_country_code":
						$("#divPhoneNumberCtrl").addClass("error");
						$("#spanPhoneNumberInfo").html("请直接输入手机号码，不要以国家代码开头！");
						break;

					case "existed":
						$("#divPhoneNumberCtrl").addClass("error");
						$("#spanPhoneNumberInfo").html("手机号码已被注册");
						break;
						
					case "empty_password":
						$("#divPasswordCtrl").addClass("error");
						$("#spanPasswordInfo").html("请输入密码");
						break;
						
					case "password_different_to_confirm_password":
						$("#divError").removeClass("hidden");
						$("#divPasswordCtrl").addClass("error");
						$("#divConfirmPwdCtrl").addClass("error");
						$("#error_text").html("密码与确认密码不一致");
						break;
						
					default:
						$("#divError").removeClass("hidden");
						$("#error_text").html("服务器故障，暂时无法注册，请稍后重试");
						break;
					}

				}
			});
			return false;
		});
	</script>
  </body>
</html>
