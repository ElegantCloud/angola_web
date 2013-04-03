<%@page import="com.angolacall.mvc.controller.ProfileController"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="com.angolacall.web.user.UserBean"%>
<%
	UserBean userBean = (UserBean) session
			.getAttribute(UserBean.SESSION_BEAN);
%>
<!DOCTYPE html>
<html lang="zh">
<head>
<title>悠聊-系统设置</title>
<jsp:include page="common/_head.jsp"></jsp:include>
</head>

<body>
	<jsp:include page="common/afterlogin_navibar.jsp"></jsp:include>

	<div class="container">
		<div class="row">
			<div class="span6 offset4 tabbable tabs-left">
					<div class="tab-pane active" id="pane-change-password">
						<h3>修改密码</h3>
						<hr>
						<form action="/uutalk/setting/changepassword" method="post"
							id="formChangePwd" class="im-form">
							<div class="control-group info">
								<label class="control-label">登录名</label>
								<div class="controls">
									<input type="text" disabled="disabled"
										value="<%=userBean.getUserName()%>"> <span
										class="help-inline">不可更改</span>
								</div>
							</div>
							<div id="divOldPwd" class="control-group">
								<label class="control-label">输入当前使用的密码</label> <input
									type="password" name="oldPwd" id="iptOldPwd" /> <span
									class="help-inline"></span>
							</div>
							<div id="divNewPwd" class="control-group">
								<label class="control-label">输入新密码</label> <input
									type="password" name="newPwd" id="iptNewPwd" /> <span
									class="help-inline"></span>
							</div>
							<div id="divNewPwdConfirm" class="control-group">
								<label class="control-label">再次输入新密码</label> <input
									type="password" name="newPwdConfirm" id="iptNewPwdConfirm" />
								<span class="help-inline"></span>
							</div>
							<div id="divSubmit" class="control-group">
								<button type="submit" class="btn btn-primary">确&nbsp;定</button>
								<span class="help-inline"></span>
							</div>
						</form>
					</div>

			</div>
		</div>
		<jsp:include page="common/_footer.jsp"></jsp:include>
	</div>
	<!-- /container -->

	<!-- Le javascript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
	<script src="/uutalk/js/lib/bootstrap.min.js"></script>
	<script type="text/javascript" src="/uutalk/js/lib/md5.js"></script>
	<script type="text/javascript">
		function isValidPassword($div, val) {
			var $span = $div.find(".help-inline");
			if (val == "") {
				$div.addClass("error");
				$span.html("输入不能为空");
				return false;
			} else {
				$div.removeClass("error");
				$span.html("");
				return true;
			}
		}

		$("#formChangePwd")
				.submit(
						function() {
							var $divOldPwd = $("#divOldPwd");
							var oldPwd = $("#iptOldPwd").val();
							var $divNewPwd = $("#divNewPwd");
							var newPwd = $("#iptNewPwd").val();
							var $divNewPwdConfirm = $("#divNewPwdConfirm");
							var newPwdConfirm = $("#iptNewPwdConfirm").val();
							if (isValidPassword($divOldPwd, oldPwd)
									&& isValidPassword($divNewPwd, newPwd)
									&& isValidPassword($divNewPwdConfirm,
											newPwdConfirm)) {
								var $divSubmit = $("#divSubmit");
								var $spanSubmit = $divSubmit
										.find(".help-inline");
								var jqxhr = $
										.post(
												"setting/changepassword",
												{
													oldPwd : md5(oldPwd),
													newPwd : newPwd,
													newPwdConfirm : newPwdConfirm
												},
												function(data) {
													if ("200" == data) {
														$divSubmit
																.removeClass("error");
														$spanSubmit.html("");
														alert("修改密码成功");
													} else if ("400" == data) {
														$divSubmit
																.addClass("error");
														$spanSubmit
																.html("原始密码输入错误");
													} else if ("403" == data) {
														$divSubmit
																.addClass("error");
														$spanSubmit
																.html("新密码两次输入不一致");
													} else if ("500" == data) {
														$divSubmit
																.addClass("error");
														$spanSubmit
																.html("服务器内部错误");
													} else {
														$divSubmit
																.addClass("error");
														$spanSubmit
																.html("未知错误：["
																		+ data
																		+ "]");
													}
												});
								jqxhr.fail(function(jqXHR, textStatus,
										errorThown) {
									$divSubmit.addClass("error");
									if ("error" == textStatus) {
										$spanSubmit
												.html("请求错误：" + jqXHR.status);
									} else {
										$spanSubmit.html("请求失败：" + textStatus);
									}
								});
								jqxhr.always(function() {
									$("#iptOldPwd").val("");
									$("#iptNewPwd").val("");
									$("#iptNewPwdConfirm").val("");
								});
							}
							return false;
						});

		$("#iptNewNickname").focus(function() {
			$("#infoNickname").html("");
		});
	</script>
</body>
</html>
