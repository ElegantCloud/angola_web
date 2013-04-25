package com.angolacall.mvc.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alipay.client.base.PartnerConfig;
import com.alipay.client.security.RSASignature;
import com.angolacall.constants.ChargeStatus;
import com.angolacall.constants.ChargeType;
import com.angolacall.constants.UUTalkConfigKeys;
import com.angolacall.framework.Configuration;
import com.angolacall.framework.ContextLoader;
import com.angolacall.mvc.admin.model.ChargeMoneyConfigDao;
import com.angolacall.mvc.admin.model.UUTalkConfigManager;
import com.angolacall.mvc.model.charge.ChargeUtil;
import com.angolacall.web.user.UserBean;
import com.richitec.ucenter.model.UserDAO;
import com.richitec.util.CryptoUtil;
import com.richitec.util.MailSender;
import com.richitec.util.MyRC4;
import com.richitec.vos.client.OrderSuiteInfo;
import com.richitec.vos.client.SuiteInfo;
import com.richitec.vos.client.VOSClient;
import com.richitec.vos.client.VOSHttpResponse;
import com.yeepay.DigestUtil;

@Controller
@RequestMapping("/profile")
public class ProfileApiController {
	private static Log log = LogFactory.getLog(ProfileController.class);
	private UserDAO userDao;

	@PostConstruct
	public void init() {
		userDao = ContextLoader.getUserDAO();
	}

	@RequestMapping(value = "/changePwd")
	public void changePwdAPI(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName,
			@RequestParam(value = "oldPwd") String oldPwd,
			@RequestParam(value = "newPwd") String newPwd,
			@RequestParam(value = "newPwdConfirm") String newPwdConfirm)
			throws IOException, JSONException {
		log.info("countryCode: " + countryCode + " username: " + userName
				+ " oldPwd: " + oldPwd + " newpwd: " + newPwd + " confirmpwd: "
				+ newPwdConfirm);
		Map<String, Object> user = userDao.getUser(countryCode, userName);
		String pwd = (String) user.get("password");

		if (!oldPwd.equals(pwd)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (newPwd.isEmpty() || !newPwd.equals(newPwdConfirm)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String md5Password = CryptoUtil.md5(newPwd);
		if (userDao.changePassword(userName, md5Password, countryCode) <= 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		Map<String, Object> userMap = userDao.getUser(countryCode, userName);
		String userkey = (String) userMap.get("userkey");
		JSONObject ret = new JSONObject();
		ret.put("userkey", userkey);
		response.getWriter().print(ret.toString());
	}

	// === following code is used for alipay client request
	@RequestMapping("/alipayinfo")
	public void alipayInfo(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		JSONObject info = new JSONObject();
		info.put("partner_id", PartnerConfig.PARTNER);
		info.put("seller", PartnerConfig.SELLER);

		Map<String, Object> user = ContextLoader.getUserDAO().getUser(
				countryCode, userName);
		String userkey = (String) user.get("userkey");
		String cryptMsg = MyRC4.encryptPro(info.toString(), userkey);
		response.getWriter().print(cryptMsg);
	}

	@RequestMapping("/alipaysign")
	public void alipayClientParamSign(HttpServletResponse response,
			@RequestParam(value = "charge_money_id") String chargeMoneyId,
			@RequestParam String content, @RequestParam String out_trade_no,
			@RequestParam String total_fee,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws IOException {
		log.info("content: " + content);
		ContextLoader.getChargeDAO().addChargeRecord(out_trade_no, countryCode,
				userName, Double.valueOf(total_fee), ChargeStatus.processing,
				chargeMoneyId);
		response.getWriter().print(
				RSASignature.sign(content, PartnerConfig.RSA_PRIVATE));
	}

	@RequestMapping("/yeepaySign")
	public void yeepaySign(HttpServletResponse response,
			@RequestParam(value = "charge_money_id") String chargeMoneyId,
			@RequestParam String content,
			@RequestParam(value = "order_num") String orderNum,
			@RequestParam(value = "money") String money,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		log.info("yeepaySign - content: " + content);
		ContextLoader.getChargeDAO().addChargeRecord(orderNum, countryCode,
				userName, Double.valueOf(money), ChargeStatus.processing,
				chargeMoneyId);
		JSONObject ret = new JSONObject();
		ret.put("sign", DigestUtil.hmacSign(content, ContextLoader
				.getConfiguration().getYeepayKey()));
		response.getWriter().print(ret.toString());
	}

	@RequestMapping("/getRegInviteLink")
	public void getRegInviteLink(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws IOException {
		String inviterId = ContextLoader.getRegLinkTagDao().getRegLinkTag(
				countryCode, userName);
		if (inviterId == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		String inviteRegUrl = ContextLoader.getConfiguration().getServerUrl()
				+ "/invitejoin/" + inviterId;
		response.getWriter().print(inviteRegUrl);
	}

	@RequestMapping("/getBindPhone")
	public void getBindPhone(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		Map<String, Object> user = userDao.getUser(countryCode, userName);
		String bindPhone = (String) user.get("bindphone");
		String bindPhoneCountryCode = (String) user
				.get("bindphone_country_code");
		JSONObject ret = new JSONObject();
		ret.put("bindphone", bindPhone);
		ret.put("bindphone_country_code", bindPhoneCountryCode);
		response.getWriter().print(ret.toString());
	}

	@RequestMapping("/setBindPhone")
	public void setBindPhone(
			HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName,
			@RequestParam(value = "bindphone_country_code") String bindPhoneCountryCode,
			@RequestParam(value = "bindphone") String bindPhone) {
		userDao.setBindPhone(countryCode, userName, bindPhoneCountryCode,
				bindPhone);
	}

	@RequestMapping("/regInviteDesc")
	public void getRegInviteDescription(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		UUTalkConfigManager ucm = ContextLoader.getUUTalkConfigManager();
		JSONObject ret = new JSONObject();
		StringBuffer info = new StringBuffer();
		String regGiftDesc = ucm.getRegGiftDescription();
		if (!regGiftDesc.equals("")) {
			info.append(regGiftDesc);
			info.append('\n');
		}
		info.append(ucm.getInviteChargeGiftDescription());
		ret.put(UUTalkConfigKeys.reg_gift_desc_text.name(), info.toString());
		response.getWriter().print(ret.toString());
	}

	@RequestMapping("/getChargeMoneyList")
	public void getChargeMoneyList(HttpServletResponse response)
			throws IOException {
		ChargeMoneyConfigDao cmcd = ContextLoader.getChargeMoneyConfigDao();
		List<Map<String, Object>> list = cmcd.getChargeMoneyList();
		JSONArray ret = new JSONArray();

		if (list != null) {
			for (Map<String, Object> item : list) {
				Integer id = (Integer) item.get("id");
				Float chargeMoney = (Float) item.get("charge_money");
				Float giftMoney = (Float) item.get("gift_money");
				String description = (String) item.get("description");
				try {
					JSONObject record = new JSONObject();
					record.put("id", id.intValue());
					record.put("charge_money", chargeMoney.floatValue());
					record.put("gift_money", giftMoney.floatValue());
					record.put("description", description);
					ret.put(record);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}

		response.getWriter().print(ret.toString());
	}

	/**
	 * get the ordered suites and the available suites to order
	 * 
	 * @param response
	 * @param countryCode
	 * @param userName
	 * @throws JSONException
	 * @throws IOException
	 */
	@RequestMapping("/getSuites")
	public void getSuites(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		log.info("getSuites");
		VOSClient vosClient = ContextLoader.getVOSClient();
		List<OrderSuiteInfo> orderSuites = vosClient.getOrderSuites(userDao
				.genVosAccountName(countryCode, userName));
		List<SuiteInfo> allSuites = vosClient.getAllSuites();

		Configuration config = ContextLoader.getConfiguration();
		String suitePrefix = config.getSuitePrefix();

		JSONObject ret = new JSONObject();
		if (orderSuites != null) {
			JSONArray orderSuiteArray = new JSONArray();
			for (OrderSuiteInfo osi : orderSuites) {
				// if ("suite0".equals(osi.getSuiteName())) {
				// continue;
				// }
				// orderSuiteArray.put(osi.toJSONObject());
				if (osi.getSuiteName().startsWith(suitePrefix)) {
					orderSuiteArray.put(osi.toJSONObject());
				}
			}
			ret.put("my_suites", orderSuiteArray);
		}

		if (allSuites != null) {
			JSONArray allSuitesArray = new JSONArray();
			for (SuiteInfo si : allSuites) {
				// if ("suite0".equals(si.getSuiteName())) {
				// continue;
				// }
				//
				// // boolean isOrdered = false;
				// // for (OrderSuiteInfo osi : orderSuites) {
				// // if (si.getSuiteId().equals(osi.getSuiteId())) {
				// // isOrdered = true;
				// // break;
				// // }
				// // }
				// //
				// // if (!isOrdered) {
				// allSuitesArray.put(si.toJSONObject());
				// // }
				if (si.getSuiteName().startsWith(suitePrefix)) {
					allSuitesArray.put(si.toJSONObject());
				}

			}
			ret.put("all_suites", allSuitesArray);
		}

		response.getWriter().print(ret.toString());
	}

	/**
	 * subscribe suite
	 * 
	 * @param response
	 * @param countryCode
	 * @param userName
	 * @param suiteId
	 * @param openTimeType
	 *            - "at_once" open the suite at once, "next_month" open the
	 *            suite next month
	 * @throws JSONException
	 * @throws IOException
	 */
	@RequestMapping("/subscribeSuite")
	public void subscribeSuite(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName,
			@RequestParam(value = "suiteId") String suiteId,
			@RequestParam(value = "open_time_type") String openTimeType)
			throws JSONException, IOException {

		String availableTime = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		if (openTimeType.equals("at_once")) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		} else {
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		availableTime = df.format(cal.getTime());
		log.info("available time: " + availableTime);
		VOSHttpResponse vosResponse = ContextLoader.getVOSClient()
				.subscribeSuite(
						userDao.genVosAccountName(countryCode, userName),
						suiteId, availableTime);
		if (vosResponse.getHttpStatusCode() != 200
				|| !vosResponse.isOperationSuccess()) {
			log.error("\nCannot do callback for user : " + countryCode
					+ userName + "\nVOS Http Response : "
					+ vosResponse.getHttpStatusCode() + "\nVOS Status Code : "
					+ vosResponse.getVOSStatusCode() + "\nVOS Response Info ："
					+ vosResponse.getVOSResponseInfo());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JSONObject ret = new JSONObject();
			ret.put("vos_status_code", vosResponse.getVOSStatusCode());
			ret.put("vos_info", vosResponse.getVOSResponseInfo());
			response.getWriter().print(ret.toString());
			return;
		}

	}

	@RequestMapping("/unsubscribeSuite")
	public void unsubscribeSuite(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName,
			@RequestParam(value = "orderSuiteId") String orderSuiteId)
			throws JSONException, IOException {
		VOSHttpResponse vosResponse = ContextLoader.getVOSClient()
				.unsubscribeSuite(orderSuiteId);
		if (vosResponse.getHttpStatusCode() != 200
				|| !vosResponse.isOperationSuccess()) {
			log.error("\nCannot do callback for user : " + countryCode
					+ userName + "\nVOS Http Response : "
					+ vosResponse.getHttpStatusCode() + "\nVOS Status Code : "
					+ vosResponse.getVOSStatusCode() + "\nVOS Response Info ："
					+ vosResponse.getVOSResponseInfo());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JSONObject ret = new JSONObject();
			ret.put("vos_status_code", vosResponse.getVOSStatusCode());
			ret.put("vos_info", vosResponse.getVOSResponseInfo());
			response.getWriter().print(ret.toString());
			return;
		}
	}

	@RequestMapping("/getRegedUserCountViaShare")
	public void getRegedUserCountViaShare(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		JSONObject ret = new JSONObject();
		ret.put("shared_user_count",
				userDao.getRegedUserCountViaShare(countryCode, userName));
		response.getWriter().print(ret.toString());
	}

	@RequestMapping("/setEmail")
	public void setEmail(HttpServletResponse resposne,
			@RequestParam String email,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		JSONObject ret = new JSONObject();
		if (userDao.isEmailBinded(email)) {
			ret.put("result", "email is already binded by others");
			resposne.getWriter().print(ret.toString());
			return;
		}

		int rows = userDao.setEmail(countryCode, userName, email);
		if (rows > 0) {
			// email set ok
			// send activation email to user
			Map<String, Object> user = userDao.getUser(countryCode, userName);
			Float frozenMoney = (Float) user.get("frozen_money");
			if (frozenMoney > 0) {
				// send money gain mail
				try {
					sendMoneyGainEmail(user);
					ret.put("result", "money gain mail send ok");
				} catch (Exception e) {
					e.printStackTrace();
					ret.put("result", "money gain mail send failed");
				}
				resposne.getWriter().print(ret.toString());
				return;
			} else {
				// send email address verification mail
				try {
					sendEmailAddressVerifyMail(user);
					ret.put("result", "address verify mail send ok");
				} catch (Exception e) {
					e.printStackTrace();
					ret.put("result", "address verify mail send failed");
				}
				resposne.getWriter().print(ret.toString());
				return;
			}
		} else {
			ret.put("result", "email set error");
			resposne.getWriter().print(ret.toString());
		}
	}

	private void sendEmailAddressVerifyMail(Map<String, Object> user)
			throws AddressException, MessagingException {
		Configuration config = ContextLoader.getConfiguration();

		String countryCode = (String) user.get("countrycode");
		String userName = (String) user.get("username");
		String email = (String) user.get("email");
		String randomId = (String) user.get("random_id");
		String title = "悠聊邮箱绑定验证";

		String url = config.getServerUrl() + "/verifyEmailAddress/" + randomId;

		String content = "<h3>亲爱的用户" + countryCode + userName
				+ "，<br/>欢迎您使用悠聊网络电话。</h3>"
				+ "<p><h4>点击验证邮箱按钮，完成邮箱绑定验证。</h4><br/>" + "<a href=\"" + url
				+ "\"><button type=\"button\">验证邮箱</button></a><br/><br/>"
				+ "如果不能点击，请复制以下链接到浏览器。<br/>" + url + "</p>";
		MailSender mailSender = ContextLoader.getMailSender();
		mailSender.sendMail(email, title, content);
	}

	private void sendMoneyGainEmail(Map<String, Object> user)
			throws AddressException, MessagingException {
		Configuration config = ContextLoader.getConfiguration();

		String countryCode = (String) user.get("countrycode");
		String userName = (String) user.get("username");
		Float frozenMoney = (Float) user.get("frozen_money");
		String email = (String) user.get("email");
		String randomId = (String) user.get("random_id");
		String title = "悠聊喊你领话费啦";
		String url = config.getServerUrl() + "/getFrozenMoneyViaEmailLink/"
				+ randomId;

		String content = "<h3>亲爱的用户" + countryCode + userName
				+ "，<br/>欢迎您使用悠聊网络电话。</h3>"
				+ "<p><h4>现在点击领取话费，即可获得<font color=\"red\">" + frozenMoney
				+ "元</font>话费！</h4><br/>" + "<a href=\"" + url
				+ "\"><button type=\"button\">领取话费</button></a><br/><br/>"
				+ "如果不能点击，请复制以下链接到浏览器。<br/>" + url + "</p>";
		MailSender mailSender = ContextLoader.getMailSender();
		mailSender.sendMail(email, title, content);
	}

	@RequestMapping("/getAccountInfo")
	public void getAccountInfo(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws IOException {
		UserBean user = userDao.getUserBean(countryCode, userName);
		response.getWriter().print(user.toJSONObject().toString());
	}

	@RequestMapping("/sendActivateFrozenMoneyMail")
	public void sendActivateFrozenMoneyMail(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws IOException {
		Map<String, Object> user = userDao.getUser(countryCode, userName);
		try {
			sendMoneyGainEmail(user);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return;
		}
	}

	@RequestMapping("/adclick")
	public void chargeWhenAdClicked(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName) throws JSONException, IOException {
		Double money = Double.parseDouble(ContextLoader.getUUTalkConfigManager().getAdClickGiftMoney());
		boolean result = ChargeUtil.chargeUser(ChargeType.adclick, countryCode, userName, money);
		JSONObject ret = new JSONObject();
		ret.put("result", result);
		ret.put("money", money.doubleValue());
		response.getWriter().print(ret.toString());
	}
}
