<%@page import="com.angolacall.constants.WebConstants"%>
<%@page import="com.angolacall.web.user.UserBean"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	UserBean userBean = (UserBean) session
			.getAttribute(UserBean.SESSION_BEAN);
	String pageName = String.valueOf(request
			.getAttribute(WebConstants.page_name.name()));
%>
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span> </a> <a
				class="brand" href="http://www.uu-talk.com/">环宇通</a>
			<div class="nav-collapse">
				<ul class="nav pull-right">
                    <li class="<%="signup".equals(pageName) ? "active" : ""%>">
                        <a href="/uutalk/signup">注册</a>
                    </li>
					<li class="<%="signin".equals(pageName) ? "active" : ""%>">
					<% if (userBean == null) { %>
						<a href="/uutalk/signin">登录</a>
					<% } else {	%>
						<a href="/uutalk/accountcharge">进入账户</a>
					<% } %>
					</li>
				    <li class="<%="deposite".equals(pageName) ? "active" : ""%>">
				        <a href="/uutalk/deposite">在线充值</a>
                    </li>
				</ul>
			</div>
		</div>
	</div>
</div>