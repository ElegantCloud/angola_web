<%@page import="com.angolacall.framework.Configuration"%>
<%@page import="com.angolacall.framework.ContextLoader"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>QQ LOGIN</title>
</head>
<body>
	<%
		Configuration cfg = ContextLoader.getConfiguration();
		String redirecturl = "https://openmobile.qq.com/oauth2.0/m_authorize?response_type=token&client_id=%s&redirect_uri=%s/share_assist/process_qq_redirect_url&scope=add_share&display=mobile";
		redirecturl = String.format(redirecturl, cfg.getQqAppId(),
				cfg.getServerUrl());
		response.sendRedirect(redirecturl);
	%>

</body>
</html>