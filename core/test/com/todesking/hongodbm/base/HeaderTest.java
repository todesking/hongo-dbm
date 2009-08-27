/**
 * 
 */
package com.todesking.hongodbm.base;

import static com.todesking.hongodbm.TestHelper.assertArrayEquals;
import junit.framework.TestCase;

import com.todesking.hongodbm.ArrayStorage;
import com.todesking.hongodbm.BinaryUtils;
import com.todesking.hongodbm.base.Header;

public class HeaderTest extends TestCase {
	public void testHeader() {
		final ArrayStorage storage = new ArrayStorage(0);
		final Header header = new Header(storage, 0);
		header.initialize(16, 1000);
		assertArrayEquals(BinaryUtils.encodeInt(0xDEADBEAF), storage
			.read(0L, 4));
		assertEquals(16, header.blockSize());
		assertEquals(1000, header.bucketsSize());
	}
}