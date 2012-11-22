package com.angolacall.mvc.model.charge;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.angolacall.constants.ChargeStatus;
import com.angolacall.constants.ChargeType;
import com.angolacall.framework.ContextLoader;
import com.richitec.util.RandomString;
import com.richitec.vos.client.VOSClient;
import com.richitec.vos.client.VOSHttpResponse;

public class ChargeUtil {
	private static Log log = LogFactory.getLog(ChargeUtil.class);
	/**
	 * 得到订单号
	 * 
	 * @param type - pay type (alipay, netbank, card)
	 * @param accountName - account to charge
	 * @return
	 */
	public static String getOrderNumber(String type, String countryCode, String accountName) {
		Date currTime = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("_yyyyMMdd_HHmmss_",
				Locale.US);
		return type + sf.format(currTime) + countryCode + accountName + "_"
				+ RandomString.validateCode();
	}
	
	public static String finishCharge(String chargeId, String money) {
		ChargeDAO chargeDao = ContextLoader.getChargeDAO();
		Map<String, Object> chargeInfo = chargeDao.getChargeInfoById(chargeId);
		if (chargeInfo == null) {
			return null;
		}
		String userName = (String) chargeInfo.get("username");
		String countryCode = (String) chargeInfo.get("countrycode");
		if (userName == null || countryCode == null) {
			return null;
		}
		String status = (String) chargeInfo.get("status");
		if (ChargeStatus.success.name().equals(status)) {
			return countryCode + userName;
		}
		
		Double amount = Double.valueOf(money);
		VOSClient vosClient = ContextLoader.getVOSClient();
		VOSHttpResponse response = vosClient.deposite(countryCode + userName, amount);
		if (response.isOperationSuccess()) {
			log.info("vos deposite success");
			chargeDao.updateChargeRecord(chargeId, ChargeStatus.success);
			
			// give money to referrer if has
			Map<String, Object> user = ContextLoader.getUserDAO().getUser(countryCode, userName);
			String referrer = (String) user.get("referrer");
			String referrerCountryCode = (String) user.get("referrer_country_code");
			if (referrer != null && referrerCountryCode != null && !referrer.equals("") && !referrerCountryCode.equals("")) {
				Double giveAmount = amount * ContextLoader.getConfiguration().getChargeGivingPercentage();
				giveMoneyToReferrer(ChargeType.chargecontribute, referrerCountryCode, referrer, giveAmount, countryCode, userName);
			}
			
			return countryCode + userName;
		} else {
			log.info("vos deposite fail");
			chargeDao.updateChargeRecord(chargeId, ChargeStatus.vos_fail);
			return null;
		}
	}
	
	/**
	 * give money to referrer
	 * @param chargetype
	 * @param referrerCountryCode
	 * @param referrer - target user to give money
	 * @param money - amount of money to give
	 * @param contributorCountryCode
	 * @param contributor - user who makes the contribution
	 */
	public static void giveMoneyToReferrer(ChargeType chargetype, String referrerCountryCode, String referrer, Double money, String contributorCountryCode, String contributor) {
		String chargeId = getOrderNumber(chargetype.name(), referrerCountryCode, referrer);
		VOSClient vosClient = ContextLoader.getVOSClient();
		VOSHttpResponse response = vosClient.deposite(referrerCountryCode + referrer, money);
		if (response.isOperationSuccess()) {
			ContextLoader.getChargeDAO().addChargeRecord(chargeId, referrerCountryCode, referrer, money, contributorCountryCode, contributor, ChargeStatus.success);
		}
	}
}
