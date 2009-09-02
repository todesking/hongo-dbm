package com.todesking.hongodbm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.AssertionFailedError;

public class TestHelper {
	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		if (!Arrays.equals(expected, actual))
			throw new AssertionFailedError(
				"two arrays are different. expected "
					+ Arrays.toString(expected)
					+ " but "
					+ Arrays.toString(actual));
	}

	public static byte[] ba(int... array) {
		final byte[] result = new byte[array.length];
		for (int i = 0; i < result.length; i++)
			result[i] = (byte) array[i];
		return result;
	}

	public static byte[] bin(int n) {
		return BinaryUtils.encodeInt(n);
	}

	public static byte[] bin(String s) {
		return BinaryUtils.encodeString(s);
	}

	public static byte[] bin(long l) {
		return BinaryUtils.encodeLong(l);
	}

	public static void assertThrows(Class<? extends Throwable> expected,
			Class<?> targetClass, String methodName, Object... args)
			throws Exception {
		assertThrows(expected, null, findMethod(
			targetClass,
			methodName,
			getTypeArray(args)), args);

	}

	public static void assertThrows(Class<? extends Throwable> expected,
			Object obj, String methodName, Object... args) throws Exception {
		assertThrows(expected, obj, findMethod(
			obj.getClass(),
			methodName,
			getTypeArray(args)), args);

	}

	private static void assertThrows(Class<? extends Throwable> expected,
			Object target, Method method, Object[] args) throws Exception {
		try {
			method.invoke(target, args);
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) {
				final Throwable actual =
					((InvocationTargetException) e).getTargetException();
				if (actual.getClass() == expected)
					return; // success
			}
			throw e;
		}
	}

	private static Class<?>[] getTypeArray(Object... values) {
		final Class<?>[] paramTypes = new Class<?>[values.length];
		for (int i = 0; i < paramTypes.length; i++)
			paramTypes[i] = values[i].getClass();
		return paramTypes;
	}

	private static Method findMethod(Class<?> klass, String methodName,
			Class<?>[] argTypes) throws NoSuchMethodException {
		try {
			return klass.getMethod(methodName, argTypes);
		} catch (NoSuchMethodException noSuchMethod) {
			final Method[] methods = klass.getMethods();
			find_method: for (Method m : methods) {
				if (!m.getName().equals(methodName))
					continue find_method;
				final Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes.length != argTypes.length)
					continue find_method;
				for (int i = 0; i < paramTypes.length; i++) {
					if (!isSameType(paramTypes[i], argTypes[i]))
						continue find_method;
				}
				return m;
			}
			throw noSuchMethod;
		}
	}

	private static boolean isSameType(Class<?> type,
			Class<?> type_maybe_boxed_or_sub) {
		// exactry same
		if (type == type_maybe_boxed_or_sub)
			return true;

		// subtype
		if (type.isAssignableFrom(type_maybe_boxed_or_sub))
			return true;

		// array
		if (type.isArray()) {
			if (type_maybe_boxed_or_sub.isArray())
				return isSameType(
					type.getComponentType(),
					type_maybe_boxed_or_sub.getComponentType());
			else
				return false;
		}

		// primitive
		if (!type.isPrimitive())
			return false;
		if (type == Byte.TYPE && type_maybe_boxed_or_sub == Byte.class)
			return true;
		if (type == Short.TYPE && type_maybe_boxed_or_sub == Short.class)
			return true;
		if (type == Integer.TYPE && type_maybe_boxed_or_sub == Integer.class)
			return true;
		if (type == Long.TYPE && type_maybe_boxed_or_sub == Long.class)
			return true;

		if (type == Boolean.TYPE && type_maybe_boxed_or_sub == Boolean.class)
			return true;

		if (type == Character.TYPE
			&& type_maybe_boxed_or_sub == Character.class)
			return true;

		if (type == Float.TYPE && type_maybe_boxed_or_sub == Float.class)
			return true;
		if (type == Double.TYPE && type_maybe_boxed_or_sub == Double.class)
			return true;

		return false;
	}

	public static abstract class AssertThrows {
		public AssertThrows(Class<? extends Throwable> expected) {
			try {
				proc();
			} catch (Throwable e) {
				if (e.getClass() == expected) {
					return; // success
				} else {
					junit.framework.Assert.fail("expected "
						+ expected.getName()
						+ " but throws "
						+ e.getClass().getName());
					return;
				}
			}
			junit.framework.Assert.fail("expected "
				+ expected.getName()
				+ " but nothing");
		}

		protected abstract void proc() throws Exception;
	}
}
