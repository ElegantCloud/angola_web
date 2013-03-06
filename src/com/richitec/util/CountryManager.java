package com.richitec.util;

import java.util.HashMap;
import java.util.Map;

public class CountryManager {

	private static CountryManager instance;

	private Map<String, String> countryMap;
	private static String[] COUNTRY_CODES = { "0086", "086", "+86", "86",
			"00244", "0244", "+244", "244" };

	private CountryManager() {
		countryMap = new HashMap<String, String>();
		init();
	}

	private void init() {
		countryMap.put("86", "cn");
		countryMap.put("244", "ao");
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
