package com.todesking.hongodbm;

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
			Object obj, String methodName, Object... args) throws Exception {
		final Class<?>[] paramTypes = new Class<?>[args.length];
		for (int i = 0; i < paramTypes.length; i++)
			paramTypes[i] = args[i].getClass();

		final Method m = obj.getClass().getMethod(methodName, paramTypes);
		try {
			m.invoke(obj, args);
		} catch (Exception e) {
			if (e.getClass() != expected)
				throw e;
		}
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
