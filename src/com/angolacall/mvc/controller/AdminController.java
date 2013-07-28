package com.angolacall.mvc.controller;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.angolacall.constants.Pages;
import com.angolacall.constants.UUTalkConfigKeys;
import com.angolacall.constants.WebConstants;
import com.angolacall.framework.ContextLoader;
import com.angolacall.mvc.admin.model.ChargeMoneyConfigDao;
import com.angolacall.mvc.admin.model.UUTalkConfigManager;
import com.angolacall.web.user.AdminUserBean;
import com.richitec.ucenter.model.AdminUserDao;
import com.richitec.util.ValidatePattern;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private static Log log = LogFactory.getLog(AdminController.class);

	private UUTalkConfigManager ucm;

	@PostConstruct
	public void init() {
		ucm = ContextLoader.getUUTalkConfigManager();
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView index() {
		ModelAndView view = new ModelAndView("admin/index");
		return view;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView index_() {
		ModelAndView view = new ModelAndView("admin/index");
		return view;
	}

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public ModelAndView index__() {
		ModelAndView view = new ModelAndView("admin/index");
		return view;
	}

	@RequestMapping("/login")
	public ModelAndView login(HttpSession session,
			@RequestParam String loginName, @RequestParam String loginPwd) {
		log.info("admin login - loginname: " + loginName);

		AdminUserDao aud = ContextLoader.getAdminUserDao();

		if (aud.validateUser(loginName, loginPwd)) {
			AdminUserBean user = new AdminUserBean();
			user.setUserName(loginName);
			user.setPassword(loginPwd);
			session.setAttribute(AdminUserBean.SESSION_BEAN, user);

			ModelAndView view = new ModelAndView();
			view.setViewName("redirect:giftmanage");
			return view;
		} else {
			ModelAndView view = new ModelAndView();
			view.setViewName("index");
			view.addObject("LoginRetCode", HttpServletResponse.SC_FORBIDDEN);
			return view;
		}
	}

	@RequestMapping("/signout")
	public String signout(HttpSession session) {
		session.removeAttribute(AdminUserBean.SESSION_BEAN);
		return "redirect:/admin/";
	}

	@RequestMapping(value = "/giftmanage", method = RequestMethod.GET)
	public ModelAndView giftManage() {
		ModelAndView view = new ModelAndView("admin/giftmanage");
		view.addObject(WebConstants.page_name.name(), Pages.gift_manage.name());
		view.addObject(UUTalkConfigKeys.reg_gift_value.name(),
				ucm.getRegGiftValue());
		view.addObject(UUTalkConfigKeys.reg_gift_desc_text.name(),
				ucm.getRegGiftDescription());
		view.addObject(UUTalkConfigKeys.invite_charge_invite_desc_text.name(),
				ucm.getInviteChargeGiftDescription());
		return view;
	}

	@RequestMapping(value = "/giftmanage/editRegGiftValue", method = RequestMethod.POST)
	public void editRegGiftValue(HttpServletResponse response,
			@RequestParam String regGiftValue) throws IOException {
		if (ValidatePattern.isValidMoney(regGiftValue)) {
			ucm.setRegGiftValue(regGiftValue);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
	}

	@RequestMapping(value = "/giftmanage/editRegGiftDesc", method = RequestMethod.POST)
	public void editRegGiftDescription(HttpServletResponse response,
			@RequestParam String regGiftDesc) {
		ucm.setRegGiftDescription(regGiftDesc);
	}

	@RequestMapping(value = "/giftmanage/editInviteChargeGiftDesc")
	public void editInviteChargeGiftDescription(HttpServletResponse response,
			@RequestParam String inviteChargeGiftDesc) {
		log.info("editInviteChargeGiftDescription - desc: "
				+ inviteChargeGiftDesc);
		ucm.setInviteChargeGiftDescription(inviteChargeGiftDesc);
	}

	@RequestMapping(value = "/chargemanage", method = RequestMethod.GET)
	public ModelAndView chargeManage() {
		ModelAndView view = new ModelAndView("admin/chargemanage");
		view.addObject(WebConstants.page_name.name(),
				Pages.charge_manage.name());
		view.addObject("charge_money_list", ContextLoader
				.getChargeMoneyConfigDao().getChargeMoneyList());
		return view;
	}

	@RequestMapping(value = "/chargemanage/addChargeMoney", method = RequestMethod.POST)
	public void addChargeMoney(HttpServletResponse response,
			@RequestParam(value = "charge_money") String chargeMoney,
			@RequestParam(value = "gift_money") String giftMoney,
			@RequestParam(value = "description") String description)
			throws JSONException, IOException {
		JSONObject ret = new JSONObject();
		if (!ValidatePattern.isValidMoney(chargeMoney)) {
			ret.put("result", "invalid_charge_money");
			response.getWriter().print(ret.toString());
			return;
		}

		if (!ValidatePattern.isValidMoney(giftMoney)) {
			ret.put("result", "invalid_gift_money");
			response.getWriter().print(ret.toString());
			return;
		}

		ChargeMoneyConfigDao cmcd = ContextLoader.getChargeMoneyConfigDao();
		cmcd.addChargeMoney(chargeMoney, giftMoney, description);
		ret.put("result", "ok");
		response.getWriter().print(ret.toString());
	}

	@RequestMapping(value = "/chargemanage/editChargeMoney", method = RequestMethod.POST)
	public void editChargeMoney(HttpServletResponse response,
			@RequestParam(value = "id") String id,
			@RequestParam(value = "charge_money") String chargeMoney,
			@RequestParam(value = "gift_money") String giftMoney,
			@RequestParam(value = "description") String description)
			throws IOException, JSONException {
		JSONObject ret = new JSONObject();
		if (!ValidatePattern.isValidMoney(chargeMoney)) {
			ret.put("result", "invalid_charge_money");
			response.getWriter().print(ret.toString());
			return;
		}

		if (!ValidatePattern.isValidMoney(giftMoney)) {
			ret.put("result", "invalid_gift_money");
			response.getWriter().print(ret.toString());
			return;
		}

		ChargeMoneyConfigDao cmcd = ContextLoader.getChargeMoneyConfigDao();
		cmcd.editChargeMoney(id, chargeMoney, giftMoney, description);
		ret.put("result", "ok");
		response.getWriter().print(ret.toString());
	}

	@RequestMapping(value = "/chargemanage/deleteChargeMoney", method = RequestMethod.POST)
	public void deleteChargeMoney(HttpServletResponse response,
			@RequestParam(value = "id") String id) {
		ChargeMoneyConfigDao cmcd = ContextLoader.getChargeMoneyConfigDao();
		cmcd.deleteChargeMoney(id);
	}

	@RequestMapping(value = "/noticemanage", method = RequestMethod.GET)
	public ModelAndView noticeManage() {
		ModelAndView view = new ModelAndView("admin/noticemanage");
		view.addObject(WebConstants.page_name.name(),
				Pages.notice_manage.name());
		view.addObject("notices", ContextLoader.getNoticeDao().getNotices());
		return view;
	}

	@RequestMapping(value = "/noticemanage/addNotice", method = RequestMethod.POST)
	public void addNotice(HttpServletResponse response,
			@RequestParam(value = "content") String content) {
		ContextLoader.getNoticeDao().addNotice(content);
	}

	@RequestMapping(value = "/noticemanage/delNotice", method = RequestMethod.POST)
	public void deleteNotice(HttpServletResponse response,
			@RequestParam(value = "noticeId") String noticeId) {
		ContextLoader.getNoticeDao().hideNotice(Integer.parseInt(noticeId));
	}

	@RequestMapping(value = "/noticemanage/pubNotice", method = RequestMethod.POST)
	public void publishNotice(HttpServletResponse response,
			@RequestParam(value = "noticeId") String noticeId) {
		ContextLoader.getNoticeDao().publishNotice(Integer.parseInt(noticeId));
	}

	@RequestMapping(value = "/noticemanage/editNotice", method = RequestMethod.POST)
	public void editNotice(HttpServletResponse response,
			@RequestParam(value = "noticeId") String noticeId,
			@RequestParam(value = "content") String content) {
		ContextLoader.getNoticeDao().saveNotice(Integer.parseInt(noticeId),
				content);
	}

	@RequestMapping(value = "/registermanage", method = RequestMethod.GET)
	public ModelAndView registerManage() {
		ModelAndView view = new ModelAndView("admin/register_manage");
		view.addObject(WebConstants.page_name.name(),
				Pages.register_manage.name());
		view.addObject(UUTalkConfigKeys.default_register_money.name(),
				ucm.getDefaultRegisterMoney());
		view.addObject("register_activity", ContextLoader
				.getRegisterActivityDao().getRegisterActivity());
		view.addObject(UUTalkConfigKeys.ao_cb_register_money.name(), ucm.getAngolaCallbackVersionRegisterMoney());
		return view;
	}

	@RequestMapping(value = "/registermanage/editRegMoney", method = RequestMethod.POST)
	public void editRegMoney(HttpServletResponse response,
			@RequestParam String money) throws IOException {
		if (ValidatePattern.isValidMoney(money)) {
			ucm.setDefaultRegisterMoney(money);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
	}

	@RequestMapping(value = "/registermanage/closeRegisterActivity", method = RequestMethod.POST)
	public void closeRegisterActivity(HttpServletResponse response,
			@RequestParam String id) {
		ContextLoader.getRegisterActivityDao().closeActivity(
				Integer.parseInt(id));
	}

	@RequestMapping(value = "/registermanage/openRegisterActivity", method = RequestMethod.POST)
	public void openRegisterActivity(HttpServletResponse response,
			@RequestParam String id) {
		ContextLoader.getRegisterActivityDao().openActivity(
				Integer.parseInt(id));
	}

	@RequestMapping(value = "/registermanage/editRegisterActivity", method = RequestMethod.POST)
	public void editRegisterActivity(HttpServletResponse response,
			@RequestParam String id, @RequestParam String startDate,
			@RequestParam String endDate, @RequestParam String giftMoney) throws IOException {
		if (ValidatePattern.isValidMoney(giftMoney)) {
			ContextLoader.getRegisterActivityDao().editActivity(
					Integer.parseInt(id), startDate, endDate, giftMoney);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
	}
	
	@RequestMapping(value = "/registermanage/editAngolaCallbackRegisterMoney", method = RequestMethod.POST)
	public void editAngolaCallbackRegisterMoney(HttpServletResponse response,
			@RequestParam String money) throws IOException {
		if (ValidatePattern.isValidMoney(money)) {
			ucm.setAngolaCallbackVersionRegisterMoney(money);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
		}
	}
}
