package com.angolacall.mvc.controller.model.share;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.richitec.util.HttpUtils;
import com.richitec.util.HttpUtils.HttpResponseResult;
import com.richitec.util.TextUtility;

public class ShareAssistance {
	private static Log log = LogFactory.getLog(ShareAssistance.class);
	
	public static String getQQUserOpenID(String accessToken) {
		String openId = null;
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
}
