/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2006 All Rights Reserved.
 */
package com.alipay.client.security;

/**
 * 安全管理实现类
 * 
 * @author jun.huyj
 * @version $Id: SecurityManagerImpl.java, v 0.1 Nov 11, 2008 10:03:38 AM
 *          jun.huyj Exp $
 */
public class SecurityManagerImpl implements SecurityManager {

	private Signature signature;

	public String sign(String algoType, String content, String key)
			throws Exception {

		if (algoType.equals("MD5")) {
			//
			signature = new MD5Signature();
		} else {
			throw new Exception("本应用不支持的算法");
		}
		return signature.sign(content, key);
	}

	public boolean verify(String algoType, String content, String sign,
			String key) throws Exception {
		if (algoType.equals("MD5")) {
			//
			signature = new MD5Signature();
		} else {
			throw new Exception("本应用不支持的算法");
		}
		return signature.verify(content, sign, key);
	}
}
