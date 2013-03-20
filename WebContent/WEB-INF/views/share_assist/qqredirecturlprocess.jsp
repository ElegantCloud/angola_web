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
	<script type="text/javascript">
		var url = location.href;
		var paramters = url.substring(url.indexOf("?#") + 2);
		var new_url = "<%=ContextLoader.getConfiguration().getServerUrl()%>/share_assist/share_to_qzone?" + paramters;
		location = new_url;
	</script>


</body>
</html>