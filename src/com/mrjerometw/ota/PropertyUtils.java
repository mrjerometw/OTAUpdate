package com.mrjerometw.ota;

import java.lang.reflect.Method;

public class PropertyUtils {
	
	private static Method set = null;
	private static Method get = null;
	
	public static void set(String prop, String value) {

		try {
			if (set == null) {
				Class<?> cls = Class.forName("android.os.SystemProperties");
				set = cls.getDeclaredMethod("set", new Class<?>[] {String.class, String.class});
			}
			set.invoke(null, new Object[] {prop, value});
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	public static String get(String prop, String defaultvalue) {
		String value = defaultvalue;
		try {
			if (get == null) {
				Class<?> cls = Class.forName("android.os.SystemProperties");
				get = cls.getDeclaredMethod("get", new Class<?>[] {String.class, String.class});
			}
			value =  (String)(get.invoke(null ,new Object[] {prop, defaultvalue}));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return value;
	}
}
