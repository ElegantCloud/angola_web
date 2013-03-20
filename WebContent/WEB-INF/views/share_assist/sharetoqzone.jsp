<%@page import="com.angolacall.framework.ContextLoader"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Share to Qzone</title>
</head>
<body>
	<%
		String title = (String) session.getAttribute("title");
		String url = (String) session.getAttribute("url");
		String summary = (String) session.getAttribute("summary");
		String images = (String) session.getAttribute("images");
		String accessToken = (String) session.getAttribute("access_token");
		String openId = (String) session.getAttribute("openid");
	%>

	<script src="/angola/js/lib/jquery-1.8.0.min.js"></script>
	<script type="text/javascript">
	$.ajax({
		type : "post",
		url : "https://graph.qq.com/share/add_share",
		dataType : "json",
		crossDomain : true,
		data : {
			access_token : '<%=accessToken%>',
			oauth_consumer_key  : '<%=ContextLoader.getConfiguration().getQqAppId()%>',
			openid : '<%=openId%>',
			title : '<%=title%>',
			url : '<%=url%>',
			summary : '<%=summary%>',
			images : '<%=images%>',
			site : '安中通',
			fromurl : "http://www.00244dh.com",
			nswb : 1
		},
		success: function(data) {
			var ret = data.ret;
			var msg = data.msg;
			if (ret == 0) {
				alert("分享成功!");
			} else {
				alert("ret: " + ret + " msg: " + msg);
			}
		}
	});
	
	</script>


</body>
</html>