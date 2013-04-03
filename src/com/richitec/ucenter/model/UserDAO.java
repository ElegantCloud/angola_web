package com.richitec.ucenter.model;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.angolacall.constants.EmailStatus;
import com.angolacall.constants.UserAccountStatus;
import com.angolacall.framework.Configuration;
import com.angolacall.framework.ContextLoader;
import com.angolacall.web.user.UserBean;
import com.richitec.sms.client.SMSHttpResponse;
import com.richitec.util.CountryManager;
import com.richitec.util.CryptoUtil;
import com.richitec.util.RandomString;
import com.richitec.util.ValidatePattern;

public class UserDAO {
	private static Log log = LogFactory.getLog(UserDAO.class);

	private JdbcTemplate jdbc;

	public void setDataSource(DataSource ds) {
		jdbc = new JdbcTemplate(ds);
	}

	/**
	 * 获得手机验证码
	 * 
	 * @param session
	 * @param phone
	 * @param phoneCode
	 * @param countryCode
	 * @return
	 */
	public String getPhoneCode(HttpSession session, String phone,
			String countryCode) {
		String result = "0";
		String phoneCode = RandomString.validateCode();
		log.info("phone code: " + phoneCode);
		try {
			session.setAttribute("phonenumber", phone);
			session.setAttribute("phonecode", phoneCode);
			session.setAttribute("countrycode", countryCode);
			String content = "验证码：" + phoneCode + " [悠聊]";
			SMSHttpResponse response = ContextLoader.getSMSClient()
					.sendTextMessage(phone, content);
			log.info("sms return: " + response.getCode());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 注册
	 * 
	 * @param session
	 * @param phone
	 * @param password
	 * @param password1
	 * @param code
	 * @return
	 */
	public String regUser(String countryCode, String phone,
			String referrerCountryCode, String referrer, String password,
			String password1, String source) {
		String result = checkRegisterUser(countryCode, phone, password,
				password1);
		log.info("checkRegisterUser - result: " + result);
		if (result.equals("0")) {
			String userkey = CryptoUtil.md5(phone + password);
			String vosPhonePwd = RandomString.genRandomNum(6);
			String sql = "INSERT INTO im_user(username, password, userkey, referrer_country_code, referrer, countrycode, bindphone, bindphone_country_code, vosphone_pwd, source) VALUES (?,?,?,?,?,?,?,?,?,?)";
			Object[] params = new Object[] { phone, CryptoUtil.md5(password),
					userkey, referrerCountryCode, referrer, countryCode, phone,
					countryCode, vosPhonePwd, source };
			int resultCount = jdbc.update(sql, params);
			result = resultCount > 0 ? "0" : "1001";
		}
		return result;
	}

	public UserBean getUserBean(String countryCode, String loginName,
			final String loginPwd) throws DataAccessException {
		String sql = "SELECT * FROM im_user WHERE username=? AND countrycode=? AND password=? AND status =?";
		Object[] params = new Object[] { loginName, countryCode, loginPwd,
				UserAccountStatus.success.name() };
		return jdbc.queryForObject(sql, params, new RowMapper<UserBean>() {
			@Override
			public UserBean mapRow(ResultSet rs, int rowCount)
					throws SQLException {
				UserBean user = new UserBean();
				user.setReferrer(rs.getString("referrer"));
				user.setReferrerCountryCode(rs
						.getString("referrer_country_code"));
				user.setUserKey(rs.getString("userkey"));
				user.setCountryCode(rs.getString("countrycode"));
				user.setVosPhone(String.valueOf(rs.getLong("vosphone")));
				user.setVosPhonePwd(rs.getString("vosphone_pwd"));
				user.setBindPhone(rs.getString("bindphone"));
				user.setBindPhoneCountryCode(rs
						.getString("bindphone_country_code"));
				user.setStatus(rs.getString("status"));
				user.setEmail(rs.getString("email"));
				user.setEmailStatus(rs.getString("email_status"));
				user.setFrozenMoney(rs.getFloat("frozen_money"));
				return user;
			}
		});
	}

	public Map<String, Object> getUser(String countryCode, String userName) {
		String sql = "SELECT * FROM im_user WHERE username = ? AND countrycode = ?";
		return jdbc.queryForMap(sql, userName, countryCode);
	}

	public UserBean getUserBean(String countryCode, String userName)
			throws DataAccessException {
		String sql = "SELECT * FROM im_user WHERE username=? AND countrycode=?";
		Object[] params = new Object[] { userName, countryCode };
		return jdbc.queryForObject(sql, params, new RowMapper<UserBean>() {
			@Override
			public UserBean mapRow(ResultSet rs, int rowCount)
					throws SQLException {
				UserBean user = new UserBean();
				user.setReferrer(rs.getString("referrer"));
				user.setReferrerCountryCode(rs
						.getString("referrer_country_code"));
				user.setUserKey(rs.getString("userkey"));
				user.setCountryCode(rs.getString("countrycode"));
				user.setVosPhone(String.valueOf(rs.getLong("vosphone")));
				user.setVosPhonePwd(rs.getString("vosphone_pwd"));
				user.setBindPhone(rs.getString("bindphone"));
				user.setBindPhoneCountryCode(rs
						.getString("bindphone_country_code"));
				user.setStatus(rs.getString("status"));
				user.setEmail(rs.getString("email"));
				user.setEmailStatus(rs.getString("email_status"));
				user.setFrozenMoney(rs.getFloat("frozen_money"));
				return user;
			}
		});
	}

	/**
	 * record the device info of login user
	 * 
	 * @param username
	 * @param brand
	 * @param model
	 * @param release
	 * @param sdk
	 * @param width
	 * @param height
	 */
	public void recordDeviceInfo(String username, String countryCode,
			String brand, String model, String release, String sdk,
			String width, String height) {
		String sql = "SELECT count(*) FROM device_info WHERE username = ? AND countrycode = ?";
		int count = jdbc.queryForInt(sql, username, countryCode);
		if (count > 0) {
			jdbc.update(
					"UPDATE device_info SET brand=?, model=?, "
							+ "release_ver=?, sdk=?, width=?, height=? WHERE username = ? AND countrycode = ?",
					brand, model, release, sdk, width, height, username,
					countryCode);
		} else {
			jdbc.update("INSERT INTO device_info VALUE(?,?,?,?,?,?,?,?)",
					username, countryCode, brand, model, release, sdk, width,
					height);
		}
	}

	public String checkPhoneCode(HttpSession session, String code) {
		if (code.equals("")) {
			return "1"; // code is required
		} else if (!code.equals(session.getAttribute("phonecode"))) {
			return "2"; // error code
		} else {
			return "0";
		}
	}

	/**
	 * 判断用户注册信息是否正确
	 * 
	 * @param session
	 * @param phone
	 * @param password
	 * @param password1
	 * @param code
	 * @return
	 */
	public String checkRegisterUser(String countryCode, String phone,
			String password, String password1) {
		try {
			if (phone.equals("")) {
				return "1"; // 手机号码必填
			} else if (!ValidatePattern.isNumber(phone)) {
				return "2"; // 手机号码格式错误
			} else if (isExistsLoginName(countryCode, phone)) {
				return "3"; // 手机号码已存�?
			} else if (password.equals("")) {
				return "4"; // 密码必填
			} else if (!password.equals(password1)) {
				return "5"; // 两次密码输入不一�?
			} else if (CountryManager.getInstance().hasCountryCodePrefix(phone)) {
				return "7";
			} else {
				return "0";
			}
		} catch (Exception e) {
			return "1001";
		}

	}

	/**
	 * 判断手机号码是否正确
	 * 
	 * @param phone
	 * @return
	 */
	public String checkRegisterPhone(String countryCode, String phone) {
		try {
			if (phone.equals("")) {
				return "1"; // 手机号码必填
			} else if (!ValidatePattern.isNumber(phone)) {
				return "2"; // 手机号码格式错误
			} else if (isExistsLoginName(countryCode, phone)) {
				return "3"; // 手机号码已存�?
			} else {
				return "0";
			}
		} catch (Exception e) {
			return "1001";
		}
	}

	/**
	 * 判断该用户名是否存在
	 * 
	 * @param loginName
	 * @return
	 * @throws SQLException
	 */
	public boolean isExistsLoginName(String countryCode, String loginName)
			throws SQLException {
		String sql = "SELECT count(username) FROM im_user WHERE username = ? AND countrycode = ?";
		Object[] params = new Object[] { loginName, countryCode };
		return jdbc.queryForInt(sql, params) > 0;
	}

	/**
	 * 获得userkey
	 * 
	 * @param username
	 * @return
	 */
	public String getUserKey(String countryCode, String username) {
		String sql = "SELECT userkey FROM im_user WHERE username = ? AND countrycode = ?";
		Object[] params = new Object[] { username, countryCode };
		return jdbc.queryForObject(sql, params, String.class);
	}

	public Map<String, Object> getVOSPhone(String countryCode, String username) {
		String sql = "SELECT vosphone, vosphone_pwd FROM im_user WHERE username = ? AND countrycode = ?";
		return jdbc.queryForMap(sql, username, countryCode);
	}

	public int changePassword(String userName, String md5Password,
			String countryCode) {
		String sql = "UPDATE im_user SET password=?, userkey=? WHERE username=? AND countrycode=?";
		String userkey = CryptoUtil.md5(RandomString.genRandomChars(10));
		return jdbc.update(sql, md5Password, userkey, userName, countryCode);
	}

	public int updateUserAccountStatus(String countryCode, String userName,
			UserAccountStatus status) {
		String sql = "UPDATE im_user SET status = ? WHERE username = ? AND countrycode=?";
		return jdbc.update(sql, status.name(), userName, countryCode);
	}

	public int setBindPhone(String countryCode, String userName,
			String bindPhoneCountryCode, String bindPhone) {
		String sql = "UPDATE im_user SET bindphone = ?, bindphone_country_code = ? WHERE countrycode = ? AND username = ?";
		return jdbc.update(sql, bindPhone, bindPhoneCountryCode, countryCode,
				userName);
	}

	public int getRegedUserCountViaShare(String countryCode, String userName) {
		String sql = "SELECT count(*) FROM im_user WHERE referrer = ? AND referrer_country_code = ?";
		return jdbc.queryForInt(sql, userName, countryCode);
	}

	public int updateEmailStatus(String countryCode, String userName,
			EmailStatus emailStatus) {
		String sql = "UPDATE im_user SET email_status = ? WHERE countrycode = ? AND username = ?";
		return jdbc.update(sql, emailStatus.name(), countryCode, userName);
	}

	public int setEmail(String countryCode, String userName, String email) {
		String randomId = RandomString.getRandomId(countryCode + userName
				+ email);

		String sql = "UPDATE im_user SET email = ?, email_status = ?, random_id = ? WHERE countrycode = ? AND username = ?";
		int rows = jdbc.update(sql, email, EmailStatus.unverify.name(),
				randomId, countryCode, userName);
		return rows;
	}

	public boolean isEmailBinded(String email) {
		String sql = "SELECT count(*) FROM im_user WHERE email = ?";
		int count = jdbc.queryForInt(sql, email);
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	public Map<String, Object> getUserByRandomId(String randomId) {
		String sql = "SELECT * FROM im_user WHERE random_id = ?";
		return jdbc.queryForMap(sql, randomId);
	}

	public void addFrozenMoney(String countryCode, String userName, Double money) {
		String sql = "UPDATE im_user SET frozen_money = frozen_money + ? WHERE countrycode = ? AND username = ?";
		jdbc.update(sql, money, countryCode, userName);
	}

	public void setFrozenMoney(String countryCode, String userName, Double money) {
		String sql = "UPDATE im_user SET frozen_money = ? WHERE countrycode = ? AND username = ?";
		jdbc.update(sql, money, countryCode, userName);
	}

	public void clearFrozenMoney(String countryCode, String userName) {
		setFrozenMoney(countryCode, userName, 0.0);
	}
	
	public int updateRandomId(String countryCode, String userName, String randomId) {
		String sql = "UPDATE im_user SET random_id = ? WHERE countrycode = ? AND username = ?";
		int rows = jdbc.update(sql, randomId, countryCode, userName);
		return rows;
	}
	
	public static String genVosAccountName(String countryCode, String userName, String source) {
		Configuration config = ContextLoader.getConfiguration();
		return config.getAppPrefix() + source + "_" + countryCode + userName;
	}
	
	public String genVosAccountName(String countryCode, String userName) {
		Map<String, Object> user = getUser(countryCode, userName);
		String source = (String) user.get("source");
		return genVosAccountName(countryCode, userName, source);
	}
}
