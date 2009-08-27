package com.todesking.hongodbm;

import junit.framework.TestCase;

import static com.todesking.hongodbm.TestHelper.*;

public class BinaryUtilsTest extends TestCase {
	public void testInt() {
		checkInt(0);
		checkInt(100);
		checkInt(-100);
		checkInt(0xFFFFFFFF);
		checkInt(0x12345678);

		BinaryUtils.decodeInt(ba(0, 1, 2, 3));
		new AssertThrows(IllegalArgumentException.class) {
			@Override
			protected void proc() throws Exception {
				BinaryUtils.decodeInt(ba(0, 1, 2));
			}
		};
	}

	public void testLong() {
		checkLong(0);
		checkLong(100);
		checkLong(-1000000000L);
		checkLong(0xFFFFFFFFFFFFFFFFL);
		checkLong(0x1234567890ABCDEFL);

		BinaryUtils.decodeLong(ba(0, 1, 2, 3, 4, 5, 6, 7));
		new AssertThrows(IllegalArgumentException.class) {
			@Override
			protected void proc() throws Exception {
				BinaryUtils.decodeLong(ba(0, 1, 2, 3, 4, 5, 6, 7, 8));
			}
		};
	}

	public void testString() {
		checkString("");
		checkString("hoge hage");
		checkString("日本語");
	}

	public void checkInt(int value) {
		assertEquals(value, BinaryUtils.decodeInt(BinaryUtils.encodeInt(value)));
	}

	public void checkLong(long value) {
		assertEquals(value, BinaryUtils.decodeLong(BinaryUtils
			.encodeLong(value)));
	}

	public void checkString(String value) {
		assertEquals(value, BinaryUtils.decodeString(BinaryUtils
			.encodeString(value)));
	}

	public void testCompare() {
		assertEquals(-1, BinaryUtils.compare(ba(1, 2, 3), ba(2, 3, 4)));
		assertEquals(-1, BinaryUtils.compare(ba(1, 2), ba(1, 2, 3)));
		assertEquals(0, BinaryUtils.compare(ba(), ba()));
		assertEquals(0, BinaryUtils.compare(ba(1, 2, 3), ba(1, 2, 3)));
		assertEquals(1, BinaryUtils.compare(ba(2, 3, 4), ba(1, 2, 3)));
		assertEquals(1, BinaryUtils.compare(ba(1, 2, 3), ba(1, 2)));
	}
}
