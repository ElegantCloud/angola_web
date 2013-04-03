<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="com.angolacall.web.user.UserBean"%>
<%@page import="com.richitec.vos.client.VOSHttpResponse"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset={dede:global.cfg_soft_lang/}" />
<title>充值中心</title>
<link href="http://www.uu-talk.com/templets/uutalk/style/uutalk.css" rel="stylesheet" media="screen" type="text/css" />
</head>
<body>

<div class="header w960">
	
	<div class="nav-bar">
		<ul>
		<li><a href="http://www.uu-talk.com">首页</a></li>
		<li><a href="#">充值中心</a></li>
		<li><a href="http://www.uu-talk.com/a/zifeishuoming/">资费说明</a></li>
		<li><a href="http://www.uu-talk.com/a/bangzhu/">帮助</a></li>
		<li><a href="http://www.uu-talk.com/a/hezuojiameng/">合作加盟</a></li>
		</ul>
	</div>
</div>

<div class="content w960">
	<div class="chongzhi-banner">
		<div class="chongzhi-gift"></div>
		<a class="chongzhi50" href=""></a>
		<a class="chongzhi100" href=""></a>
		<a class="chongzhi200" href=""></a>
		<a class="chongzhi500" href=""></a>
		<a class="download-btn" href="http://www.uu-talk.com/appvcenter/downloadapp/4/android"></a>
	</div>
	<form id="azcardForm" action="/uutalk/azcardbalance" method="post">
		<div class="chongzhi-form-field">
			<label>查询充值卡余额请输入绑定的电话号码：</label>
			<input id="iptPhoneNumber" name=phoneNumber type="text" 
				pattern="\d{9}|\d{11}" maxlength="11" 
				null" />
			<input id="iptAZCardSubmit" type="submit" value="查询余额"/>
			<label class="red" id="lblErrorInfo"></label>
		</div>
	</form>

</div>

<div class="footer w960">
	<p class="copyright">Copyright © (HongKong) UuTalk Technology Limited. （香港）悠聊科技有限公司</p>
	<p class="powered">    
		Powered by 
		<a href="http://www.dedecms.com" title="织梦内容管理系统(DedeCms)" target="_blank">DedeCMS_V57_UTF8_SP1</a>
		 © 2004-2011 
		<a href="http://www.desdev.cn/" target="_blank">DesDev</a> 
		Inc.
	</p>
</div>

<script src="/uutalk/js/lib/jquery-1.8.0.min.js"></script>
<script type="text/javascript">
$(function(){
	var $ipt = $("#iptPhoneNumber");
	var $label = $("#lblErrorInfo");
	
	$("#azcardForm").submit(function(){
		$label.html("");
		
		var phoneNumber = $ipt.val();
		if (!$.isNumeric(phoneNumber) 
			||( phoneNumber.length != 9 && phoneNumber.length != 11))
		{
			$label.html("手机号码格式错误" + phoneNumber.length);
			return false;
		}
			
		var jqxhr = $.post("/uutalk/azcardbalance", 
			{phoneNumber: $ipt.val()},
			function(data){
				var status = data.status;
				if (0 == status){
					$label.html("账户余额：" + data.balance + "元");
				} else {
					$label.html("账户不存在");
				}
			}, "json");
		
        jqxhr.fail(function(jqXHR, textStatus, errorThown){
            if ("error" == textStatus){
            	$label.html("请求错误：" + jqXHR.status);
            } else {
            	$label.html("请求失败：" + textStatus);
            }
        });
        
		return false;
	});
});
</script>

</body>
</html>