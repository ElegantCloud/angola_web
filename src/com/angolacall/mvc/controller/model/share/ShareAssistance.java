package com.angolacall.mvc.controller.model.share;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Configurable;

import com.angolacall.framework.Configuration;
import com.angolacall.framework.ContextLoader;
import com.richitec.util.HttpUtils;
import com.richitec.util.HttpUtils.HttpResponseResult;
import com.richitec.util.HttpUtils.PostRequestFormat;
import com.richitec.util.TextUtility;
import com.sun.xml.rpc.processor.modeler.j2ee.xml.resourceEnvRefType;

public class ShareAssistance {
	private static Log log = LogFactory.getLog(ShareAssistance.class);
	
	public static String getQQUserOpenID(String accessToken) {
		String openId = "";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);
		HttpResponseResult response = HttpUtils.getRequest(
				"https://graph.qq.com/oauth2.0/me", params);
		
		log.info("response: " + response.getResponseText());
		
		if (response.getStatusCode() == 200
				&& response.getResponseText() != null
				&& !"".equals(response.getResponseText())) {
			String[] result = TextUtility.splitText(response.getResponseText(), "callback(", ");");
			if (result != null) {
				try {
					JSONObject data = new JSONObject(result[0]);
					openId = data.getString("openid");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		}
		return openId;
	}
	
	public static int shareToQzone(HttpSession session) {
		int retCode = -1;
		String title = formatString((String) session.getAttribute("title"));
		String url = formatString((String) session.getAttribute("url"));
		String summary = formatString((String) session.getAttribute("summary"));
		String images = formatString((String) session.getAttribute("images"));
		String accessToken = formatString((String) session.getAttribute("access_token"));
		String openId = formatString((String) session.getAttribute("openid"));
		
		
//		// test
//		title = "安中通测试";
//		url = "http://www.00244dh.com";
//		summary = "安中通，通天下--";
		
		Configuration cfg = ContextLoader.getConfiguration();
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);
		params.put("oauth_consumer_key", cfg.getQqAppId());
		params.put("openid", openId);
		params.put("title", title);
		params.put("url", url);
		params.put("summary", summary);
		params.put("images", images);
		params.put("comment", "");
		params.put("site", cfg.getQqShareSite());
		params.put("fromurl", cfg.getQqShareFromUrl());
		
		log.info("params: " + params);
		
		HttpResponseResult response = HttpUtils.postRequest("https://graph.qq.com/share/add_share", PostRequestFormat.URLENCODED, params);
		log.info("add_share response: " + response.getResponseText());
		if (response.getStatusCode() == 200
				&& response.getResponseText() != null
				&& !"".equals(response.getResponseText())) {
			try {
				JSONObject data = new JSONObject(response.getResponseText());
				retCode = data.getInt("ret");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return retCode;
	}
	
	public static String formatString(String text){ 
		if(text == null) {
			return ""; 
		}
		return text;
	}
}
