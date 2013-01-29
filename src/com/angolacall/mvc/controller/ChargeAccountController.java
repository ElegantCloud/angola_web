package com.angolacall.mvc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alipay.client.base.PartnerConfig;
import com.alipay.client.security.RSASignature;
import com.alipay.util.AlipayNotify;
import com.angolacall.constants.ChargeStatus;
import com.angolacall.constants.ChargeType;
import com.angolacall.constants.WebConstants;
import com.angolacall.framework.ContextLoader;
import com.angolacall.mvc.model.charge.ChargeDAO;
import com.angolacall.mvc.model.charge.ChargeUtil;
import com.angolacall.web.user.UserBean;
import com.richitec.sms.client.SMSClient;
import com.richitec.ucenter.model.UserDAO;
import com.richitec.util.Pager;
import com.richitec.util.RandomString;
import com.richitec.vos.client.VOSClient;
import com.richitec.vos.client.VOSHttpResponse;

@Controller
public class ChargeAccountController {
	private static Log log = LogFactory.getLog(ChargeAccountController.class);

	private VOSClient vosClient;
	private ChargeDAO chargeDao;
	private UserDAO userDao;
	private SMSClient smsClient;

	@PostConstruct
	public void init() {
		vosClient = ContextLoader.getVOSClient();
		chargeDao = ContextLoader.getChargeDAO();
		userDao = ContextLoader.getUserDAO();
		smsClient = ContextLoader.getSMSClient();
	}

	@RequestMapping(value = "/deposite", method = RequestMethod.GET)
	public ModelAndView deposite() {
		ModelAndView view = new ModelAndView();
		view.setViewName("deposite");
		view.addObject(WebConstants.page_name.name(), "deposite");
		view.addObject("charge_money_list", ContextLoader.getChargeMoneyConfigDao().getChargeMoneyList());
		return view;
	}
	
	/**
	 * 充值中心页面
	 */	
	@RequestMapping(value = "/chongzhi", method=RequestMethod.GET)
	public ModelAndView chongzhiGet() {
		ModelAndView view = new ModelAndView();
		view.setViewName("chongzhi");
		view.addObject("charge_money_list", ContextLoader.getChargeMoneyConfigDao().getChargeMoneyList());
		return view;
	}
	
	/**
	 * 通过充值中心页面提交充值
	 * @throws SQLException 
	 */
	@RequestMapping(value="/chongzhi", method=RequestMethod.POST)
	public ModelAndView chongzhiPost(HttpServletResponse response,
		@RequestParam(value = "countryCode", required=false, defaultValue="") String countryCode,
		@RequestParam(value = "accountName", required=false, defaultValue="") String accountName,
		@RequestParam(value = "depositeType") String depositeType,
		@RequestParam(value = "depositeId", required=false) String depositeId,
		@RequestParam(value = "cardNumber", required=false) String cardNumber,
		@RequestParam(value = "cardPwd", required=false) String cardPwd) throws SQLException {
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("charge_money_list", ContextLoader.getChargeMoneyConfigDao().getChargeMoneyList());
		
		boolean isExist = userDao.isExistsLoginName(countryCode, accountName);
		if (!isExist) {
			mv.setViewName("chongzhi");
			mv.addObject("accountError", "NoUser");
			return mv;
		}
		
		if ("alipay".equals(depositeType)){
			if (null == depositeId || depositeId.isEmpty()){
				mv.setViewName("chongzhi");
				mv.addObject("alipayError", "请选择充值金额");
				return mv;
			} else {
				//支付宝暂时不可用，临时添加
				mv.setViewName("chongzhi");
				mv.addObject("alipayError", "支付宝暂时不可用，造成不便敬请谅解！");
				return mv;
			}
			
//			mv.setViewName("accountcharge/alipay");
//			return mv;
		}
		
		if ("azcard".equals(depositeType)){
			Double value = getCardValue(cardNumber);
			if (null == value){
				mv.setViewName("chongzhi");
				mv.addObject("uutalkError", "InvalidCardNumber");
				return mv;
			}
			
			VOSHttpResponse vosResp = depositeToVOS(countryCode, accountName,
					cardNumber, cardPwd, value);
			
			if (vosResp.getHttpStatusCode()!= 200){
				mv.setViewName("chongzhi");
				mv.addObject("vosHttpError", vosResp);
			} else
			if (!vosResp.isOperationSuccess()){
				mv.setViewName("chongzhi");
				mv.addObject("vosError", vosResp);
				return mv;
			} else {
				mv.setViewName("accountcharge/vosComplete");
				mv.addObject("vosResponse", vosResp);
				return mv;
			}
		}
		
		mv.setViewName("chongzhi");
		return mv;
	}	
	
	/**
	 * 根据卡号获取充值卡面额
	 * 卡号前四位为充值卡面额
	 * 
	 * @param cardNumber
	 * @return
	 */
	private Double getCardValue(String cardNumber){
		if (null == cardNumber || cardNumber.length()<4){
			return null;
		}
		
		Double value = null;
		String cardValue = cardNumber.substring(0, 4);
		try {
			value = Double.parseDouble(cardValue);
		} catch (NumberFormatException e) {
			return null;
		}
		
		return value;
	}
	
	private VOSHttpResponse depositeToVOS(
			String countryCode, String accountName,
			String cardNumber, String cardPwd, Double value){
		
		String chargeId = getCardChargeId(cardNumber);
		VOSHttpResponse vosResp = vosClient.depositeByCard(countryCode
				+ accountName, cardNumber, cardPwd);
		if (vosResp.getHttpStatusCode() != 200 || !vosResp.isOperationSuccess()) {
			chargeDao.addChargeRecord(chargeId, countryCode, accountName, value,
					ChargeStatus.vos_fail);
			log.error("\nCannot deposite to account <" + accountName
					+ "> with card <" + cardNumber + ">" + "<" + cardPwd + ">"
					+ "\nVOS Http Response : " + vosResp.getHttpStatusCode()
					+ "\nVOS Status Code : " + vosResp.getVOSStatusCode()
					+ "\nVOS Response Info ：" + vosResp.getVOSResponseInfo());
		}
		
		if (vosResp.isOperationSuccess()) {
			/*
			 * log.info("VOS INFO : " + vosResp.getVOSResponseInfo());
			 * DepositeCardInfo info = new
			 * DepositeCardInfo(vosResp.getVOSResponseInfo());
			 * mv.addObject("despositeInfo", info);
			 */
			chargeDao.addChargeRecord(chargeId, countryCode, accountName, value,
					ChargeStatus.success);
//			smsClient.sendTextMessage(accountName, "您的安中通账户已成功充值" + value
//					+ "元，谢谢！");
			
			ChargeUtil.checkAndGiveMoneyToReferrer(countryCode, accountName, value);
		}
		
		return vosResp;
	}

	/**
	 * 充值卡号前四位表示该卡的面额
	 * 
	 * @param response
	 * @param accountName
	 * @param pin
	 * @param cardPwd
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	@RequestMapping(value = "/cardchargepage", method = RequestMethod.POST)
	public ModelAndView cardchargepage(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "accountName") String accountName,
			@RequestParam(value = "cardNumber") String cardNumber,
			@RequestParam(value = "cardPwd") String cardPwd)
			throws IOException, SQLException {
		ModelAndView mv = new ModelAndView();
		boolean isExist = userDao.isExistsLoginName(countryCode, accountName);
		if (!isExist) {
			mv.setViewName("accountcharge/invalidAccount");
			return mv;
		}

		Double value = getCardValue(cardNumber);
		if (null == value){
			mv.setViewName("accountcharge/invalidPin");
			return mv;
		}
		
		VOSHttpResponse vosResp = depositeToVOS(countryCode, accountName,
				cardNumber, cardPwd, value);

		mv.addObject("vosResponse", vosResp);
		mv.setViewName("accountcharge/vosComplete");
		return mv;
	}

	@RequestMapping(value = "/accountcharge", method = RequestMethod.GET)
	public ModelAndView accountCharge(
			HttpSession session,
			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset) {
		ModelAndView view = new ModelAndView();
		view.setViewName("accountcharge");
		view.addObject(WebConstants.page_name.name(), "accountcharge");
		// get account
		UserBean userBean = (UserBean) session
				.getAttribute(UserBean.SESSION_BEAN);

		// get account balance
		view.addObject(
				WebConstants.balance.name(),
				vosClient.getAccountBalance(userBean.getCountryCode()
						+ userBean.getUserName()));

		// get charge history list
		int total = chargeDao.getChargeListTotalCount(
				userBean.getCountryCode(), userBean.getUserName());
		int pageSize = 10;
		List<Map<String, Object>> chargeList = chargeDao.getChargeList(
				userBean.getCountryCode(), userBean.getUserName(), offset,
				pageSize);

		String url = "accountcharge?";
		Pager pager = new Pager(offset, pageSize, total, url);
		view.addObject(WebConstants.pager.name(), pager);
		view.addObject(WebConstants.charge_list.name(), chargeList);
		return view;
	}

	@RequestMapping(value = "/alipay", method = RequestMethod.POST)
	public ModelAndView aliPay(HttpSession session,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "accountName") String accountName,
			@RequestParam(value = "depositeId") String depositeId)
			throws Exception {
		log.info("****** prepay alipay ******");
		boolean isExist = userDao.isExistsLoginName(countryCode, accountName);
		ModelAndView mv = new ModelAndView();
		if (isExist) {
			mv.setViewName("accountcharge/alipay");
		} else {
			mv.setViewName("accountcharge/invalidAccount");
		}
		return mv;
	}

	private Map<String, String> getParameterMap(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		return params;
	}

	/**
	 * 支付宝异步返回URL
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/alipayComplete")
	public @ResponseBody
	String aliPayComplete(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		log.info("****** alipay complete ******");
		// Map<String, String> params = new HashMap<String, String>();
		// Map requestParams = request.getParameterMap();
		// for (Iterator iter = requestParams.keySet().iterator();
		// iter.hasNext();) {
		// String name = (String) iter.next();
		// String[] values = (String[]) requestParams.get(name);
		// String valueStr = "";
		// for (int i = 0; i < values.length; i++) {
		// valueStr = (i == values.length - 1) ? valueStr + values[i]
		// : valueStr + values[i] + ",";
		// }
		// params.put(name, valueStr);
		// }
		Map<String, String> params = getParameterMap(request);

		String order_no = request.getParameter("out_trade_no"); // 获取订单号
		String total_fee = request.getParameter("total_fee"); // 获取总金额
		String trade_status = request.getParameter("trade_status"); // 交易状态

		log.info("trade_status: " + trade_status);
		if (AlipayNotify.verify(params)) {
			if (trade_status.equals("TRADE_FINISHED")
					|| trade_status.equals("TRADE_SUCCESS")) {
				ChargeUtil.finishCharge(order_no, total_fee);
			} else {
				chargeDao.updateChargeRecord(order_no, ChargeStatus.fail);
			}
			return "success";
		} else {
			chargeDao.updateChargeRecord(order_no, ChargeStatus.fail);
			return "fail";
		}
	}

	/**
	 * 支付宝同步返回URL
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/alipayReturn")
	public ModelAndView aliPayReturn(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws Exception {
		log.info("****** alipay return ******");
		ModelAndView mv = new ModelAndView();
		mv.setViewName("accountcharge/receive");
		// 获取支付宝GET过来反馈信息
		// Map<String, String> params = new HashMap<String, String>();
		// Map requestParams = request.getParameterMap();
		// for (Iterator iter = requestParams.keySet().iterator();
		// iter.hasNext();) {
		// String name = (String) iter.next();
		// String[] values = (String[]) requestParams.get(name);
		// String valueStr = "";
		// for (int i = 0; i < values.length; i++) {
		// valueStr = (i == values.length - 1) ? valueStr + values[i]
		// : valueStr + values[i] + ",";
		// }
		// params.put(name, valueStr);
		// }
		Map<String, String> params = getParameterMap(request);

		String order_no = request.getParameter("out_trade_no"); // 获取订单号
		String total_fee = request.getParameter("total_fee"); // 获取总金额
		String trade_status = request.getParameter("trade_status"); // 交易状态

		if (AlipayNotify.verify(params)) {
			if (trade_status.equals("TRADE_FINISHED")
					|| trade_status.equals("TRADE_SUCCESS")) {
				// 判断该笔订单是否在商户网站中已经做过处理（可参考“集成教程”中“3.4返回数据处理”）
				// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
				// 如果有做过处理，不执行商户的业务程序
				String accountName = ChargeUtil.finishCharge(order_no,
						total_fee);
				mv.addObject("result", "0");
				mv.addObject(WebConstants.charge_money.name(), total_fee);
				mv.addObject(WebConstants.pay_account_name.name(), accountName);
			} else {
				mv.addObject("result", "1");
			}
		} else {
			// 该页面可做页面美工编辑
			mv.addObject("result", "1");
		}
		return mv;
	}

	// API urls

	/**
	 * get account balance, used for API
	 * 
	 * @param response
	 * @param userName
	 * @throws JSONException
	 * @throws IOException
	 */
	@RequestMapping("/accountBalance")
	public void accountBalance(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName)
			throws JSONException, IOException {
		// get account balance
		JSONObject ret = new JSONObject();
		Double value = vosClient.getAccountBalance(countryCode + userName);
		ret.put("result", null == value ? 1 : 0);
		if (null != value) {
			ret.put(WebConstants.balance.name(), value);
		}
		response.getWriter().print(ret.toString());
	}

	/**
	 * charge with card, used for API
	 * 
	 * @param response
	 * @param userName
	 * @param pin
	 * @param password
	 * @throws IOException
	 * @throws SQLException
	 */
	@RequestMapping(value = "/cardCharge", method = RequestMethod.POST)
	public void cardCharge(HttpServletResponse response,
			@RequestParam(value = "countryCode") String countryCode,
			@RequestParam(value = "username") String userName,
			@RequestParam(value = "pin") String pin,
			@RequestParam(value = "password") String password)
			throws IOException, SQLException {
		boolean isExist = userDao.isExistsLoginName(countryCode, userName);
		if (!isExist) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (pin.length() < 4) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Double value = 0.0;
		String cardValue = pin.substring(0, 4);
		log.info("card value: " + cardValue);
		try {
			value = Double.parseDouble(cardValue);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String chargeId = getCardChargeId(pin);
		log.info("charge id: " + chargeId);

		VOSHttpResponse vosResp = vosClient.depositeByCard(countryCode
				+ userName, pin, password);

		// log.info("\n deposite to account <" + userName + "> with card <" +
		// pin
		// + ">" + "<" + password + ">" + "\nVOS Http Response : "
		// + vosResp.getHttpStatusCode() + "\nVOS Status Code : "
		// + vosResp.getVOSStatusCode() + "\nVOS Response Info ："
		// + vosResp.getVOSResponseInfo());

		if (vosResp.getHttpStatusCode() != 200 || !vosResp.isOperationSuccess()) {
			chargeDao.addChargeRecord(chargeId, countryCode, userName, value,
					ChargeStatus.vos_fail);
			log.error("\nCannot deposite to account <" + userName
					+ "> with card <" + pin + ">" + "<" + password + ">"
					+ "\nVOS Http Response : " + vosResp.getHttpStatusCode()
					+ "\nVOS Status Code : " + vosResp.getVOSStatusCode()
					+ "\nVOS Response Info ：" + vosResp.getVOSResponseInfo());
		}

		if (vosResp.isOperationSuccess()) {
			chargeDao.addChargeRecord(chargeId, countryCode, userName, value,
					ChargeStatus.success);
			smsClient.sendTextMessage(userName, "您的安中通账户已成功充值" + value
					+ "元，谢谢！");
			
			ChargeUtil.checkAndGiveMoneyToReferrer(countryCode, userName, value);
			response.setStatus(HttpServletResponse.SC_OK);
		} else if (vosResp.getVOSStatusCode() == -10079) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else if (vosResp.getVOSStatusCode() == -10078) {
			response.sendError(HttpServletResponse.SC_CONFLICT);
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}
	
	private String getCardChargeId(String pin) {
		return ChargeType.card.name() + pin + "_"
				+ RandomString.genRandomChars(10);
	}

	@RequestMapping("/alipayClientComplete")
	public void alipayClientComplete(HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		log.info("=== alipayClientComplete ===");
		// 获得通知参数
		Map map = request.getParameterMap();
		// 获得通知签名
		String sign = (String) ((Object[]) map.get("sign"))[0];
		log.info("sign: " + sign);

		String notify_data = (String) ((Object[]) map.get("notify_data"))[0];
		log.info("notify data: " + notify_data);

		// 获得待验签名的数据
		String verifyData = "notify_data=" + notify_data;
		boolean verified = false;
		PrintWriter out = response.getWriter();

		// 使用支付宝公钥验签名
		try {
			verified = RSASignature.doCheck(verifyData, sign,
					PartnerConfig.RSA_ALIPAY_PUBLIC);
		} catch (Exception e) {
			e.printStackTrace();
			out.print("fail");
			return;
		}

		try {
			JSONObject notifyInfo = XML.toJSONObject(notify_data)
					.getJSONObject("notify");
			log.info("notify json: " + notifyInfo.toString());

			String order_no = notifyInfo.getString("out_trade_no");
			String total_fee = notifyInfo.getString("total_fee");
			String trade_status = notifyInfo.getString("trade_status");

			log.info("order_no: " + order_no);
			log.info("total_fee: " + total_fee);
			log.info("trade_status: " + trade_status);
			// 验证签名通过
			if (verified) {
				// 根据交易状态处理业务逻辑
				// 当交易状态成功，处理业务逻辑成功。回写success
				if (trade_status.equals("TRADE_FINISHED")
						|| trade_status.equals("TRADE_SUCCESS")) {
					ChargeUtil.finishCharge(order_no, total_fee);
				} else {
					chargeDao.updateChargeRecord(order_no, ChargeStatus.fail);
				}
				out.print("success");
			} else {
				log.info("sign check failed for alipay notification");
				chargeDao.updateChargeRecord(order_no, ChargeStatus.fail);
				out.print("fail");
			}
		} catch (Exception e) {
			e.printStackTrace();
			out.print("fail");
		}
	}

}
