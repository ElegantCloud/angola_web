<%@page import="com.angolacall.constants.ChargeStatus"%>
<%@page import="java.util.Map"%>
<%@page import="com.angolacall.constants.ChargeType"%>
<%@page import="com.angolacall.mvc.model.charge.ChargeUtil"%>
<%@page import="com.angolacall.framework.ContextLoader"%>
<%@page import="com.angolacall.framework.Configuration"%>
<%@page language="java" contentType="text/html;charset=gbk"%>
<%@page import="com.yeepay.*"%>
<%!	String formatString(String text){ 
			if(text == null) {
				return ""; 
			}
			return text;
		}
%>
<%
	Configuration cfg = ContextLoader.getConfiguration();
	//request.setCharacterEncoding("GBK");
	String accountName = formatString((String)request.getAttribute("accountName"));
	String countryCode = formatString((String)request.getAttribute("countryCode"));
	String chargeMoneyId = formatString(request.getParameter("depositeId"));
	
	Map<String, Object> chargeMoneyRecord = ContextLoader.getChargeMoneyConfigDao().getChargeMoneyRecord(Integer.parseInt(chargeMoneyId));
	Float chargeMoney = (Float) chargeMoneyRecord.get("charge_money");
	
	String keyValue   		     		= formatString(cfg.getYeepayKey());   					// 商家密钥
	String nodeAuthorizationURL  	= formatString(cfg.getYeepayCommonReqURL());  	// 交易请求地址
	// 商家设置用户购买商品的支付信息
	String    pd_FrpId           	= formatString((String)request.getAttribute("yeepayTunnel"));  // 支付通道编码
	
	String    p0_Cmd 		     			= formatString("Buy");                               									// 在线支付请求，固定值 ”Buy”
	String    p1_MerId 		    		= formatString(cfg.getYeepayMerchantId()); 		// 商户编号
	
	ChargeType chargeType = ChargeType.yeepay;
	if ("SZX-NET".equals(pd_FrpId)) {
		chargeType = ChargeType.szx_card;
	} else if ("UNICOM-NET".equals(pd_FrpId)) {
		chargeType = ChargeType.unicom_card;
	} else if ("TELECOM-NET".equals(pd_FrpId)) {
		chargeType = ChargeType.telecom_card;
	}
	String    p2_Order           	= ChargeUtil.getOrderNumber(chargeType.name(), countryCode, accountName);           					// 商户订单号
	String	  p3_Amt           	 	= String.format("%.2f", chargeMoney.floatValue());      	   							// 支付金额
	String	  p4_Cur    		 			= formatString("CNY");	   		   							// 交易币种
	String	  p5_Pid 		     			= "UU-Talk Account Charge";	       	   						// 商品名称
	String	  p6_Pcat  		     		= "";	       	   					// 商品种类
	String 	  p7_Pdesc   		 			= "";		   								// 商品描述
	String 	  p8_Url 	         		= formatString(cfg.getYeepayNotifyReturnUrl()); 		       						// 商户接收支付成功数据的地址
	String 	  p9_SAF 		     			= "0"; 			   							// 需要填写送货信息 0：不需要  1:需要
	String 	  pa_MP 			 				= "";         	   						// 商户扩展信息
	      	   					
	// 银行编号必须大写
	pd_FrpId = pd_FrpId.toUpperCase();
	String 	  pr_NeedResponse    	= formatString("1");    // 默认为"1"，需要应答机制
  String 	  hmac 			     			= formatString("");							               							// 交易签名串
    
    // 获得MD5-HMAC签名
    hmac = PaymentForOnlineService.getReqMd5HmacForOnlinePayment(p0_Cmd,
			p1_MerId,p2_Order,p3_Amt,p4_Cur,p5_Pid,p6_Pcat,p7_Pdesc,
			p8_Url,p9_SAF,pa_MP,pd_FrpId,pr_NeedResponse,keyValue);
  
  
    ContextLoader.getChargeDAO().addChargeRecord(p2_Order, countryCode, accountName, chargeMoney.doubleValue(), ChargeStatus.processing, chargeMoneyId);
%>
<html>
	<head>
		<title>易宝支付
		</title>
	</head>
	<body>
		<form id="yeepayForm" name="yeepay" action='<%=nodeAuthorizationURL%>' method='POST'>
			<input type='hidden' name='p0_Cmd'   value='<%=p0_Cmd%>'>
			<input type='hidden' name='p1_MerId' value='<%=p1_MerId%>'>
			<input type='hidden' name='p2_Order' value='<%=p2_Order%>'>
			<input type='hidden' name='p3_Amt'   value='<%=p3_Amt%>'>
			<input type='hidden' name='p4_Cur'   value='<%=p4_Cur%>'>
			<input type='hidden' name='p5_Pid'   value='<%=p5_Pid%>'>
			<input type='hidden' name='p6_Pcat'  value='<%=p6_Pcat%>'>
			<input type='hidden' name='p7_Pdesc' value='<%=p7_Pdesc%>'>
			<input type='hidden' name='p8_Url'   value='<%=p8_Url%>'>
			<input type='hidden' name='p9_SAF'   value='<%=p9_SAF%>'>
			<input type='hidden' name='pa_MP'    value='<%=pa_MP%>'>
			<input type='hidden' name='pd_FrpId' value='<%=pd_FrpId%>'>
			<input type="hidden" name="pr_NeedResponse"  value="<%=pr_NeedResponse%>">
			<input type='hidden' name='hmac'     value='<%=hmac%>'>
			<input type='submit' style="display: none;"/>
		</form>
		<script>document.forms['yeepayForm'].submit();</script>
	</body>
</html>
