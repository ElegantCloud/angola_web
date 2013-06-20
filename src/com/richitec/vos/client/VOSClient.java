package com.richitec.vos.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.angolacall.framework.Configuration;
import com.angolacall.framework.ContextLoader;
import com.richitec.util.CountryManager;
import com.richitec.util.RandomString;

public class VOSClient {

	public static final String P_loginName = "loginName";
	public static final String P_loginPassword = "loginPassword";
	public static final String P_account = "account";
	public static final String P_name = "name";
	public static final String P_type = "type";
	public static final String P_operationType = "operationType";
	public static final String P_validTime = "validTime";
	public static final String P_suiteId = "suiteId";
	public static final String P_availableTime = "availableTime";
	public static final String P_e164 = "e164";
	public static final String P_dynamic = "dynamic";
	public static final String P_protocol = "protocol";
	public static final String P_money = "money";
	public static final String P_pin = "pin";
	public static final String P_callLevel = "callLevel";
	public static final String P_password = "password";
	public static final String P_caller = "caller";
	public static final String P_callee = "callees";
	public static final String P_number = "number";
	public static final String P_callbackBillingNumber = "callbackBillingNumber";
	public static final String P_callbackBillingPassword = "callbackBillingPassword";
	public static final String P_calloutBillingNumber = "calloutBillingNumber";
	public static final String P_calloutBillingPassword = "calloutBillingPassword";
	public static final String P_id = "id";
	public static final String P_agentAccount = "agentAccount";

	private HttpClient httpClient;
	private PoolingClientConnectionManager connManager;

	private String baseURI;
	private String loginName;
	private String loginPassword;

	public VOSClient() {
		connManager = new PoolingClientConnectionManager();
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(100);

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);

		this.httpClient = new DefaultHttpClient(connManager, httpParameters);
	}

	public void setBaseUri(String baseUri) {
		this.baseURI = baseUri;
		if (!this.baseURI.endsWith("/")) {
			this.baseURI += "/";
		}
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	private VOSHttpResponse execute(HttpUriRequest req) {
		VOSHttpResponse response = null;
		try {
			HttpContext context = new BasicHttpContext();
			response = httpClient.execute(req,
					new ResponseHandler<VOSHttpResponse>() {
						@Override
						public VOSHttpResponse handleResponse(HttpResponse arg0)
								throws ClientProtocolException, IOException {
							return new VOSHttpResponse(arg0);
						}
					}, context);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				EntityUtils.consume(response.getHttpResponse().getEntity());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	/**
	 * 在VOS上创建一个账户
	 * 
	 * @param account
	 */
	public VOSHttpResponse addAccount(String account, String agent) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_account, account));
		params.add(new BasicNameValuePair(P_type, "0"));
		params.add(new BasicNameValuePair(P_operationType, "0"));
		// params.add(new BasicNameValuePair(P_validTime,
		// "2015-01-01 00:00:00"));
		if (agent != null) {
			params.add(new BasicNameValuePair(P_agentAccount, agent));
		}
		

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "setcustomer.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	/**
	 * 为账户设置套餐, 立即生效
	 * 
	 * @param account
	 * @param suiteId
	 */
	public VOSHttpResponse addSuiteToAccount(String account, String suiteId) {
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String availableTime = df.format(cal.getTime());
		return subscribeSuite(account, suiteId, availableTime);
	}

	/**
	 * 订购套餐
	 * 
	 * @param account
	 * @param suiteId
	 * @param availableTime
	 * @return
	 */
	public VOSHttpResponse subscribeSuite(String account, String suiteId,
			String availableTime) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_operationType, "0"));
		params.add(new BasicNameValuePair(P_account, account));
		params.add(new BasicNameValuePair(P_suiteId, suiteId));
		params.add(new BasicNameValuePair(P_availableTime, availableTime));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "setsuiteorder.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	public VOSHttpResponse unsubscribeSuite(String orderSuiteId) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_operationType, "2"));
		params.add(new BasicNameValuePair(P_id, orderSuiteId));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "setsuiteorder.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	/**
	 * 为账户添加话机
	 * 
	 * @param account
	 * @param phoneNumber
	 */
	public VOSHttpResponse addPhoneToAccount(String account,
			String phoneNumber, String phonePwd) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_account, account));
		params.add(new BasicNameValuePair(P_e164, phoneNumber));
		params.add(new BasicNameValuePair(P_protocol, "1"));
		params.add(new BasicNameValuePair(P_password, phonePwd));
		params.add(new BasicNameValuePair(P_operationType, "0"));
		params.add(new BasicNameValuePair(P_dynamic, "1"));
		params.add(new BasicNameValuePair(P_callLevel, "5"));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "setphone.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	/**
	 * 向账户充值
	 * 
	 * @param account
	 * @param money
	 * @return
	 */
	public VOSHttpResponse deposite(String account, Double money) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_account, account));
		params.add(new BasicNameValuePair(P_money, money.toString()));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "pay.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	/**
	 * 使用充值卡进行充值
	 * 
	 * @param account
	 * @param pin
	 * @param password
	 * @return
	 */
	public VOSHttpResponse depositeByCard(String account, String pin,
			String password) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		// params.add(new BasicNameValuePair(P_loginName, loginName));
		// params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_name, account));
		params.add(new BasicNameValuePair(P_type, "3"));// 账户类型: 0:话机; 1:网关;
														// 2:绑定号码; 3:账户名称
		params.add(new BasicNameValuePair(P_pin, pin));
		params.add(new BasicNameValuePair(P_password, password));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "paybyphonecard.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	/**
	 * 获取账户信息，包括账户余额，到期时间，透支额度等。
	 * 
	 * @param account
	 * @return
	 */
	public AccountInfo getAccountInfo(String account) {
		return getAccountInfo(account, 0);
	}
	
	public AccountInfo getAccountInfo(String account, Integer type) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_name, account));
		params.add(new BasicNameValuePair(P_type, type.toString()));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "getcustomer.jsp");
		post.setEntity(entity);

		VOSHttpResponse response = execute(post);
		if (response.getHttpStatusCode() == 200
				&& response.isOperationSuccess()) {
			return new AccountInfo(response.getVOSResponseInfo());
		}
		return null;
	}	

	/**
	 * 获取用户当前套餐信息
	 * 
	 * @param account
	 * @return
	 */
	public CurrentSuiteInfo getCurrentSuite(String account) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_account, account));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "getsuitecurrent.jsp");
		post.setEntity(entity);

		VOSHttpResponse response = execute(post);
		if (response.getHttpStatusCode() == 200
				&& response.isOperationSuccess()) {
			return new CurrentSuiteInfo(response.getVOSResponseInfo());
		}
		return null;
	}

	public List<OrderSuiteInfo> getOrderSuites(String account) {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_account, account));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "getsuiteorder.jsp");
		post.setEntity(entity);

		VOSHttpResponse response = execute(post);
		List<OrderSuiteInfo> orderSuites = new LinkedList<OrderSuiteInfo>();
		if (response.getHttpStatusCode() == 200
				&& response.isOperationSuccess()) {
			System.out.println("getOrderSuites response: "
					+ response.getVOSResponseInfo());
			String[] suites = response.getVOSResponseInfo().split("&");
			if (suites != null) {
				for (String suite : suites) {
					OrderSuiteInfo osi = new OrderSuiteInfo(suite);
					System.out.println("order suite: "
							+ osi.toJSONObject().toString());
					orderSuites.add(osi);
				}
			}
		}
		return orderSuites;
	}

	public List<SuiteInfo> getAllSuites() {
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "getsuite.jsp");
		post.setEntity(entity);

		VOSHttpResponse response = execute(post);
		List<SuiteInfo> suiteList = new LinkedList<SuiteInfo>();
		if (response.getHttpStatusCode() == 200
				&& response.isOperationSuccess()) {
			// System.out.println("getAllSuites response: " +
			// response.getVOSResponseInfo());
			String[] suites = response.getVOSResponseInfo().split("&");
			if (suites != null) {
				for (String suite : suites) {
					SuiteInfo suiteInfo = new SuiteInfo(suite);
					System.out.println("suite: "
							+ suiteInfo.toJSONObject().toString());
					suiteList.add(suiteInfo);
				}
			}
		}
		return suiteList;
	}

	public Double getAccountBalance(String account) {
		Double balance = null;
		AccountInfo accountInfo = getAccountInfo(account);
		CurrentSuiteInfo suiteInfo = getCurrentSuite(account);
		if (accountInfo != null && suiteInfo != null) {
			balance = accountInfo.getBalance() + suiteInfo.getGiftBalance();
		}

		return balance;
	}

	public VOSHttpResponse doCallBack(String callee, String caller,
			String callerCountryCode, String vosPhoneNumber,
			String vosPhonePassword) {
		Configuration config = ContextLoader.getConfiguration();

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair(P_loginName, loginName));
		params.add(new BasicNameValuePair(P_loginPassword, loginPassword));
		params.add(new BasicNameValuePair(P_caller, config
				.getCallbackCallerPrefix() + callerCountryCode + caller));
		
		String callerCountryABBR = CountryManager.getInstance().getCountryABBR(callerCountryCode);
		if (callerCountryABBR == null) {
			callerCountryABBR = "";
		}
		
		params.add(new BasicNameValuePair(P_callee, config
				.getCallbackCalleePrefix() + callerCountryABBR + callee));
		params.add(new BasicNameValuePair(P_number, "9999"));
		params.add(new BasicNameValuePair(P_password, "9999"));
		params.add(new BasicNameValuePair(P_callbackBillingNumber,
				vosPhoneNumber));
		params.add(new BasicNameValuePair(P_callbackBillingPassword,
				vosPhonePassword));
		params.add(new BasicNameValuePair(P_calloutBillingNumber,
				vosPhoneNumber));
		params.add(new BasicNameValuePair(P_calloutBillingPassword,
				vosPhonePassword));

		HttpEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
		HttpPost post = new HttpPost(this.baseURI + "callback.jsp");
		post.setEntity(entity);

		return execute(post);
	}

	public static void main(String[] args) {
		VOSClient client = new VOSClient();
		client.setBaseUri("http://vos.00244dh.com/thirdparty/");
		client.setLoginName("admin");
		client.setLoginPassword("uutalk123");
		// VOSHttpResponse resp = client.addAccount("123456");
		// System.out.println(resp.getHttpStatusCode());
		// System.out.println(resp.getVOSStatusCode());
		// System.out.println(resp.getVOSResponseInfo());
		// System.out.println();

		// VOSHttpResponse depositeResp = client.deposite("123456", -100.123);
		// System.out.println(depositeResp.getHttpStatusCode());
		// System.out.println(depositeResp.getVOSStatusCode());
		// System.out.println(depositeResp.getVOSResponseInfo());

		// VOSHttpResponse addPhoneResponse = client.addPhoneToAccount("123456",
		// "10123456", "123132");
		// System.out.println(addPhoneResponse.getHttpStatusCode());
		// System.out.println(addPhoneResponse.getVOSStatusCode());
		// System.out.println(addPhoneResponse.getVOSResponseInfo());
		// System.out.println();
		//
		// VOSHttpResponse addSuiteResponse = client.addSuiteToAccount("123456",
		// "252");
		// System.out.println(addSuiteResponse.getHttpStatusCode());
		// System.out.println(addSuiteResponse.getVOSStatusCode());
		// System.out.println(addSuiteResponse.getVOSResponseInfo());
		// System.out.println();
		//
		// AccountInfo accountInfo = client.getAccountInfo("123456");
		// System.out.println(accountInfo.getAccountID());
		// System.out.println(accountInfo.getAccountName());
		// System.out.println(accountInfo.getExpireTime());
		// System.out.println(accountInfo.getBalance());
		// System.out.println(accountInfo.getOverdraft());
		// System.out.println();
		//
		// CurrentSuiteInfo suiteInfo = client.getCurrentSuite("123456");
		// System.out.println("suiteId : " + suiteInfo.getSuiteId());
		// System.out.println("name : " + suiteInfo.getSuiteName());
		// System.out.println("gift : " + suiteInfo.getGiftBalance());
		// System.out.println("orderId : " + suiteInfo.getOrderId());

		// client.getAllSuites();
		List<OrderSuiteInfo> orderSuites = client
				.getOrderSuites("008613813005146");

	}
}
