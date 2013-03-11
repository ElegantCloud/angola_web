/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2006 All Rights Reserved.
 */
package com.alipay.client.security;

/**
 * 安全管理类
 * 
 * @author jun.huyj
 * @version $Id: SecurityManager.java, v 0.1 Nov 11, 2008 10:01:29 AM jun.huyj Exp $
 */
public interface SecurityManager {

    /**
     * 对原始数据进行签名
     * 
     * @param content 原始数据
     * @param algoType 算法类型
     * @param key 私钥
     * @return 签名
     * @throws Exception
     */
    public String sign(String algoType, String content, String key) throws Exception;

    /**
     * 验证签名
     * 
     * @param content 原始数据
     * @param algoType 算法类型
     * @param sign 签名
     * @param key 公钥
     * @return 签名验证通过 False 签名验证失败
     * @throws Exception
     */
    public boolean verify(String algoType, String content, String sign, String key)
                                                                                   throws Exception;

}
