<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.angolacall.web.user.UserBean"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="zh">
  <head>
    <title>悠聊-在线充值</title>
	<jsp:include page="common/_head.jsp"></jsp:include>
  </head>

  <body>
    <jsp:include page="common/beforelogin_navibar.jsp"></jsp:include>
	
	<%
		UserBean userBean = (UserBean) session.getAttribute(UserBean.SESSION_BEAN);
	
		List<Map<String, Object>> chargeMoneyList = (List<Map<String, Object>>) request.getAttribute("charge_money_list");
	%>

    <div class="container">
    	<div class="row">
    		<div class="span6 offset3">
    			<h2>请选择你喜欢的充值方式</h2>
    			<small>如需帮助请联系客服，QQ： 1622122511</small>
	    		<hr>
    		</div>
    		<div class="tabbable span6 offset3">
    			<ul id="pay_type" class="nav nav-tabs">
    				<li type="alipay" class="active">
    					<a href="#pane-pay" data-toggle="tab">支付宝充值</a>
    				</li>
    				<!--  
    				<li type="netbank">
    					<a href="#pane-pay" data-toggle="tab">网银充值</a>
    				</li>
    				-->
    				<li type="card">
    					<a href="#pane-zhihuicard" data-toggle="tab">充值卡充值</a>
    				</li>
    			</ul>
				<div class="tab-content">
					<div class="tab-pane active" id="pane-pay">
			    		<form id="formAlipay" action="alipay" method="post">
			    			<label>国家代码</label>
				    		<select name="countryCode">
				    			<jsp:include page="common/countrycode_options.jsp"></jsp:include>
				    		</select>
			    		
				    		<label>请输入要充值的账户名</label>
				    		<input id="account_name_input" type="text" 
				    		name="accountName"	pattern="\d{9}|\d{11}" maxlength="11"
				    		value="<%=userBean != null ? userBean.getUserName() : ""%>" />
				    		
							<label>请选择充值金额（单位：元）</label>
							<select name="depositeId">
								<%
									if (chargeMoneyList != null) {
										for (Map<String, Object> item : chargeMoneyList) {
											Integer id = (Integer) item.get("id");
											Float chargeMoney = (Float) item.get("charge_money");
											String description = (String) item.get("description");
											%>
											<option value="<%=id.toString() %>"><%=chargeMoney.toString() + "(" + description + ")" %></option>
											<%
										}
									}
								%>
							</select>
							<hr>
							<button id="btnGoToAlipay" type="submit" class="btn btn-warning">去支付宝充值</button>
			    		</form>
					</div>		
					<div class="tab-pane" id="pane-zhihuicard">
						<form id="formCard" action="cardchargepage", method="post">
							<label>国家代码</label>
				    		<select name="countryCode">
				    			<jsp:include page="common/countrycode_options.jsp"></jsp:include>
				    		</select>
							<label>请输入要充值的账户名</label>
				    		<input id="iptCardAccounName" type="text" 
				    		name="accountName" pattern="\d{9}|\d{11}" maxlength="11"
				    		value="<%=userBean != null ? userBean.getUserName() : ""%>" />						
							<label>请输入充值卡卡号</label>
							<input id="iptCardPin" type="text" name="cardNumber" />
							<label>请输入充值卡密码</label>
							<input id="iptCardPassword" type="text" name="cardPwd" />
							<hr>
							<button id="btnCardSubmit" type="submit" class="btn btn-success">确&nbsp;定</button>						
						</form>
					</div>						
				</div>    			
    		</div>
    	</div>
 	
		<jsp:include page="common/_footer.jsp"></jsp:include>
    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
    <script src="/uutalk/js/lib/bootstrap.min.js"></script>
    <script src="/uutalk/js/applib/common.js"></script>
  </body>
</html>
