/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package com.alipay.client.trade;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alipay.client.base.ClientConfig;
import com.alipay.client.security.SecurityManagerImpl;
import com.alipay.client.util.ParameterUtil;

/**
 * 
 * 调用支付宝的开放平台创建、支付交易步骤
 * 
 * 1.将业务参数：外部交易号、商品名称、商品总价、卖家帐户、卖家帐户、notify_url这些东西按照xml 的格式放入<req_data></req_data>中
 * 2.将通用参数也放入请求参数中 3.对以上的参数进行签名，签名结果也放入请求参数中
 * 4.请求支付宝开放平台的alipay.wap.trade.create.direct服务
 * 5.从开放平台返回的内容中取出request_token（对返回的内容要先用私钥解密，再用支付宝的公钥验签名）
 * 6.使用拿到的request_token组装alipay.wap.auth.authAndExecute服务的跳转url
 * 7.根据组装出来的url跳转到支付宝的开放平台页面，交易创建和支付在支付宝的页面上完成
 * 
 * 
 * @author feng.chenf
 * @version $Id: Trade.java, v 0.1 2008-11-17 上午09:52:02 feng.chenf Exp $
 */
public class Md5Trade extends HttpServlet {

    private static final long                          serialVersionUID = -3035307235076650766L;

    // 客户端配置
    private ClientConfig                               clientConfig     = new ClientConfig();

    // 安全管理类
    private com.alipay.client.security.SecurityManager securityManager  = new SecurityManagerImpl();

    public void doPost(HttpServletRequest request, HttpServletResponse response)
                                                                                throws ServletException,
                                                                                IOException {
        Map<String, String> reqParams = prepareTradeRequestParamsMap(request);

        String sign = sign(reqParams);

        reqParams.put("sign", sign);
        reqParams.put("sign_type", request.getParameter("sign_type"));

        String url = "";
        try {
            url = getRedirectUrl(reqParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect(url);

    }

    /**
     * 准备要加密的参数
     * 
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> prepareTradeRequestParamsMap(HttpServletRequest request)
                                                                                        throws UnsupportedEncodingException {
        Map<String, String> requestParams = new HashMap<String, String>();
        request.setCharacterEncoding("utf-8");

        //以下是基本参数
        //参数编码字符集
        String inputCharset = request.getParameter("_input_charset");
        requestParams.put("_input_charset", inputCharset);
        //接口名称
        String service = request.getParameter("service");
        requestParams.put("service", service);
        //合作伙伴ID
        String partner = request.getParameter("partner");
        requestParams.put("partner", partner);
        //返回URL
        String returnUrl = request.getParameter("return_url");
        requestParams.put("return_url", returnUrl);
        //通知URL
        String notifyUrl = request.getParameter("notify_url");
        requestParams.put("notify_url", notifyUrl);

        //以下是业务参数
        // 外部交易号
        String outTradeNo = request.getParameter("out_trade_no").trim();
        requestParams.put("out_trade_no", outTradeNo);
        // 商品名称
        String subject = request.getParameter("subject").trim();
        requestParams.put("subject", subject);
        // 商品描述
        String body = request.getParameter("body").trim();
        requestParams.put("body", body);
        // 商品外币总价
        String totalFee = request.getParameter("total_fee").trim();
        requestParams.put("total_fee", totalFee);
        // 商品人民币总价
        String rmbFee = request.getParameter("rmb_fee").trim();
        requestParams.put("rmb_fee", rmbFee);
        // 外币币种
        String currency = request.getParameter("currency").trim();
        requestParams.put("currency", currency);
        // 供货方
        String supplier = request.getParameter("supplier").trim();
        requestParams.put("supplier", supplier);
        // 交易超时规则
        String timeoutRule = request.getParameter("timeout_rule").trim();
        requestParams.put("timeout_rule", timeoutRule);
        // 外部用户ID
        String outUser = request.getParameter("out_user").trim();
        requestParams.put("out_user", outUser);
        // 未完成支付，用户点击链接返回商户
        String merchantUrl = request.getParameter("merchant_url").trim();
        requestParams.put("merchant_url", merchantUrl);

        System.out.println("prepareTradeRequestParamsMap" + requestParams);
        return requestParams;
    }

    /**
     * 对参数进行签名
     * 
     * @param reqParams
     * @return
     */
    private String sign(Map<String, String> reqParams) {

        String signData = ParameterUtil.getSignData(reqParams);
        String sign = "";
        try {
            sign = securityManager.sign(clientConfig.getMd5SignAlgo(), signData, clientConfig
                .getMd5Key());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sign;
    }

    /**
     * 调用alipay.wap.auth.authAndExecute服务的时候需要跳转到支付宝的页面，组装跳转url
     * 
     * @param reqParams
     * @return
     * @throws Exception
     */
    private String getRedirectUrl(Map<String, String> reqParams) throws Exception {
        String redirectUrl = clientConfig.getServerUrl() + ":" + clientConfig.getServerPort()
                             + "/gateway.do?";
        redirectUrl = redirectUrl + ParameterUtil.mapToUrl(reqParams);
        return redirectUrl;
    }

    public static void main(String[] args) throws Exception {
        String signData = "_input_charset=GBK&body=商品描述&currency=USD&merchant_url=http://api.test.alipay.net/atinterface/receive_return.htm&notify_url=http://api.test.alipay.net/atinterface/receive_notify.htm&out_trade_no=2236782506196005&out_user=&partner=2088101010466915&return_url=http://api.test.alipay.net/atinterface/receive_return.htm&rmb_fee=&service=create_forex_trade_wap&subject=名称&supplier=test&timeout_rule=&total_fee=1";
        String key = "pprnyzvyksdxz1xyzpk924sgc2lsghho";
        com.alipay.client.security.SecurityManager securityManager = new SecurityManagerImpl();
        String sign = securityManager.sign("MD5", signData, key);
        System.out.println(sign);
        
        boolean is = securityManager
            .verify("MD5", signData, "6598b5e32c045c2ebdabfd6758831a60", key);
        System.out.println(is);
        
        String encode = URLEncoder.encode("%E5%90%8D%E7%A7%B0", "utf-8");
        System.out.println(encode);
    }
}
