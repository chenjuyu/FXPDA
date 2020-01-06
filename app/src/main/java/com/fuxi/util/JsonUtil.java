package com.fuxi.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Json解析工具类
 */
public class JsonUtil {
	private static Gson gson = new Gson();

	@SuppressWarnings("hiding")
	public static <T> T parseJson(String response, Class<T> clazz) {
		try {
			return gson.fromJson(response, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T parseJson(String response, Type type) {
		try {
			return gson.fromJson(response, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String toJson(Object object) {
		try {
			return gson.toJson(object);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}