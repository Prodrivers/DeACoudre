package me.poutineqc.deacoudre.tools;

import java.util.HashMap;

public class CaseInsensitiveMap extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2603835328506619000L;

	@Override
    public String put(String key, String value) {
       return super.put(key.toLowerCase(), value);
    }

    // not @Override because that would require the key parameter to be of type Object
    public String get(String key) {
       return super.get(key.toLowerCase());
    }
}