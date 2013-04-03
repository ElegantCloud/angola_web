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
	
	String keyValue   		     		= formatString(cfg.getYeepayKey());   					// �̼���Կ
	String nodeAuthorizationURL  	= formatString(cfg.getYeepayCommonReqURL());  	// ���������ַ
	// �̼������û�������Ʒ��֧����Ϣ
	String    pd_FrpId           	= formatString((String)request.getAttribute("yeepayTunnel"));  // ֧��ͨ������
	
	String    p0_Cmd 		     			= formatString("Buy");                               									// ����֧�����󣬹̶�ֵ ��Buy��
	String    p1_MerId 		    		= formatString(cfg.getYeepayMerchantId()); 		// �̻����
	
	ChargeType chargeType = ChargeType.yeepay;
	if ("SZX-NET".equals(pd_FrpId)) {
		chargeType = ChargeType.szx_card;
	} else if ("UNICOM-NET".equals(pd_FrpId)) {
		chargeType = ChargeType.unicom_card;
	} else if ("TELECOM-NET".equals(pd_FrpId)) {
		chargeType = ChargeType.telecom_card;
	}
	String    p2_Order           	= ChargeUtil.getOrderNumber(chargeType.name(), countryCode, accountName);           					// �̻�������
	String	  p3_Amt           	 	= String.format("%.2f", chargeMoney.floatValue());      	   							// ֧�����
	String	  p4_Cur    		 			= formatString("CNY");	   		   							// ���ױ���
	String	  p5_Pid 		     			= "AnZhongTong Account Charge";	       	   						// ��Ʒ����
	String	  p6_Pcat  		     		= "";	       	   					// ��Ʒ����
	String 	  p7_Pdesc   		 			= "";		   								// ��Ʒ����
	String 	  p8_Url 	         		= formatString(cfg.getYeepayNotifyReturnUrl()); 		       						// �̻�����֧���ɹ����ݵĵ�ַ
	String 	  p9_SAF 		     			= "0"; 			   							// ��Ҫ��д�ͻ���Ϣ 0������Ҫ  1:��Ҫ
	String 	  pa_MP 			 				= "";         	   						// �̻���չ��Ϣ
	      	   					
	// ���б�ű����д
	pd_FrpId = pd_FrpId.toUpperCase();
	String 	  pr_NeedResponse    	= formatString("1");    // Ĭ��Ϊ"1"����ҪӦ�����
  String 	  hmac 			     			= formatString("");							               							// ����ǩ����
    
    // ���MD5-HMACǩ��
    hmac = PaymentForOnlineService.getReqMd5HmacForOnlinePayment(p0_Cmd,
			p1_MerId,p2_Order,p3_Amt,p4_Cur,p5_Pid,p6_Pcat,p7_Pdesc,
			p8_Url,p9_SAF,pa_MP,pd_FrpId,pr_NeedResponse,keyValue);
  
  
    ContextLoader.getChargeDAO().addChargeRecord(p2_Order, countryCode, accountName, chargeMoney.doubleValue(), ChargeStatus.processing, chargeMoneyId);
%>
<html>
	<head>
		<title>�ױ�֧��
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
