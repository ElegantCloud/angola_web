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
					恭喜你，注册成功！<a href="/uutalk/signin">点此登录</a>
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
			    		<button type="button" class="btn" id="btnGetPhoneCode">获取手机验证码</button>
			    		<span id="spanPhoneNumberInfo" class="help-inline">
			    		
			    		</span>
		    		</div>
	    		</div>
	    		<div id="divPhoneCodeCtrl" class="control-group">
	    		    <label class="control-label" for="iptPhoneCode">验证码</label>
	    		    <div class="controls">
		    		    <input type="text" name="phoneCode" id="iptPhoneCode" 
		    		    pattern="[0-9]{6}" maxlength="6" />
		    		    <span id="spanPhoneCodeInfo" class="help-inline">
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
		    	<!-- <div id="divConfirmPwdCtrl" class="control-group ">
		    	    <label class="control-label" for="iptConfirmPassword">确认密码</label>
		    	    <div class="controls">
		    		    <input type="password" name="confirmPassword" id="iptConfirmPassword" />
		    		    <span id="spanConfirmPasswordInfo" class="help-inline">
		    		    </span>
		    		</div>
		    	</div>
		    	<hr> -->
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
		$(function(){
			function disable60Seconds(){
				var $btn = $("#btnGetPhoneCode");
				var oldHtml = $btn.html();
	            $btn.attr("disabled", true);
	            var seconds = 60;
	            var itvl = setInterval(function(){
	                $btn.html(seconds + " 秒后可重试");
	                seconds -= 1;
	                if (seconds < 0){
	                    $btn.html(oldHtml);
	                    $("#spanPhoneNumberInfo").html("");
	                    $("#spanCountryCodeInfo").html("");
	                    clearInterval(itvl);
	                    $btn.attr("disabled", false);
	                }
	            }, 1000);
			}
			
			$("#btnGetPhoneCode").click(function(){
				var $span = $("#spanPhoneNumberInfo");
				var $divCtrl = $("#divPhoneNumberCtrl");
				var country_code = $("#countryCode").val();
				var phoneNumber = $("#iptPhoneNumber").val();
				
				if (country_code == "--") {
					$("#divCountryCode").addClass("error");
					$("#spanCountryCodeInfo").html("请选择您所在的国家");
					return false;
				}
				$("#divCountryCode").removeClass("error");
				
				if (!$.isNumeric(phoneNumber))
				{
					$divCtrl.addClass("error");
					$span.html("手机号码格式错误");
					return;
				} 
				$span.html("");
				$divCtrl.removeClass("error");
				$divCtrl.removeClass("success");
				$.post("/uutalk/user/getPhoneCode", 
					{ phone: phoneNumber,
					countryCode : country_code},
					function(data){
						if ("0" == data.result){
							disable60Seconds();
							$divCtrl.addClass("success");
							$span.html("验证码已发送，注意查看手机短信。");
						} else
						if ("3" == data.result){
							$divCtrl.addClass("error");
							$span.html("该号码已注册，请勿重复使用！");
						} else
						if ("2" == data.result) {
							$divCtrl.addClass("error");
							$span.html("无效的手机号码！");
						} else {
							$divCtrl.addClass("error");
							$span.html("错误：" + data.result);
						}
					}, "json");
			});
			
		});
		
		$("#formSignup").submit(function() {
			var referrer = $("#ipReferrer").val();
			var referrer_country_code = $("#ipReferrerCountryCode").val();
			var country_code = $("#countryCode").val();
			var phone_number = $("#iptPhoneNumber").val();
			var phone_code = $("#iptPhoneCode").val();
			var pwd = $("#iptPassword").val();
			
			$("#divError").addClass("hidden");
			$("#divCountryCode").removeClass("error");
			
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
			
			if (phone_code == null || phone_code == "") {
				$("#divPhoneCodeCtrl").addClass("error");
				$("#spanPhoneCodeInfo").html("请填入手机验证码");
				return false;
			}
			$("#divPhoneCodeCtrl").removeClass("error");
			
			if (pwd == null || pwd == "") {
				$("#divPasswordCtrl").addClass("error");
				$("#spanPasswordInfo").html("请输入密码");
				return false;
			}
			$("#divPasswordCtrl").removeClass("error");
			
			$.ajax({
				type : "post",
				url : "/uutalk/user/regViaInvite",
				dataType : "json",
				data : {
					"referrer" : referrer,
					"referrerCountryCode" : referrer_country_code,
					"countryCode" : country_code,
					"phoneNumber" : phone_number,
					"phoneCode" : phone_code,
					"password" : pwd,
					"confirmPassword" : pwd
				},
				statusCode :  {
					200 : function() {
						$("#formSignup").hide();
						$("#reg_success_div").removeClass("hidden");
					},
					401 : function() {
						$("#divError").removeClass("hidden");
						$("#error_text").html("验证码不正确，请检查您的输入");
						$("#divCountryCode").addClass("error");
						$("#divPhoneNumberCtrl").addClass("error");
						$("#divPhoneCodeCtrl").addClass("error");
					},
					500 : function() {
						$("#divError").removeClass("hidden");
						$("#error_text").html("服务器发生故障。。请稍后再试");
					}
				}
			});
			return false;
		});
	</script>
  </body>
</html>
