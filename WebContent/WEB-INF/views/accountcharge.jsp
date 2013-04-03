<%@page import="com.angolacall.constants.ChargeType"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@page import="com.richitec.util.Pager"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.angolacall.constants.WebConstants"%>
<!DOCTYPE html>
<html lang="zh">
  <head>
    <title>悠聊-充值账户</title>
	<jsp:include page="common/_head.jsp"></jsp:include>
  </head>

  <body>
    <jsp:include page="common/afterlogin_navibar.jsp"></jsp:include>
	
	<%
		Double balance = (Double) request.getAttribute(WebConstants.balance.name());
         String strBalance = null;
         if (null != balance){
			 strBalance = String.format("%,.2f", balance.doubleValue());
         } else {
        	 strBalance = "Error";
         }
		
		List<Map<String, Object>> chargeList = (List<Map<String, Object>>) request.getAttribute(WebConstants.charge_list.name());
		Pager pager = (Pager) request.getAttribute(WebConstants.pager.name());
	%>


    <div class="container">
		<div class="row">
			<div class="span6 offset3">
			    <div class="clearfix">
					<h2 class="pull-left">账户余额：￥<%=strBalance %></h2>
					<a class="pull-right btn btn-large btn-success im-btn" href="deposite" target="blank">在线充值</a>
			    </div>
			    <hr>
				<table class="table table-striped">
					<thead>
						<tr>
						<th>充值日期</th>
						<th>充值金额</th>
						<th>充值方式</th>
						</tr>
					</thead>
					<tbody>
					<% for (int i=0; i<10; i++) {
						  String time = "&nbsp;";
						  String money = "&nbsp;";
						  String type = "&nbsp;";
						  if (null != chargeList && i<chargeList.size()) {
							  Map<String, Object> m = chargeList.get(i);
							  time = String.valueOf(m.get("charge_time"));
							  money = "￥" +  String.format("%,.2f", m.get("money"));
							  String chargeId = String.valueOf(m.get("chargeId"));
							  if (chargeId.startsWith(ChargeType.alipay.name())){
								  type = "支付宝";
							  } else if (chargeId.startsWith(ChargeType.card.name())) {
								  type = "悠聊充值卡";
							  } else if (chargeId.startsWith(ChargeType.invitereg.name())) {
								  type = "系统赠送[您邀请的用户成功注册]";
							  } else if (chargeId.startsWith(ChargeType.chargecontribute.name())) {
								  type = "系统赠送[您邀请的用户成功充值]";
							  } else if (chargeId.startsWith(ChargeType.chargegift.name())) {
								  type = "充值赠送";
							  }
						  }
					%>
						<tr>
							<td><%=time %></td>
							<td><%=money %></td>
							<td><%=type %></td>
						</tr>						
					<% }//end of for %>
					</tbody>
				</table>
				<ul class="pager">
					<li class="previous <%=pager.getHasPrevious() ? "" : "hidden" %>"><a href=<%=pager.getPreviousPage() %>>上一页</a></li>
					<li class="next <%=pager.getHasNext() ? "" : "hidden" %>"><a href=<%=pager.getNextPage() %>>下一页</a></li>
				</ul>
			</div>
		</div>
    	
		<jsp:include page="common/_footer.jsp"></jsp:include>
    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
    <script src="/uutalk/js/lib/bootstrap.min.js"></script>

  </body>
</html>
