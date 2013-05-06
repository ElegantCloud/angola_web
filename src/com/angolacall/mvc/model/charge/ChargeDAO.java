package com.angolacall.mvc.model.charge;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.angolacall.constants.ChargeStatus;

public class ChargeDAO {
	private static Log log = LogFactory.getLog(ChargeDAO.class);
	private JdbcTemplate jdbc;

	public void setDataSource(DataSource ds) {
		jdbc = new JdbcTemplate(ds);
	}

	public int getChargeListTotalCount(String countryCode, String userName)
			throws DataAccessException {
		String sql = "SELECT count(*) FROM im_charge_history WHERE username = ? AND status = ? ORDER BY time DESC";
		return jdbc.queryForInt(sql, userName, ChargeStatus.success.name());
	}

	public List<Map<String, Object>> getChargeList(String countryCode,
			String userName, int offset, int pageSize) {
		String sql = "SELECT chargeId, money, DATE_FORMAT(time, '%Y-%m-%d %H:%i') as charge_time "
				+ "FROM im_charge_history WHERE username = ? AND status = ? "
				+ "ORDER BY time DESC LIMIT ?, ?";
		int startIndex = (offset - 1) * pageSize;
		List<Map<String, Object>> list = null;
		try {
			list = jdbc.queryForList(sql, userName,
					ChargeStatus.success.name(), startIndex, pageSize);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void addChargeRecord(String chargeId, String countryCode,
			String userName, Double money, ChargeStatus status) {
		String sql = "INSERT INTO im_charge_history(chargeId, username, money, status, countrycode) VALUES(?, ?, ?, ?, ?)";
		jdbc.update(sql, chargeId, userName, money, status.name(), countryCode);
	}

	public void addChargeRecord(String chargeId, String countryCode,
			String userName, Double money, ChargeStatus status,
			String chargeMoneyId) {
		String sql = "INSERT INTO im_charge_history(chargeId, username, money, status, countrycode, charge_money_cfg_id) VALUES(?, ?, ?, ?, ?, ?)";
		jdbc.update(sql, chargeId, userName, money, status.name(), countryCode,
				chargeMoneyId);
	}

	public void addChargeRecord(String chargeId, String countryCode,
			String userName, Double money) {
		String sql = "INSERT INTO im_charge_history(chargeId, username, money, countrycode) VALUES(?, ?, ?, ?)";
		jdbc.update(sql, chargeId, userName, money, countryCode);
	}

	public void addChargeRecord(String chargeId, String countryCode,
			String userName, Double money, String contributorCountryCode,
			String contributor, ChargeStatus status) {
		String sql = "INSERT INTO im_charge_history(chargeId, username, money, countrycode, status, contributor_country_code, contributor) VALUES(?, ?, ?, ?, ? ,?, ?)";
		jdbc.update(sql, chargeId, userName, money, countryCode, status.name(),
				contributorCountryCode, contributor);
	}

	public void updateChargeRecord(String chargeId, ChargeStatus status) {
		String sql = "UPDATE im_charge_history SET status = ? WHERE chargeId = ?";
		jdbc.update(sql, status.name(), chargeId);
	}

	public Map<String, Object> getChargeInfoById(String chargeId) {
		String sql = "SELECT * FROM im_charge_history WHERE chargeId = ?";
		Map<String, Object> info = null;
		try {
			info = jdbc.queryForMap(sql, chargeId);
		} catch (DataAccessException e) {
			log.info(e.getMessage());
		}
		return info;
	}

	public Double getGiftMoney(String chargeId) {
		String sql = "SELECT cmc.gift_money FROM im_charge_history AS ich, charge_money_config AS cmc "
				+ "WHERE ich.chargeId = ? AND ich.charge_money_cfg_id = cmc.id AND ich.money = cmc.charge_money ";
		try {
			Float giftMoney = jdbc.queryForObject(sql,
					new Object[] { chargeId }, Float.class);
			return giftMoney.doubleValue();
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		return null;
	}

	public Double getDayAdClickGiftMoneyPerUser(String userName,
			String countryCode) {
		String sql = "SELECT SUM(money) FROM im_charge_history WHERE username = ? AND countrycode = ? AND status = ? AND DATE(time) = CURDATE()";
		Double money = 0.0;
		try {
			money = jdbc.queryForObject(sql, new Object[] { userName,
					countryCode, ChargeStatus.success.name() }, Double.class);
		} catch (Exception e) {
			log.info("getDayAdClickGiftMoneyPerUser: " + e.getMessage());
		}
		if (money == null) {
			money = 0.0;
		}
		return money;
	}
	
	public Long getAdChargeLastTimeByUser(String userName, String countryCode) {
		String sql = "SELECT UNIX_TIMESTAMP(time) AS create_time FROM im_charge_history  WHERE username = ? AND countrycode = ? AND chargeId LIKE 'adclick%' AND status = 'success' ORDER BY time DESC LIMIT 1";
		Long time = 0l;
		try {
			time = jdbc.queryForLong(sql, userName, countryCode);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (time == null) {
			time = 0l;
		}
		return time;
	}
}