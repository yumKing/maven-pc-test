package org.jin.httpclient.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtils {

	public static Class<?> getClass(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>) type;
		}

		if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		}

		return Object.class;
	}

	public static Class<?> getWapperClass(Class<?> clazz) {
		if (byte.class.equals(clazz)) {
			return Byte.class;
		} else if (boolean.class.equals(clazz)) {
			return Boolean.class;
		} else if (int.class.equals(clazz)) {
			return Integer.class;
		} else if (long.class.equals(clazz)) {
			return Long.class;
		} else if (short.class.equals(clazz)) {
			return Short.class;
		} else if (float.class.equals(clazz)) {
			return Float.class;
		} else if (double.class.equals(clazz)) {
			return Double.class;
		} else if (long.class.equals(clazz)) {
			return Long.class;
		} else if (void.class.equals(clazz)) {
			return Void.class;
		} else if (char.class.equals(clazz)) {
			return Character.class;
		}
		return clazz;
	}
	
	
}
