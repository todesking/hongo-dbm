/**
 * 
 */
package com.todesking.hongodbm;

import static com.todesking.hongodbm.TestHelper.assertArrayEquals;
import static com.todesking.hongodbm.TestHelper.assertThrows;
import static com.todesking.hongodbm.TestHelper.ba;
import junit.framework.TestCase;

public class ArrayStorageTest extends TestCase {
	public void test() throws Exception {
		final ArrayStorage storage = new ArrayStorage(1);
		assertThrows(IndexOutOfBoundsException.class, storage, "read", -1L, 1);
		assertThrows(IndexOutOfBoundsException.class, storage, "read", 0L, 4);
		assertThrows(
			IndexOutOfBoundsException.class,
			storage,
			"write",
			0x7FFFFFFFFL,
			ba(10));
		storage.write(0, BinaryUtils.encodeInt(10));
		assertArrayEquals(BinaryUtils.encodeInt(10), storage.read(0L, 4));

		storage.fill(0L, 8, (byte) 0);
		assertArrayEquals(ba(0, 0, 0, 0, 0, 0, 0, 0), storage.read(0L, 8));
	}

	public void testClose() throws Exception {
		final ArrayStorage storage = new ArrayStorage(1);
		storage.close();
		assertThrows(NullPointerException.class, storage, "close");
	}

	public void testFlush() {
		final ArrayStorage storage = new ArrayStorage(1);
		storage.flush();
	}

	public void testFillShouldExtendStorage() {
		final ArrayStorage storage = new ArrayStorage(0);
		storage.fill(0L, 10, (byte) 0xFF);
		assertArrayEquals(BinaryUtils.encodeInt(0xFFFFFFFF), storage
			.read(0L, 4));
	}
}