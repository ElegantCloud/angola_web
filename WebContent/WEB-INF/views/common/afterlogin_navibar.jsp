<%@page import="com.angolacall.constants.WebConstants"%>
<%@page import="com.angolacall.web.user.UserBean"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	UserBean userBean = (UserBean) session
			.getAttribute(UserBean.SESSION_BEAN);
	if (userBean == null) {
		response.sendRedirect("");
		return;
	}
	String pageName = String.valueOf(request
			.getAttribute(WebConstants.page_name.name()));
%>
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a> <a class="brand" href="http://www.00244dh.com/">安中通</a>
			<ul class="nav">
				<li><a id="username" class="im-attendee-name" title="<%=userBean.getUserName()%>">
				    <i class="icon-user"></i>&nbsp;<%=userBean.getDisplayName()%></a>
				</li>
				<li><a id="logout" href="/angola/signout">退出登录</a></li>
			</ul>
			<div class="nav-collapse">
				<ul class="nav pull-right">
					<li class="<%="accountcharge".equals(pageName) ? "active" : ""%>"><a href="/angola/accountcharge">充值账户</a></li>
					<li class="<%="setting".equals(pageName) ? "active" : ""%>"><a href="/angola/setting">系统设置</a></li>
				</ul>
			</div>
		</div>
	</div>
</div>