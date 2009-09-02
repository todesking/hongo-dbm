/**
 * 
 */
package com.todesking.hongodbm.base;

import junit.framework.TestCase;

import com.todesking.hongodbm.ArrayStorage;
import com.todesking.hongodbm.base.Buckets;
import com.todesking.hongodbm.base.Storage;

import static com.todesking.hongodbm.TestHelper.*;

public class BucketsTest extends TestCase {
	public void test() throws Exception {
		final Storage storage = new ArrayStorage(0);
		storage.fill(0L, 1000, (byte) 0);
		final Buckets buckets = new Buckets(storage, 0L, 1000);
		buckets.initialize();
		assertEquals(1000, buckets.bucketsSize());
		assertEquals(-1, buckets.get(100));
		assertThrows(IndexOutOfBoundsException.class, buckets, "get", 1000);
		assertThrows(IndexOutOfBoundsException.class, buckets, "get", -1);
		buckets.set(999, 100);

		assertThrows(IndexOutOfBoundsException.class, buckets, "set", 1000, 10L);
		assertThrows(IndexOutOfBoundsException.class, buckets, "set", -1, 10L);
		buckets.set(100, 1234);
		assertEquals(1234, buckets.get(100));
	}
}