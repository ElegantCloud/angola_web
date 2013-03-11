/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package com.alipay.client.base;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author feng.chenf
 * @version $Id: ClientConfig.java, v 0.1 2008-12-24 上午09:31:39 feng.chenf Exp $
 */
public class ClientConfig {
	public static String merchant_url = "http://www.uu-talk.com/uutalk/alipayWapCharge";
	public static String server_url = "https://mapi.alipay.com/gateway.do?";
	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "UTF-8";
    /**
     * 
     * 
     * 支付宝开放平台服务调用的地址 
     * 
     */
    private String serverUrl   = "";

    /**
     * 支付宝开放平台服务调用的端口  
     */
    private String serverPort  = "";

    /**
     * 商户的MD5安全配置号
     */
    private String md5SecId    = "";

    /**
     * 商户的MD5私钥
     */
    private String md5Key      = "";

    /**
    * 签名的算法 本次示例采用的是MD5
    */
    private String md5SignAlgo = "MD5";

    public ClientConfig() {
        try {

            InputStream iss = this.getClass().getClassLoader().getResourceAsStream(
                "com/alipay/client/config/config.xml");
            DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder dombuilder = domfac.newDocumentBuilder();
            Document doc = dombuilder.parse(iss);

            /********* MD5 *********/
            NodeList md5SecNode = doc.getElementsByTagName("md5SecId");
            this.md5SecId = md5SecNode.item(0).getFirstChild().getNodeValue().trim();

            NodeList md5SignAlgoNode = doc.getElementsByTagName("md5SignAlgo");
            this.md5SignAlgo = md5SignAlgoNode.item(0).getFirstChild().getNodeValue().trim();

            NodeList md5KeyNode = doc.getElementsByTagName("md5Key");
            this.md5Key = md5KeyNode.item(0).getFirstChild().getNodeValue().trim();
            
            NodeList serverUrlNode = doc.getElementsByTagName("serverUrl");
            this.serverUrl = serverUrlNode.item(0).getFirstChild().getNodeValue().trim();
            
            NodeList serverPortNode = doc.getElementsByTagName("serverPort");
            this.serverPort = serverPortNode.item(0).getFirstChild().getNodeValue().trim();

        } catch (Exception e) {
            //异常处理
            //此处为演示代码 直接输出错误信息
            e.printStackTrace();
        }
    }

    /**
     * @return Returns the serverUrl.
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * @param serverUrl The serverUrl to set.
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * @return Returns the serverPort.
     */
    public String getServerPort() {
        return serverPort;
    }

    /**
     * @param serverPort The serverPort to set.
     */
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getMd5SecId() {
        return md5SecId;
    }

    public void setMd5SecId(String md5SecId) {
        this.md5SecId = md5SecId;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }

    public String getMd5SignAlgo() {
        return md5SignAlgo;
    }

    public void setMd5SignAlgo(String md5SignAlgo) {
        this.md5SignAlgo = md5SignAlgo;
    }
}
