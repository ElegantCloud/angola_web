/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package com.alipay.client.notify;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alipay.client.base.ClientConfig;
import com.alipay.client.security.SecurityManagerImpl;

/**
 * 接收通知并处理
 * 
 * @author feng.chenf
 * @version $Id: NotifyReceiver.java, v 0.1 2009-2-3 下午03:11:58 feng.chenf Exp $
 */
public class Md5NotifyReceiver extends HttpServlet {

    private ClientConfig                               clientConfig     = new ClientConfig();

    private com.alipay.client.security.SecurityManager securityManager  = new SecurityManagerImpl();

    private static final long                          serialVersionUID = 7216412938937049671L;

    @SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    	System.out.println("接收到通知!");
        //获得通知参数
        Map map = request.getParameterMap();
        //获得通知签名
        String sign = (String) ((Object[]) map.get("sign"))[0];
        //获得待验签名的数据
        String verifyData = getVerifyData(map);
        
        boolean verified = false;
        //验签名
        try {
            verified = securityManager.verify(clientConfig.getMd5SignAlgo(), verifyData, sign,
                clientConfig.getMd5Key());
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter out = response.getWriter();
        //验证签名通过
        if (verified) {
        	//TODO 根据交易状态处理业务逻辑
        	//当交易状态成功，处理业务逻辑成功。回写success
        	System.out.println("接收支付宝系统通知验证签名成功！");
            out.print("success");
        } else {
        	System.out.println("接收支付宝系统通知验证签名失败，请检查！");
            out.print("fail");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    /**
     * 获得验签名的数据
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
	private String getVerifyData(Map map) {
        String service = (String) ((Object[]) map.get("service"))[0];
        String v = (String) ((Object[]) map.get("v"))[0];
        String sec_id = (String) ((Object[]) map.get("sec_id"))[0];
        String notify_data = (String) ((Object[]) map.get("notify_data"))[0];
        System.out.println("通知参数为："+"service=" + service + "&v=" + v + "&sec_id=" + sec_id + "&notify_data="+ notify_data);
        return "service=" + service + "&v=" + v + "&sec_id=" + sec_id + "&notify_data="
               + notify_data;
    }
}
