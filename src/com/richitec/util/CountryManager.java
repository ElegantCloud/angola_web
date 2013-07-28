package com.richitec.util;

import java.util.HashMap;
import java.util.Map;

public class CountryManager {

	private static CountryManager instance;

	private Map<String, String> countryMap;
	private static String[] COUNTRY_CODES = { "0086", "086", "+86", "86",
			"00244", "0244", "+244", "244", "0057", "057", "+57", "57", "00852", "0852", "+852", "852",
			"0066", "066", "+66", "66"};

	private CountryManager() {
		countryMap = new HashMap<String, String>();
		init();
	}

	private void init() {
		countryMap.put("0057", "co");
		countryMap.put("0086", "cn");
		countryMap.put("00244", "ao");
		countryMap.put("244", "ao");
		countryMap.put("00852", "hk");
		countryMap.put("0066", "th");
	}

	public synchronized static CountryManager getInstance() {
		if (instance == null) {
			instance = new CountryManager();
		}
		return instance;
	}

	public String getCountryABBR(String code) {
		return countryMap.get(code);
	}
	
	public boolean hasCountryCodePrefix(String phoneNumber) {
		boolean ret = false;
		for (String code : COUNTRY_CODES) {
			if (phoneNumber.startsWith(code)) {
				ret = true;
				break;
			}
		}
		
		return ret;
	}
}
