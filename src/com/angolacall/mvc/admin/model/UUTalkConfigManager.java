package com.angolacall.mvc.admin.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.angolacall.constants.UUTalkConfigKeys;
import com.angolacall.framework.ContextLoader;
import com.richitec.ucenter.model.CommonConfigDao;

public class UUTalkConfigManager {
	private static Log log = LogFactory.getLog(UUTalkConfigManager.class);
	private CommonConfigDao commonConfigDao;

	public void setCommonConfigDao(CommonConfigDao ccd) {
		this.commonConfigDao = ccd;
	}

	public String getRegGiftValue() {
		String giftValue = commonConfigDao
				.getValue(UUTalkConfigKeys.reg_gift_value.name());
		log.info("giftValue: " + giftValue);
		if (giftValue == null) {
			giftValue = "0";
		}
		return giftValue;
	}

	public void setRegGiftValue(String regGiftValue) {
		commonConfigDao.setValue(UUTalkConfigKeys.reg_gift_value.name(),
				regGiftValue);
	}

	public void setRegGiftDescription(String desc) {
		commonConfigDao.setValue(UUTalkConfigKeys.reg_gift_desc_text.name(),
				desc);
	}

	public String getRegGiftDescription() {
		String desc = commonConfigDao
				.getValue(UUTalkConfigKeys.reg_gift_desc_text.name());
		if (desc == null) {
			desc = "";
		}
		return desc;
	}

	public void setInviteChargeGiftDescription(String desc) {
		commonConfigDao.setValue(
				UUTalkConfigKeys.invite_charge_invite_desc_text.name(), desc);
	}

	public String getInviteChargeGiftDescription() {
		String desc = commonConfigDao
				.getValue(UUTalkConfigKeys.invite_charge_invite_desc_text
						.name());
		if (desc == null) {
			desc = "";
		}
		return desc;
	}

	public void setDefaultRegisterMoney(String money) {
		commonConfigDao.setValue(
				UUTalkConfigKeys.default_register_money.name(), money);
	}

	public String getDefaultRegisterMoney() {
		String money = commonConfigDao
				.getValue(UUTalkConfigKeys.default_register_money.name());
		if (money == null) {
			money = "0";
		}
		return money;
	}

	public Double getRegisterGivenMoney() {
		Double defaultMoney = Double.parseDouble(getDefaultRegisterMoney());
		RegisterActivityDao rad = ContextLoader.getRegisterActivityDao();
		Date today = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = df.format(today);
		Double giftMoney = rad.getActivityGiftMoney(currentDate);
		return defaultMoney > giftMoney ? defaultMoney : giftMoney;
	}

	public void setAdClickGiftMoney(String money) {
		commonConfigDao.setValue(UUTalkConfigKeys.ad_click_gift_money.name(),
				money);
	}

	public String getAdClickGiftMoney() {
		String money = commonConfigDao
				.getValue(UUTalkConfigKeys.ad_click_gift_money.name());
		if (money == null) {
			money = "0";
		}
		return money;
	}
	
	public void setAdMaxGiftMoney(String money) {
		commonConfigDao.setValue(UUTalkConfigKeys.ad_click_max_gift_money.name(), money);
	}
	
	public String getAdClickMaxGiftMoney() {
		String money = commonConfigDao
				.getValue(UUTalkConfigKeys.ad_click_max_gift_money.name());
		if (money == null) {
			money = "0";
		}
		return money;
	}
}
