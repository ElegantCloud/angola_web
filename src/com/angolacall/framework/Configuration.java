package com.angolacall.framework;

/**
 * Manage the configuration of phone conference server, including IP info
 * 
 * @author sk
 * 
 */
public class Configuration {

	private String suite0Id;
	private String suite5Id;
	private String suite10Id;
	private String appDonwloadPageUrl;
	private String appvcenterUrl;
	private String appId;
	private String serverUrl;
	private String callbackCalleePrefix;
	private String callbackCallerPrefix;
	private String suitePrefix;
	private String yeepayMerchantId;
	private String yeepayKey;
	private String yeepayOnlinePaymentReqURL;
	private String yeepayCommonReqURL;
	private String yeepayQueryRefundReqURL;
	private String yeepayNotifyReturnUrl;
	private String qqAppId;
	private String qqShareSite;
	private String qqShareFromUrl;
	private String aocbRegCountryCode;
	private String aocbDefaultSuiteId;
	
	public String getSuite0Id() {
		return this.suite0Id;
	}

	public void setSuite0Id(String id) {
		this.suite0Id = id;
	}

	public String getSuite5Id() {
		return this.suite5Id;
	}

	public void setSuite5Id(String id) {
		this.suite5Id = id;
	}

	public String getSuite10Id() {
		return this.suite10Id;
	}

	public void setSuite10Id(String id) {
		this.suite10Id = id;
	}

	public String getAppDonwloadPageUrl() {
		return appDonwloadPageUrl;
	}

	public void setAppDonwloadPageUrl(String appDonwloadPageUrl) {
		this.appDonwloadPageUrl = appDonwloadPageUrl;
	}

	public String getAppvcenterUrl() {
		return appvcenterUrl;
	}

	public void setAppvcenterUrl(String appvcenterUrl) {
		this.appvcenterUrl = appvcenterUrl;
	}

	public String getAppDownloadUrl() {
		return this.appvcenterUrl + "/downloadapp";
	}

	public String getAppVersionUrl() {
		return this.appvcenterUrl + "/version";
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getCallbackCalleePrefix() {
		return callbackCalleePrefix;
	}

	public void setCallbackCalleePrefix(String callbackCalleePrefix) {
		this.callbackCalleePrefix = callbackCalleePrefix;
	}

	public String getCallbackCallerPrefix() {
		return callbackCallerPrefix;
	}

	public void setCallbackCallerPrefix(String callbackCallerPrefix) {
		this.callbackCallerPrefix = callbackCallerPrefix;
	}

	public String getSuitePrefix() {
		return suitePrefix;
	}

	public void setSuitePrefix(String suitePrefix) {
		this.suitePrefix = suitePrefix;
	}

	public String getYeepayMerchantId() {
		return yeepayMerchantId;
	}

	public void setYeepayMerchantId(String yeepayMerchantId) {
		this.yeepayMerchantId = yeepayMerchantId;
	}

	public String getYeepayKey() {
		return yeepayKey;
	}

	public void setYeepayKey(String yeepayKey) {
		this.yeepayKey = yeepayKey;
	}

	public String getYeepayOnlinePaymentReqURL() {
		return yeepayOnlinePaymentReqURL;
	}

	public void setYeepayOnlinePaymentReqURL(String yeepayOnlinePaymentReqURL) {
		this.yeepayOnlinePaymentReqURL = yeepayOnlinePaymentReqURL;
	}

	public String getYeepayCommonReqURL() {
		return yeepayCommonReqURL;
	}

	public void setYeepayCommonReqURL(String yeepayCommonReqURL) {
		this.yeepayCommonReqURL = yeepayCommonReqURL;
	}

	public String getYeepayQueryRefundReqURL() {
		return yeepayQueryRefundReqURL;
	}

	public void setYeepayQueryRefundReqURL(String yeepayQueryRefundReqURL) {
		this.yeepayQueryRefundReqURL = yeepayQueryRefundReqURL;
	}

	public String getYeepayNotifyReturnUrl() {
		return yeepayNotifyReturnUrl;
	}

	public void setYeepayNotifyReturnUrl(String yeepayNotifyReturnUrl) {
		this.yeepayNotifyReturnUrl = yeepayNotifyReturnUrl;
	}

	public String getQqAppId() {
		return qqAppId;
	}

	public void setQqAppId(String qqAppId) {
		this.qqAppId = qqAppId;
	}

	public String getQqShareSite() {
		return qqShareSite;
	}

	public void setQqShareSite(String qqShareSite) {
		this.qqShareSite = qqShareSite;
	}

	public String getQqShareFromUrl() {
		return qqShareFromUrl;
	}

	public void setQqShareFromUrl(String qqShareFromUrl) {
		this.qqShareFromUrl = qqShareFromUrl;
	}

	public String getAocbRegCountryCode() {
		return aocbRegCountryCode;
	}

	public void setAocbRegCountryCode(String aocbRegCountryCode) {
		this.aocbRegCountryCode = aocbRegCountryCode;
	}

	public String getAocbDefaultSuiteId() {
		return aocbDefaultSuiteId;
	}

	public void setAocbDefaultSuiteId(String aocbDefaultSuiteId) {
		this.aocbDefaultSuiteId = aocbDefaultSuiteId;
	}

}
