
<%@page import="com.angolacall.constants.Pages"%>
<%@page import="com.angolacall.constants.WebConstants"%>
<%@page import="com.angolacall.web.user.AdminUserBean"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	AdminUserBean userBean = (AdminUserBean) session
	.getAttribute(AdminUserBean.SESSION_BEAN);
	if (userBean == null) {
		response.sendRedirect("/uutalk/admin/");
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
			</a> <a class="brand">悠聊管理系统</a>
			<ul class="nav">
				<li><a id="username" class="im-attendee-name">
				    <i class="icon-user"></i>&nbsp;<%=userBean.getUserName() %></a>
				</li>
				<li><a id="logout" href="/uutalk/admin/signout">退出登录</a></li>
			</ul>
			<div class="nav-collapse">
				<ul class="nav pull-right">
					<li class="<%=Pages.gift_manage.name().equals(pageName) ? "active" : ""%>"><a href="/uutalk/admin/giftmanage">赚话费管理</a></li>
					<li class="<%=Pages.charge_manage.name().equals(pageName) ? "active" : ""%>"><a href="/uutalk/admin/chargemanage">充值管理</a></li>
					<li class="<%=Pages.register_manage.name().equals(pageName) ? "active" : ""%>"><a href="/uutalk/admin/registermanage">注册管理</a></li>
					<li class="<%=Pages.notice_manage.name().equals(pageName) ? "active" : ""%>"><a href="/uutalk/admin/noticemanage">消息管理</a></li>
				</ul>
			</div>
		</div>
	</div>
</div>