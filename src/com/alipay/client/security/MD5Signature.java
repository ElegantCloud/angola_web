/**
 * 
 */
package com.alipay.client.security;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

import org.apache.commons.codec.digest.DigestUtils;

import com.alipay.client.util.StringUtil;


public class MD5Signature implements Signature{

	public String sign(String content, String key) throws Exception {
		
		String tosign = (content == null ? "" : content) + key;

        try {
            return DigestUtils.md5Hex(getContentBytes(tosign, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SignatureException("MD5签名[content = " + content + "; charset = utf-8"
                                         + "]发生异常!", e);
        }
		
	}

	public boolean verify(String content, String sign, String key)
			throws Exception {
		  String tosign = (content == null ? "" : content) + key;

	        try {
	            String mySign = DigestUtils.md5Hex(getContentBytes(tosign, "utf-8"));

	            return StringUtil.equals(mySign, sign) ? true : false;
	        } catch (UnsupportedEncodingException e) {
	            throw new SignatureException("MD5验证签名[content = " + content + "; charset =utf-8 " 
	                                         + "; signature = " + sign + "]发生异常!", e);
	        }
	}
	
	 /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
    protected byte[] getContentBytes(String content, String charset)
                                                                    throws UnsupportedEncodingException {
        if (StringUtil.isEmpty(charset)) {
            return content.getBytes();
        }

        return content.getBytes(charset);
    }

}
