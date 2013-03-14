<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.angolacall.web.user.UserBean"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="zh">
  <head>
    <title>环宇通-在线充值</title>
	<jsp:include page="common/_head.jsp"></jsp:include>
  </head>

  <body>
	<%
		List<Map<String, Object>> chargeMoneyList = (List<Map<String, Object>>) request.getAttribute("charge_money_list");
		String userName = (String) request.getAttribute("username"); 
		
		String accountError = (String) request.getAttribute("AccountError");
	%>

    <div class="container">
    	<div class="row">
    		<div class="span6 offset3">
	    		<form id="formAlipayWap" action="alipayWapPost" method="post" target="_self">
		    		<h3>您充值的账户为</h3>
		    		<input id="account_name_input" type="text" 
		    		name="username"	pattern="\d{9}|\d{11}" maxlength="11"
		    		value="<%=userName != null ? userName : "" %>" <%if (userName != null && !"".equals(userName)) {%> readonly="readonly" <% } %> />
	    			<%if(null != accountError) { %>
						<label class="text-error">账户不存在，请检查账户名是否正确</label>
					<% } %>
					<h3>请选择充值金额</h3>
					<ul class="unstyled">
						<%
							if (chargeMoneyList != null) {
								boolean checked = false;
								for (Map<String, Object> item : chargeMoneyList) {
									Integer id = (Integer) item.get("id");
									Float chargeMoney = (Float) item.get("charge_money");
									String description = (String) item.get("description");
									%>
									<li>
										<p>
											<input id="<%=id %>" class="pull-left" type="radio" name="depositeId" value="<%=id %>" <%if(!checked) {%>checked="checked"<% checked=true;} %>" />
											<label for="<%=id%>">￥<%=chargeMoney.toString() + "&nbsp;--&nbsp;" + description%></label>
										</p>
									</li>
									<%
								}
							}
						%>
					</ul>
					<hr>
					<button id="btnGoToAlipay" type="submit" class="btn btn-warning">去支付宝充值</button>
	    		</form>
    		</div>
    	</div>
 	
    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
    <script src="/uutalk/js/lib/bootstrap.min.js"></script>
    <script src="/uutalk/js/applib/common.js"></script>
  </body>
</html>
