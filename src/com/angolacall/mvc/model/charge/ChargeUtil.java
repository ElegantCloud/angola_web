package com.angolacall.mvc.model.charge;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.angolacall.constants.ChargeStatus;
import com.angolacall.constants.ChargeType;
import com.angolacall.framework.ContextLoader;
import com.angolacall.mvc.admin.model.InviteChargeGiftPlan;
import com.angolacall.mvc.admin.model.NoticeDao;
import com.richitec.sms.client.SMSClient;
import com.richitec.ucenter.model.UserDAO;
import com.richitec.util.RandomString;
import com.richitec.vos.client.VOSClient;
import com.richitec.vos.client.VOSHttpResponse;

public class ChargeUtil {
	private static Log log = LogFactory.getLog(ChargeUtil.class);

	/**
	 * 得到订单号
	 * 
	 * @param type
	 *            - pay type (alipay, netbank, card)
	 * @param accountName
	 *            - account to charge
	 * @return
	 */
	public static String getOrderNumber(String type, String countryCode,
			String accountName) {
		Date currTime = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("_yyyyMMdd_HHmmss_",
				Locale.US);
		return type + sf.format(currTime) + countryCode + accountName + "_"
				+ RandomString.validateCode();
	}

	public static String finishCharge(String chargeId) {
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
			return userName;
		}

		Float chargeMoney = (Float) chargeInfo.get("money");
		
		Double amount = chargeMoney.doubleValue();
		VOSClient vosClient = ContextLoader.getVOSClient();
		UserDAO userDao = ContextLoader.getUserDAO();
		VOSHttpResponse response = vosClient.deposite(userDao.genVosAccountName(countryCode, userName),
				amount); // deposit charged money
		if (response.isOperationSuccess()) {
			log.info("vos deposite success");
			chargeDao.updateChargeRecord(chargeId, ChargeStatus.success);
			
			SMSClient smsClient = ContextLoader.getSMSClient();
			try {
				smsClient.sendTextMessage(userName, "您的环宇通账户已成功充值" + String.format("%.2f", amount.floatValue())
						+ "元，谢谢！");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// give money to referrer if has
			checkAndGiveMoneyToReferrer(countryCode, userName, amount);

			// check if there is gift money, if has, deposit to user
			Integer chargeMoneyCfgId = (Integer) chargeInfo
					.get("charge_money_cfg_id");
			checkAndDepositGiftMoneyToCharger(chargeMoneyCfgId, countryCode,
					userName);

			return userName;
		} else {
			log.info("vos deposite fail");
			chargeDao.updateChargeRecord(chargeId, ChargeStatus.vos_fail);
			return null;
		}
	}

	/**
	 * check the referrer gift money and give it to referrer
	 * 
	 * @param countryCode
	 * @param userName
	 * @param chargeMoney
	 */
	public static void checkAndGiveMoneyToReferrer(String countryCode,
			String userName, double chargeMoney) {
		// give money to referrer if has
		Map<String, Object> user = ContextLoader.getUserDAO().getUser(
				countryCode, userName);
		String referrer = (String) user.get("referrer");
		String referrerCountryCode = (String) user.get("referrer_country_code");
		if (referrer != null && referrerCountryCode != null
				&& !referrer.equals("") && !referrerCountryCode.equals("")) {
			Double giveAmount = InviteChargeGiftPlan
					.calculateGiftMoney(chargeMoney);
			if (giveAmount > 0) {
				giveMoneyToReferrer(ChargeType.chargecontribute,
						referrerCountryCode, referrer, giveAmount, countryCode,
						userName);
			}
		}
	}

	private static void checkAndDepositGiftMoneyToCharger(
			Integer chargeMoneyCfgId, String countryCode, String userName) {
		if (chargeMoneyCfgId == null) {
			return;
		}

		ChargeDAO chargeDao = ContextLoader.getChargeDAO();
		VOSClient vosClient = ContextLoader.getVOSClient();
		Map<String, Object> record = ContextLoader.getChargeMoneyConfigDao()
				.getChargeMoneyRecord(chargeMoneyCfgId);
		Float giftMoney = (Float) record.get("gift_money");
		if (giftMoney != null) {
			// deposit gift money to user
			VOSHttpResponse response = vosClient.deposite(ContextLoader.getUserDAO().genVosAccountName(countryCode, userName), giftMoney.doubleValue());
			if (response.isOperationSuccess()) {
				String giftChargeId = getOrderNumber(
						ChargeType.chargegift.name(), countryCode, userName);
				chargeDao.addChargeRecord(giftChargeId, countryCode, userName,
						giftMoney.doubleValue(), ChargeStatus.success);
			}
		}
	}

	/**
	 * give money to referrer
	 * 
	 * @param chargetype
	 * @param referrerCountryCode
	 * @param referrer
	 *            - target user to give money
	 * @param money
	 *            - amount of money to give
	 * @param contributorCountryCode
	 * @param contributor
	 *            - user who makes the contribution
	 */
	public static void giveMoneyToReferrer(ChargeType chargetype,
			String referrerCountryCode, String referrer, Double money,
			String contributorCountryCode, String contributor) {
		String chargeId = getOrderNumber(chargetype.name(),
				referrerCountryCode, referrer);
		VOSClient vosClient = ContextLoader.getVOSClient();
		UserDAO userDao = ContextLoader.getUserDAO();
		VOSHttpResponse response = vosClient.deposite(userDao.genVosAccountName(referrerCountryCode, referrer), money);
		if (response.isOperationSuccess()) {
			ContextLoader.getChargeDAO().addChargeRecord(chargeId,
					referrerCountryCode, referrer, money,
					contributorCountryCode, contributor, ChargeStatus.success);

			// send notice tp referrer
			NoticeDao noticeDao = ContextLoader.getNoticeDao();
			String msgContent = String
					.format("系统赠送给您%.2f元", money.floatValue());
			if (ChargeType.invitereg == chargetype) {
				msgContent = "您邀请的用户成功注册，" + msgContent;
			} else if (ChargeType.chargecontribute == chargetype) {
				msgContent = "您邀请的用户成功充值，" + msgContent;
			}

			noticeDao.sendNoticeToUser(referrerCountryCode, referrer,
					msgContent);

		}
	}

	/**
	 * charge user
	 * @param chargeType
	 * @param countryCode
	 * @param userName
	 * @param money
	 * @return true - charge successfully, false - charge failed
	 */
	public static boolean chargeUser(ChargeType chargeType, String countryCode,
			String userName, Double money) {
		String chargeId = getOrderNumber(chargeType.name(), countryCode,
				userName);
		VOSClient vosClient = ContextLoader.getVOSClient();
		UserDAO userDao = ContextLoader.getUserDAO();
		VOSHttpResponse response = vosClient.deposite(userDao.genVosAccountName(countryCode, userName),
				money);
		if (response.isOperationSuccess()) {
			ContextLoader.getChargeDAO().addChargeRecord(chargeId, countryCode, userName, money, ChargeStatus.success);
			return true;
		} else {
			return false;
		}
		
	}
}
