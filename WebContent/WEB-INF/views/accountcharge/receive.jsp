<%@page import="com.angolacall.constants.WebConstants"%>
<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="zh">
<head>
<title>悠聊充值结果</title>
<jsp:include page="../common/_head.jsp"></jsp:include>
</head>
<body>
	<div class="container">
		<div class="pay_result_center">
			<div>
			<%
				String result = String.valueOf(request.getAttribute("result"));
				if (result.equals("0")) {
					String accountName = (String) request.getAttribute(WebConstants.pay_account_name.name());
					String chargeMoney = (String) request.getAttribute(WebConstants.charge_money.name());
					if (accountName == null) {
					%>
						<p>充值出现异常，系统未能成功入账，请联系管理员(QQ：1622122511)！</p>
					<%
					} else {
					%>
						<p>用户您好,</p>
						<br/>
						<p>您的账户：<%=accountName %> 成功充值<%=chargeMoney %>元。</p>
					<%
						
					}
				} else {
			%>
					<p>用户您好,</p>
					<br/>
					<p>对不起，充值失败！</p>
			<%
				}
			%>
			<div class="link_region">
				<a href="http://www.uu-talk.com">返回首页</a>
				<a href="http://www.uu-talk.com/uutalk/chongzhi">继续充值</a>
			</div>
			</div>
		</div>
	</div>

</body>
</html>