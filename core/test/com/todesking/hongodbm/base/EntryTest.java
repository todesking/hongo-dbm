/**
 * 
 */
package com.todesking.hongodbm.base;

import static com.todesking.hongodbm.TestHelper.assertArrayEquals;
import junit.framework.TestCase;

import com.todesking.hongodbm.ArrayStorage;
import com.todesking.hongodbm.BinaryUtils;
import com.todesking.hongodbm.base.Entry;
import com.todesking.hongodbm.base.Storage;

public class EntryTest extends TestCase {
	public void test() {
		final Storage storage = new ArrayStorage(0);
		final Entry entry = new Entry(storage, 0L);
		final byte[] key = BinaryUtils.encodeInt(1);
		final byte[] value = BinaryUtils.encodeLong(2L);
		entry.write(1, key, value);
		assertArrayEquals(key, entry.key());
		assertArrayEquals(value, entry.value());
		assertEquals(key.length, entry.keySize());
		assertEquals(value.length, entry.valueSize());
		assertEquals(-1, entry.left());
		assertEquals(-1, entry.right());
		entry.setLeft(10);
		entry.setRight(1000);
		assertEquals(10, entry.left());
		assertEquals(1000, entry.right());
		assertEquals(Entry.HEADER_SIZE + key.length + value.length, entry
			.size());
		assertEquals(entry.size(), entry.blocks());
	}
}