<%@page import="com.angolacall.framework.ContextLoader"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>分享结果</title>
<jsp:include page="../common/_head.jsp"></jsp:include>
</head>
<body>
	 <%!	String formatString(String text){ 
			if(text == null) {
				return ""; 
			}
			return text;
		}
	%>
	<div class="container">
		<div class="row">
			<div class="offset3 span5">
				<%
		String title = formatString((String) session.getAttribute("title"));
		String url = formatString((String) session.getAttribute("url"));
		String summary = formatString((String) session.getAttribute("summary"));
		String images = formatString((String) session.getAttribute("images"));
		Integer retCode = (Integer) request.getAttribute("ret_code");
		
		if (retCode == 0) {
			%>
				<h3>您已成功分享以下内容到QQ空间</h3>
				<blockquote>
					<p><%=summary %></p>
				</blockquote>
				<%
		} else {
			%>
				<h2>分享失败…</h2>
				<p class="text-error">错误码：<%=retCode %></p>
				<%
		}
	%>

			</div>
		</div>
	</div>

</body>
</html>