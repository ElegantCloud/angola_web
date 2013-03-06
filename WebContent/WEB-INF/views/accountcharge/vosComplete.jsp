<%@page import="com.richitec.vos.client.VOSHttpResponse"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<% 
	String accountName = request.getParameter("accountName");
	String countryCode = request.getParameter("countryCode");
	accountName = countryCode + accountName;
	String cardNumber = request.getParameter("cardNumber");
	VOSHttpResponse vosResp = (VOSHttpResponse)request.getAttribute("vosResponse");
%>

<!DOCTYPE html>
<html lang="zh">
  <head>
    <title>环宇通充值结果</title>
	<jsp:include page="../common/_head.jsp"></jsp:include>
  </head>

  <body>
    <div class="container">
    	<div class="row">
	    	<div class="hero-unit">
	    		<% if (vosResp.getHttpStatusCode() != 200) { %>
	    		<h1>操作失败，请原谅。</h1>
	    		<hr>
	    		<div class="alert alert-error">
					<h2>充值账户：<%=accountName %></h2>
					<h2>充值卡号：<%=cardNumber %></h2>
	    			<h2>内部状态：<%=vosResp.getHttpStatusCode() %></h2>
	    		</div>
				<div class="alert alert-info">
	    			<h2>如需帮助，请联系客服。QQ：1622122511</h2>
					<hr>
					<h2><a href="http://www.uu-talk.com/uutalk/chongzhi">点击这里返回充值页面</a></h2>	    			
	    		</div>
	    		<% } else if (vosResp.isOperationSuccess()) {%>
				<h1>恭喜你，充值成功！</h1>
				<hr>
				<div class="alert alert-success">
					<h2>充值账户：<%=accountName %></h2>
					<h2>充值卡号：<%=cardNumber %></h2>
					<hr>
					<h2><a href="http://www.uu-talk.com/uutalk/chongzhi">点击这里返回充值页面</a></h2>					
				</div>
	    		<% } else { %>
	    		<h1>操作失败，请原谅。</h1>
	    		<hr>
	    		<div class="alert alert-error">
					<h2>充值账户：<%=accountName %></h2>
					<h2>充值卡号：<%=cardNumber %></h2>   	
					<%
						String errorInfo = "充值失败！";
						if (vosResp.getVOSStatusCode() == -10079) {
							errorInfo = "充值卡卡号或密码错误！";
						} else if (vosResp.getVOSStatusCode() == -10078) {
							errorInfo = "对不起，此充值卡已被使用！";
						}
					
					%>	
	    			<h2>错误信息：<%=errorInfo %></h2>
	    		</div>
				<div class="alert alert-info">
	    			<h2>如需帮助，请联系客服。QQ：1622122511</h2>
					<hr>
					<h2><a href="http://www.uu-talk.com/uutalk/chongzhi">点击这里返回充值页面</a></h2>	    			
	    		</div>
	    		<% } %>
	    	</div>
    	</div>
    </div> <!-- /container -->
    
  </body>
</html>
