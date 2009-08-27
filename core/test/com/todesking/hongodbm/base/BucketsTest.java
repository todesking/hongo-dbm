/**
 * 
 */
package com.todesking.hongodbm.base;

import junit.framework.TestCase;

import com.todesking.hongodbm.ArrayStorage;
import com.todesking.hongodbm.TestHelper.AssertThrows;
import com.todesking.hongodbm.base.Buckets;
import com.todesking.hongodbm.base.Storage;

public class BucketsTest extends TestCase {
	public void test() {
		final Storage storage = new ArrayStorage(0);
		storage.fill(0L, 1000, (byte) 0);
		final Buckets buckets = new Buckets(storage, 0L, 1000);
		buckets.initialize();
		assertEquals(1000, buckets.bucketsSize());
		assertEquals(-1, buckets.get(100));
		try {
			buckets.get(1000);
			fail();
		} catch (IndexOutOfBoundsException e) {
		}
		buckets.set(999, 100);
		new AssertThrows(IndexOutOfBoundsException.class) {
			@Override
			protected void proc() throws Exception {
				buckets.set(1000, 10);
			}
		};
		buckets.set(100, 1234);
		assertEquals(1234, buckets.get(100));
	}
}