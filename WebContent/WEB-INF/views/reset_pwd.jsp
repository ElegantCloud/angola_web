<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="zh">
  <head>
    <title>环宇通-密码重置</title>
	<jsp:include page="common/_head.jsp"></jsp:include>
  </head>

  <body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="http://www.uu-talk.com/">环宇通</a>
			</div>
		</div>
	</div>

	<%
		Map<String, Object> user = (Map<String, Object>) request.getAttribute("user");
		String randomId = (String) request.getAttribute("random_id");
	%>

	<div class="container">
    	<div class="row span6 offset3">
			<%
				if (user != null) {
			%>
    		<form id="formResetPassword" action="/uutalk/user/resetPwd" class="im-form">
                <h2>重新设置密码</h2>
                <input id="random_id" type="hidden" name="random_id" value="<%=randomId %>" />
                <div id="divPassword" class="control-group">
                    <label class="control-label" for="iptPassword">输入新密码</label>
                    <div class="controls">
                        <input type="password" name="password" id="iptPassword" />
                        <span id="spanPasswordInfo" class="help-inline"></span>
                    </div>
                </div>
                <div id="divConfirmPassword" class="control-group">
                    <label class="control-label" for="iptConfirmPassword">再次输入新密码</label>
                    <div class="controls">
                        <input type="password" name="password1" id="iptConfirmPassword" />
                        <span id="spanConfirmPasswordInfo" class="help-inline"></span>
                    </div>
                </div>
                <hr>
                <div id="divSubmit" class="control-group">
                    <div class="controls">
                        <button type="submit" class="btn btn-primary">提交新的密码</button>
                        <span id="spanSubmit" class="help-inline"></span>
                    </div>
                </div>
    		</form>
    		<div id="reset_ok_info" class="hidden">
    			<h3>恭喜你，密码重置成功！请在环宇通客户端用新密码重新登录！</h3>
    			<div class="link_region">
					<a href="http://www.uu-talk.com">返回首页</a>(环宇通)
				</div>
    		</div>
    		<%
				} else {
			%>
				<p>对不起，此链接已经失效！</p>	
				<div class="link_region">
					<a href="http://www.uu-talk.com">返回首页</a>(环宇通)
				</div>
			<%	
				}
    		%>
    		
    		
    	</div>
 	
    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
    <script src="/uutalk/js/lib/bootstrap.min.js"></script>
	<script type="text/javascript">
	function isValidInput($divCtrl, value){
        var $span = $divCtrl.find(".help-inline");
        if (value == ""){
            $divCtrl.addClass("error");
            $span.html("不能为空");
            return false;
        } else {
            $divCtrl.removeClass("error");
            $span.html("");
            return true;            
        }
    }
	
	$("#formResetPassword").submit(function(){
		var isValid = true;
		
		var randomId = $("#random_id").val();
		
		var $divPassword = $("#divPassword");
		var newPassword = $("#iptPassword").val();
        isValid = isValidInput($divPassword, newPassword) && isValid;
        
        var $divCofirmPassword = $("#divConfirmPassword");
		var newPasswordConfirm = $("#iptConfirmPassword").val();
        isValid = isValidInput($divCofirmPassword, newPasswordConfirm) && isValid;
        
        if (!isValid){
        	return false;
        }
        
        $divSubmit = $("#divSubmit");
        $spanSubmit = $("#spanSubmit");
        
		$.post("/uutalk/user/resetPwd",
			{
				random_id: randomId,
				password: newPassword,
				password1: newPasswordConfirm
			},
			
		function(data) {
				if ("password_reset_ok" == data) {
					$divSubmit.removeClass("error");
					$spanSubmit.html("");
					$("#formResetPassword").addClass("hidden");
					$("#reset_ok_info").removeClass("hidden");
					
				} else if ("passwords_not_equal" == data) {
					$divSubmit.addClass("error");
					$spanSubmit.html("新密码两次输入不一致");
				} else if ("user_not_found" == data) {
					$divSubmit.addClass("error");
					$spanSubmit.html("无此用户");
				} else if ("password_reset_failed" == data) {
					$divSubmit.addClass("error");
					$spanSubmit.html("重置密码失败");
				} else {
					$divSubmit.addClass("error");
					$spanSubmit.html("未知错误：[" + data + "]");
				}
			});
		
			return false;
		});
	</script>
  </body>
</html>
