/**
 * 
 */
package com.todesking.hongodbm.base;

import static com.todesking.hongodbm.TestHelper.assertArrayEquals;
import junit.framework.TestCase;

import com.todesking.hongodbm.ArrayStorage;
import com.todesking.hongodbm.BinaryUtils;
import com.todesking.hongodbm.base.Data;
import com.todesking.hongodbm.base.Storage;

public class DataTest extends TestCase {
	public void test() {
		final Storage storage = new ArrayStorage(0);
		final Data data = new Data(storage, 0L, 1, true);
		final byte[] one = BinaryUtils.encodeInt(1);
		final byte[] twoL = BinaryUtils.encodeLong(2L);
		final byte[] three = BinaryUtils.encodeInt(3);
		assertEquals(0, data.add(one, twoL));
		assertArrayEquals(twoL, data.get(0).value());
		final long index = data.add(twoL, three);
		assertArrayEquals(twoL, data.get(index).key());
		assertArrayEquals(three, data.get(index).value());
	}
}