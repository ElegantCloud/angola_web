package com.angolacall.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.angolacall.mvc.controller.model.share.ShareAssistance;

@Controller
@RequestMapping("/share_assist")
public class ShareAssisController {
	private static Log log = LogFactory.getLog(ShareAssisController.class);

	@RequestMapping("/share_to_qzone")
	public ModelAndView shareToQzone(
			HttpSession session,
			HttpServletRequest request,
			@RequestParam(value = "title", defaultValue = "") String title,
			@RequestParam(value = "url", defaultValue = "") String url,
			@RequestParam(value = "summary", defaultValue = "") String summary,
			@RequestParam(value = "images", defaultValue = "") String images,
			@RequestParam(value = "access_token", required = false) String access_token) {

		log.info("summary: " + summary);
		log.info("access_token: " + access_token);

		String openId = "";
		
		if (!"".equals(title)) {
			session.setAttribute("title", title);
		}
		if (!"".equals(url)) {
			session.setAttribute("url", url);
		}
		if (!"".equals(summary)) {
			session.setAttribute("summary", summary);
		}
		if (!"".equals(images)) {
			session.setAttribute("images", images);
		}

		if (access_token != null && !access_token.equals("")) {
			session.setAttribute("access_token", access_token);
			openId = ShareAssistance.getQQUserOpenID(access_token);
			log.info("openid: " + openId);
			if (openId != null && !"".equals(openId)) {
				session.setAttribute("openid", openId);
			}
		}

		String accessToken = (String) session.getAttribute("access_token");

		ModelAndView view = new ModelAndView();
		if (accessToken == null || accessToken.equals("") || "".equals(openId)) {
			view.setViewName("share_assist/qq_login");
		} else {
			int retCode = ShareAssistance.shareToQzone(session);
			view.setViewName("share_assist/sharetoqzone_result");
			view.addObject("ret_code", new Integer(retCode));
		}
		return view;
	}

	@RequestMapping("/process_qq_redirect_url")
	public ModelAndView processQQRedirectUrl(HttpSession session) {
		String title = (String) session.getAttribute("title");
		log.info("processQQRedirectUrl - title: " + title);
		
		
		ModelAndView view = new ModelAndView(
				"share_assist/qqredirecturlprocess");
		return view;
	}

}
