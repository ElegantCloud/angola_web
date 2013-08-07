package com.angolacall.mvc.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.angolacall.constants.ChargeType;
import com.angolacall.constants.EmailStatus;
import com.angolacall.constants.UserAccountStatus;
import com.angolacall.constants.UserConstants;
import com.angolacall.framework.Configuration;
import com.angolacall.framework.ContextLoader;
import com.angolacall.mvc.admin.model.UUTalkConfigManager;
import com.angolacall.mvc.model.charge.ChargeUtil;
import com.angolacall.web.user.UserBean;
import com.richitec.sms.client.SMSClient;
import com.richitec.ucenter.model.UserDAO;
import com.richitec.util.CryptoUtil;
import com.richitec.util.MailSender;
import com.richitec.util.RandomString;
import com.richitec.vos.client.VOSClient;
import com.richitec.vos.client.VOSHttpResponse;

@Controller
@RequestMapping("/user")
public class UserController extends ExceptionController {

	private static Log log = LogFactory.getLog(UserController.class);

	private UserDAO userDao;
	private VOSClient vosClient;
	private Configuration config;
	private SMSClient smsClient;

	public static final String ErrorCode = "error_code";
	public static final String PhoneNumberError = "phone_number_error";
	public static final String PhoneCodeError = "phone_code_error";
	public static final String PasswordError = "password_error";
	public static final String ConfirmPasswordError = "confirm_password_error";

	@PostConstruct
	public void init() {
		userDao = ContextLoader.getUserDAO();
		vosClient = ContextLoader.getVOSClient();
		config = ContextLoader.getConfiguration();
		smsClient = ContextLoader.getSMSClient();
	}

	@RequestMapping("/login")
	public void login(
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "loginName") String loginName,
			@RequestParam(value = "loginPwd") String loginPwd,
			@RequestParam(value = "brand", required = false, defaultValue = "") String brand,
			@RequestParam(value = "model", required = false, defaultValue = "") String model,
			@RequestParam(value = "release", required = false, defaultValue = "") String release,
			@RequestParam(value = "sdk", required = false, defaultValue = "") String sdk,
			@RequestParam(value = "width", required = false, defaultValue = "0") String width,
			@RequestParam(value = "height", required = false, defaultValue = "0") String height,
			HttpServletResponse response, HttpSession session) throws Exception {
		JSONObject json = new JSONObject();
		try {
			UserBean user = userDao.getUserBean(countryCode, loginName,
					loginPwd);
			if (null != user) {
				json.put("result", "0");
				json.put("userkey", user.getUserKey());
				json.put("vosphone", user.getVosPhone());
				json.put("vosphone_pwd", user.getVosPhonePwd());
				json.put("bindphone", user.getBindPhone());
				json.put("bindphone_country_code",
						user.getBindPhoneCountryCode());
				json.put("status", user.getStatus());
				json.put("email", user.getEmail());
				json.put("email_status", user.getEmailStatus());
				json.put("reg_given_money", user.getFrozenMoney());
				user.setUserName(loginName);
				user.setPassword(loginPwd);
				session.setAttribute(UserBean.SESSION_BEAN, user);
				userDao.recordDeviceInfo(loginName, countryCode, brand, model,
						release, sdk, width, height);
			} else {
				json.put("result", "1");
			}
		} catch (DataAccessException e) {
			json.put("result", "1");
		}
		response.getWriter().print(json.toString());
	}

	/**
	 * 此接口用于 forgetpwd.jsp 页面获取手机验证码请求。
	 * 
	 * @param session
	 * @param phoneNumber
	 * @return
	 */
	@RequestMapping("/validatePhoneNumber")
	public @ResponseBody
	String validatePhoneNumber(HttpSession session,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "phone") String phoneNumber) {
		String result = userDao.checkRegisterPhone(countryCode, phoneNumber);
		if ("3".equals(result)) {
			userDao.getPhoneCode(session, phoneNumber, countryCode);
			return "200";
		} else {
			return "404";
		}
	}

	/**
	 * 用户忘记密码后重新设置密码
	 * 
	 * @param session
	 * @param phoneNumber
	 * @param phoneCode
	 * @param newPassword
	 * @param newPasswordConfirm
	 * @return
	 */
	@RequestMapping("/resetPassword")
	public @ResponseBody
	String resetPassword(HttpSession session,
			@RequestParam(value = "phone") String phoneNumber,
			@RequestParam(value = "code") String phoneCode,
			@RequestParam(value = "newPwd") String newPassword,
			@RequestParam(value = "newPwdConfirm") String newPasswordConfirm) {
		if (phoneNumber.isEmpty() || phoneCode.isEmpty()
				|| newPassword.isEmpty() || newPasswordConfirm.isEmpty()) {
			return "400";
		}

		String sessionPhoneNumber = (String) session
				.getAttribute("phonenumber");
		String countryCode = (String) session.getAttribute("countrycode");
		String sessionPhoneCode = (String) session.getAttribute("phonecode");
		if (null == sessionPhoneCode || null == sessionPhoneNumber
				|| null == countryCode) {
			return "410";
		}

		if (!phoneNumber.equals(sessionPhoneNumber)
				|| !phoneCode.equals(sessionPhoneCode)) {
			return "401";
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			return "403";
		}

		String md5Password = CryptoUtil.md5(newPassword);
		if (userDao.changePassword(phoneNumber, md5Password, countryCode) <= 0) {
			return "500";
		}

		session.removeAttribute("phonenumber");
		session.removeAttribute("phonecode");
		session.removeAttribute("countrycode");
		return "200";
	}

	@RequestMapping(value = "/resetpwdvialink/{random_id}")
	public ModelAndView resetPwdViaLink(
			@PathVariable(value = "random_id") String randomId) {
		ModelAndView view = new ModelAndView("reset_pwd");
		UserDAO userDao = ContextLoader.getUserDAO();
		Map<String, Object> user = null;
		try {
			user = userDao.getUserByRandomId(randomId);
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		if (user == null) {
			return view;
		}

		view.addObject("user", user);
		view.addObject("random_id", randomId);

		return view;
	}

	@RequestMapping(value = "/resetPwd")
	public @ResponseBody
	String resetPwd(HttpServletResponse response,
			@RequestParam(value = "random_id") String randomId,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "password1") String password1)
			throws SQLException {
		UserDAO userDao = ContextLoader.getUserDAO();
		Map<String, Object> user = null;
		try {
			user = userDao.getUserByRandomId(randomId);
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		if (user == null) {
			return "user_not_found";
		}

		if (!password.equals(password1)) {
			return "passwords_not_equal";
		}

		String countryCode = (String) user
				.get(UserConstants.countrycode.name());
		String userName = (String) user.get(UserConstants.username.name());

		String md5Password = CryptoUtil.md5(password);
		if (userDao.changePassword(userName, md5Password, countryCode) <= 0) {
			return "password_reset_failed";
		}

		userDao.updateRandomId(countryCode, userName,
				RandomString.getRandomId(countryCode + userName));

		return "password_reset_ok";
	}

	@RequestMapping(value = "/websignup", method = RequestMethod.POST)
	public ModelAndView webSignup(
			HttpSession session,
			@RequestParam(value = "countryCode", defaultValue = "") String countryCode,
			@RequestParam(value = "phoneNumber") String phoneNumber,
			@RequestParam(value = "phoneCode") String phoneCode,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "confirmPassword") String confirmPassword)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("signup");

		String sessionPhoneNumber = (String) session
				.getAttribute("phonenumber");
		String sessionCountryCode = (String) session
				.getAttribute("countrycode");
		String sessionPhoneCode = (String) session.getAttribute("phonecode");

		if (phoneNumber.isEmpty() || phoneCode.isEmpty() || password.isEmpty()
				|| confirmPassword.isEmpty()) {
			mv.addObject(ErrorCode, HttpServletResponse.SC_BAD_REQUEST);
			if (phoneNumber.isEmpty()) {
				mv.addObject(PhoneNumberError,
						HttpServletResponse.SC_BAD_REQUEST);
			}
			if (phoneCode.isEmpty()) {
				mv.addObject(PhoneCodeError, HttpServletResponse.SC_BAD_REQUEST);
			}
			if (password.isEmpty()) {
				mv.addObject(PasswordError, HttpServletResponse.SC_BAD_REQUEST);
			}
			if (confirmPassword.isEmpty()) {
				mv.addObject(ConfirmPasswordError,
						HttpServletResponse.SC_BAD_REQUEST);
			}
			return mv;
		}

		if (!phoneNumber.equals(sessionPhoneNumber)
				|| !phoneCode.equals(sessionPhoneCode)
				|| !countryCode.equals(sessionCountryCode)) {
			mv.addObject(ErrorCode, HttpServletResponse.SC_UNAUTHORIZED);
			mv.addObject(PhoneCodeError, HttpServletResponse.SC_UNAUTHORIZED);
			return mv;
		}

		if (!password.equals(confirmPassword)) {
			mv.addObject(ErrorCode, HttpServletResponse.SC_FORBIDDEN);
			mv.addObject(ConfirmPasswordError, HttpServletResponse.SC_FORBIDDEN);
			return mv;
		}

		String result = userDao.regUser(countryCode, phoneNumber, "", "",
				password, confirmPassword);

		if ("0".equals(result)) { // insert success
			result = finishVosRegister(countryCode, phoneNumber);
		}

		if ("0".equals(result)) {
			mv.addObject(ErrorCode, HttpServletResponse.SC_OK);
		} else {
			mv.addObject(ErrorCode,
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return mv;
	}

	@RequestMapping("/regViaInvite")
	public void regViaInvite(
			HttpServletResponse response,
			HttpSession session,
			@RequestParam(value = "referrer") String referrer,
			@RequestParam(value = "referrerCountryCode") String referrerCountryCode,
			@RequestParam(value = "countryCode", defaultValue = "") String countryCode,
			@RequestParam(value = "phoneNumber") String phoneNumber,
			@RequestParam(value = "phoneCode") String phoneCode,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "confirmPassword") String confirmPassword)
			throws IOException {
		String sessionPhoneNumber = (String) session
				.getAttribute("phonenumber");
		String sessionCountryCode = (String) session
				.getAttribute("countrycode");
		String sessionPhoneCode = (String) session.getAttribute("phonecode");

		if (phoneNumber.isEmpty() || phoneCode.isEmpty() || password.isEmpty()
				|| confirmPassword.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (!phoneNumber.equals(sessionPhoneNumber)
				|| !phoneCode.equals(sessionPhoneCode)
				|| !countryCode.equals(sessionCountryCode)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (!password.equals(confirmPassword)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		String result = userDao.regUser(countryCode, phoneNumber,
				referrerCountryCode, referrer, password, confirmPassword);

		if ("0".equals(result)) { // insert success
			result = finishVosRegister(countryCode, phoneNumber);

			if (result.equals("0")) {
				// give money to referrer
				if (!referrer.equals("") && !referrerCountryCode.equals("")) {
					UUTalkConfigManager ucm = ContextLoader
							.getUUTalkConfigManager();
					double regGiftMoney = Double.parseDouble(ucm
							.getRegGiftValue());
					if (regGiftMoney > 0) {
						ChargeUtil.giveMoneyToReferrer(ChargeType.invitereg,
								referrerCountryCode, referrer, regGiftMoney,
								countryCode, phoneNumber);
					}
				}
			}
		}

		if (!"0".equals(result)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

	}

	@RequestMapping("/regViaInviteDirectReg")
	public void regViaInviteDirectReg(
			HttpServletResponse response,
			HttpSession session,
			@RequestParam(value = "referrer") String referrer,
			@RequestParam(value = "referrerCountryCode") String referrerCountryCode,
			@RequestParam(value = "countryCode", defaultValue = "") String countryCode,
			@RequestParam(value = "phoneNumber") String phoneNumber,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "confirmPassword") String confirmPassword)
			throws IOException, JSONException {

		String result = userDao.regUser(countryCode, phoneNumber,
				referrerCountryCode, referrer, password, confirmPassword);

		if ("0".equals(result)) { // insert success
			result = finishVosRegister(countryCode, phoneNumber);

			if (result.equals("0")) {
				// give money to referrer
				if (!referrer.equals("") && !referrerCountryCode.equals("")) {
					UUTalkConfigManager ucm = ContextLoader
							.getUUTalkConfigManager();
					double regGiftMoney = Double.parseDouble(ucm
							.getRegGiftValue());
					if (regGiftMoney > 0) {
						ChargeUtil.giveMoneyToReferrer(ChargeType.invitereg,
								referrerCountryCode, referrer, regGiftMoney,
								countryCode, phoneNumber);
					}
				}
			}
		}

		JSONObject ret = new JSONObject();
		if ("0".equals(result)) {
			ret.put("result", "ok");
			response.getWriter().print(ret.toString());
		} else if ("1".equals(result)) {
			ret.put("result", "empty_phone");
			response.getWriter().print(ret.toString());
		} else if ("2".equals(result)) {
			ret.put("result", "invalid_phone");
			response.getWriter().print(ret.toString());
		} else if ("3".equals(result)) {
			ret.put("result", "existed");
			response.getWriter().print(ret.toString());
		} else if ("4".equals(result)) {
			ret.put("result", "empty_password");
			response.getWriter().print(ret.toString());
		} else if ("5".equals(result)) {
			ret.put("result", "password_different_to_confirm_password");
			response.getWriter().print(ret.toString());
		} else if ("7".equals(result)) {
			ret.put("result", "phone_cant_start_with_country_code");
			response.getWriter().print(ret.toString());
		} else {
			ret.put("result", "server_error");
			response.getWriter().print(ret.toString());
		}

	}

	/**
	 * 用户从手机注册获取验证码请求。
	 * 
	 * @param phone
	 * @param response
	 * @param session
	 * @throws Exception
	 */
	@RequestMapping("/getPhoneCode")
	public void getPhoneCode(
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "phone") String phone,
			HttpServletResponse response, HttpSession session) throws Exception {
		JSONObject jsonUser = new JSONObject();
		try {
			String result = "0";
			result = userDao.checkRegisterPhone(countryCode, phone);
			if (result.equals("0")) {
				result = userDao.getPhoneCode(session, phone, countryCode);
			}
			jsonUser.put("result", result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		response.getWriter().print(jsonUser.toString());
	}

	/**
	 * 验证手机客户端发送来的验证码
	 * 
	 * @param code
	 * @param response
	 * @param session
	 * @throws Exception
	 */
	@RequestMapping("/checkPhoneCode")
	public void checkPhoneCode(@RequestParam(value = "code") String code,
			HttpServletResponse response, HttpSession session) throws Exception {
		JSONObject jsonUser = new JSONObject();
		try {
			String result = "0";
			if (session.getAttribute("phonecode") != null) {
				result = userDao.checkPhoneCode(session, code);
			} else {
				result = "6"; // session timeout
			}
			jsonUser.put("result", result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		response.getWriter().print(jsonUser.toString());
	}

	@RequestMapping("/regUser")
	public void regUser(@RequestParam(value = "password") String password,
			@RequestParam(value = "password1") String password1,
			HttpServletResponse response, HttpSession session) throws Exception {
		log.info("regUser");

		String result = "";
		String phone = "";
		String countryCode = "";
		if (null == session.getAttribute("phonenumber")) {
			result = "6"; // session过期
		} else {
			phone = (String) session.getAttribute("phonenumber");
			countryCode = (String) session.getAttribute("countrycode");
			result = userDao.regUser(countryCode, phone, "", "", password,
					password1);
		}

		if ("0".equals(result)) { // insert success
			result = finishVosRegister(countryCode, phone);
		}

		JSONObject jsonUser = new JSONObject();
		try {
			jsonUser.put("result", result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		response.getWriter().print(jsonUser.toString());
	}

	private String addUserToVOS(String fullUserName, String recognizeCode, String vosPhoneNumber,
			String vosPhonePwd) {
		// create new account in VOS
		VOSHttpResponse addAccountResp = vosClient.addAccount(fullUserName);
		if (addAccountResp.getHttpStatusCode() != 200
				|| !addAccountResp.isOperationSuccess()) {
			log.error("\nCannot create VOS accont for user : " + fullUserName
					+ "\nVOS Http Response : "
					+ addAccountResp.getHttpStatusCode()
					+ "\nVOS Status Code : "
					+ addAccountResp.getVOSStatusCode()
					+ "\nVOS Response Info ："
					+ addAccountResp.getVOSResponseInfo());
			return "2001";
		}

		// create new phone in VOS
		VOSHttpResponse addPhoneResp = vosClient.addPhoneToAccount(
				fullUserName, vosPhoneNumber, vosPhonePwd);
		if (addPhoneResp.getHttpStatusCode() != 200
				|| !addPhoneResp.isOperationSuccess()) {
			log.error("\nCannot create VOS phone <" + vosPhoneNumber
					+ "> for user : " + fullUserName + "\nVOS Http Response : "
					+ addPhoneResp.getHttpStatusCode() + "\nVOS Status Code : "
					+ addPhoneResp.getVOSStatusCode() + "\nVOS Response Info ："
					+ addPhoneResp.getVOSResponseInfo());
			return "2002";
		}

		// add suite to account
		String suiteId = config.getSuite0Id();
		if (recognizeCode != null && recognizeCode.equals(config.getAocbRegCountryCode())) {
			suiteId = config.getAocbDefaultSuiteId();
		}
		
		VOSHttpResponse addSuiteResp = vosClient.addSuiteToAccount(
				fullUserName, suiteId);
		if (addSuiteResp.getHttpStatusCode() != 200
				|| !addSuiteResp.isOperationSuccess()) {
			log.error("\nCannot add VOS suite <" + suiteId
					+ "> for user : " + fullUserName + "\nVOS Http Response : "
					+ addSuiteResp.getHttpStatusCode() + "\nVOS Status Code : "
					+ addSuiteResp.getVOSStatusCode() + "\nVOS Response Info ："
					+ addSuiteResp.getVOSResponseInfo());
			return "2003";
		}

		return "0";
	}

	@RequestMapping("/checkUserExist")
	public void checkUserExist(
			@RequestParam(value = "countryCode", required = true) String countryCode,
			@RequestParam(value = "username", required = true) String userName,
			HttpServletResponse response) throws JSONException, SQLException,
			IOException {
		JSONObject ret = new JSONObject();
		boolean isExist = userDao.isExistsLoginName(countryCode, userName);
		ret.put("result", isExist);
		response.getWriter().print(ret.toString());
	}

	@RequestMapping("/getUserPwd")
	public void getUserPassword(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws IOException {
		try {
			Map<String, Object> user = userDao.getUser(countryCode, userName);
			if (user == null) {
				log.info("user is null");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			String newPwd = RandomString.genRandomNum(6);
			int rows = userDao.changePassword(userName, CryptoUtil.md5(newPwd),
					countryCode);
			if (rows > 0) {
				String msg = String.format(
						"您的新密码是%s，请登录后及时修改您的密码。[AngolaCall]", newPwd);
				String bindPhone = (String) user.get("bindphone");
				smsClient.sendTextMessage(bindPhone, msg);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (DataAccessException e) {
			// e.printStackTrace();
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@RequestMapping(value = "/clientdirectreg", method = RequestMethod.POST)
	public void clientDirectReg(
			HttpServletResponse response,
			@RequestParam String phoneNumber,
			@RequestParam String countryCode,
			@RequestParam String password,
			@RequestParam(value = "brand", required = false, defaultValue = "") String brand,
			@RequestParam(value = "model", required = false, defaultValue = "") String model,
			@RequestParam(value = "release", required = false, defaultValue = "") String release,
			@RequestParam(value = "sdk", required = false, defaultValue = "") String sdk,
			@RequestParam(value = "width", required = false, defaultValue = "0") String width,
			@RequestParam(value = "height", required = false, defaultValue = "0") String height)
			throws IOException {
		String result = "0";
		result = userDao.checkRegisterPhone(countryCode, phoneNumber);

		if (result.equals("0")) {
			result = userDao.regUser(countryCode, phoneNumber, "", "",
					password, password);
		}

		if ("0".equals(result)) { // insert success
			result = finishVosRegister(countryCode, phoneNumber);
		}

		if ("0".equals(result)) {
			userDao.recordDeviceInfo(phoneNumber, countryCode, brand, model,
					release, sdk, width, height);
		}

		JSONObject jsonUser = new JSONObject();
		try {
			jsonUser.put("result", result);
			if ("0".equals(result)) {
				UserBean user = userDao.getUserBean(countryCode, phoneNumber);
				if (null != user) {
					jsonUser.put("result", "0");
					jsonUser.put("username", phoneNumber);
					jsonUser.put("countrycode", countryCode);
					jsonUser.put("userkey", user.getUserKey());
					jsonUser.put("vosphone", user.getVosPhone());
					jsonUser.put("vosphone_pwd", user.getVosPhonePwd());
					jsonUser.put("bindphone", user.getBindPhone());
					jsonUser.put("bindphone_country_code",
							user.getBindPhoneCountryCode());
					jsonUser.put("status", user.getStatus());
					jsonUser.put("email", user.getEmail());
					jsonUser.put("email_status", user.getEmailStatus());
					jsonUser.put("reg_given_money", user.getFrozenMoney());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		response.getWriter().print(jsonUser.toString());
	}

	private String finishVosRegister(String countryCode, String userName) {
		String result = "0";
		Map<String, Object> vosPhoneInfoMap = userDao.getVOSPhone(countryCode,
				userName);
		Long vosPhoneNumber = (Long) vosPhoneInfoMap.get("vosphone");
		String vosPhonePwd = (String) vosPhoneInfoMap.get("vosphone_pwd");
		result = addUserToVOS(countryCode + userName, countryCode,
				vosPhoneNumber.toString(), vosPhonePwd);

		if ("0".equals(result)) {
			int affectedRows = userDao.updateUserAccountStatus(countryCode,
					userName, UserAccountStatus.success);
			if (affectedRows > 0) {
				result = "0";
			} else {
				result = "1";
			}
		} else if ("2001".equals(result)) {
			userDao.updateUserAccountStatus(countryCode, userName,
					UserAccountStatus.vos_account_error);
		} else if ("2002".equals(result)) {
			userDao.updateUserAccountStatus(countryCode, userName,
					UserAccountStatus.vos_phone_error);
		} else if ("2003".equals(result)) {
			userDao.updateUserAccountStatus(countryCode, userName,
					UserAccountStatus.vos_suite_error);
		}

		if ("0".equals(result)) {
			if (countryCode.equals(config.getAocbRegCountryCode())) {
				vosClient.deposite(countryCode + userName, Double
						.parseDouble(ContextLoader.getUUTalkConfigManager()
								.getAngolaCallbackVersionRegisterMoney()));
			} else {
				Double money = ContextLoader.getUUTalkConfigManager()
						.getRegisterGivenMoney();
				if (money != null && money > 0) {
					userDao.setFrozenMoney(countryCode, userName, money);
				}
			}
		}

		return result;
	}

	private void sendPwdResetEmail(Map<String, Object> user)
			throws AddressException, MessagingException {
		Configuration config = ContextLoader.getConfiguration();

		String countryCode = (String) user.get("countrycode");
		String userName = (String) user.get("username");
		String email = (String) user.get("email");
		String randomId = (String) user.get("random_id");
		String title = "安中通账户密码重置";
		String url = config.getServerUrl() + "/user/resetpwdvialink/"
				+ randomId;

		String content = "<h3>亲爱的用户" + countryCode + userName
				+ "，<br/>欢迎您使用安中通网络电话。</h3>"
				+ "<p><h4>点击密码重置，您可重新设置您的密码。</h4><br/>" + "<a href=\"" + url
				+ "\"><button type=\"button\">密码重置</button></a><br/><br/>"
				+ "如果不能点击，请复制以下链接到浏览器。<br/>" + url + "</p>";
		MailSender mailSender = ContextLoader.getMailSender();
		mailSender.sendMail(email, title, content);
	}

	@RequestMapping("/sendResetPwdEmail")
	public void sendResetPwdEmailApi(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws IOException, JSONException {
		log.info("sendResetPwdEmailApi - countryCode: " + countryCode
				+ " username: " + userName);
		JSONObject ret = new JSONObject();
		int rows = userDao.updateRandomId(countryCode, userName,
				RandomString.getRandomId(countryCode + userName));
		if (rows <= 0) {
			log.info("sendResetPwdEmailApi - user not found");
			ret.put("result", "user_not_found");
			response.getWriter().print(ret.toString());
			return;
		}

		Map<String, Object> user = userDao.getUser(countryCode, userName);
		String email = (String) user.get(UserConstants.email.name());
		String emailStatus = (String) user.get(UserConstants.email_status
				.name());

		if (email == null || email.equals("")) {
			ret.put("result", "email_not_set");
			response.getWriter().print(ret.toString());
			return;
		}

		if (!EmailStatus.verified.name().equals(emailStatus)) {
			ret.put("result", "email_unverify");
			ret.put("email", email);
			response.getWriter().print(ret.toString());
			return;
		}

		try {
			sendPwdResetEmail(user);
			ret.put("email", email);
			ret.put("result", "send_ok");
			response.getWriter().print(ret.toString());
		} catch (Exception e) {
			e.printStackTrace();
			ret.put("result", "send_failed");
			response.getWriter().print(ret.toString());
			return;
		}
	}
}
