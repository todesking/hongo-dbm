package com.todesking.hongodbm;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import com.todesking.hongodbm.base.Header;
import com.todesking.hongodbm.base.Storage;

import junit.framework.TestCase;

import static com.todesking.hongodbm.TestHelper.*;

public class HongoDBMTest extends TestCase {
	public void testOpen() throws Exception {
		final Storage storage = new ArrayStorage(0);
		assertThrows(
			IllegalArgumentException.class,
			HongoDBM.class,
			"open",
			storage);
		assertEquals(0, storage.size());

		final HongoDBM db =
			HongoDBM.create(storage, new HongoDBM.Params(1, 100));

		assertTrue(storage.size() > 0);

		for (int i = 0; i < 1000; i++)
			db.put(bin(i), new Integer(i).hashCode(), bin(i * 10));

		for (int i = 0; i < 1000; i++)
			assertArrayEquals(bin(i * 10), db.get(bin(i), new Integer(i)
				.hashCode()));

		final HongoDBM openedDb = HongoDBM.open(storage);
		for (int i = 0; i < 1000; i++)
			assertArrayEquals(bin(i * 10), openedDb.get(bin(i), new Integer(i)
				.hashCode()));
	}

	public void testOpenCorruptedBuckets() throws Exception {
		final Storage storage = new ArrayStorage(0);
		new Header(storage, 0L).initialize(100, 100);
		assertThrows(
			IllegalArgumentException.class,
			HongoDBM.class,
			"open",
			storage);
	}

	public void testPutSimple() throws Exception {
		final HongoDBM db = createDBM();

		final byte[] hoge = BinaryUtils.encodeString("hoge");
		final byte[] one = BinaryUtils.encodeInt(1);
		assertNull(db.get(hoge, Arrays.hashCode(hoge)));
		db.put(hoge, Arrays.hashCode(hoge), one);
		assertArrayEquals(one, db.get(hoge, Arrays.hashCode(hoge)));

	}

	public void testPutIgnore() throws Exception {
		final HongoDBM db = createDBM();
		db.put(bin("hoge"), 0, bin(10));
		assertArrayEquals(bin(10), db.get(bin("hoge"), 0));
		db.putIgnore(bin("hoge"), 0, bin(100));
		assertArrayEquals(bin(10), db.get(bin("hoge"), 0));
	}

	public void testPutConcat() throws Exception {
		final HongoDBM db = createDBM();
		db.put(bin("hoge"), 0, bin(10));
		assertArrayEquals(bin(10), db.get(bin("hoge"), 0));
		db.putConcat(bin("hoge"), 0, bin(100));
		assertArrayEquals(ArrayUtils.addAll(bin(10), bin(100)), db.get(
			bin("hoge"),
			0));
	}

	public void testPutReplace() throws Exception {
		final HongoDBM db = createDBM();
		db.put(bin("hoge"), 0, bin(10));
		assertArrayEquals(bin(10), db.get(bin("hoge"), 0));
		db.putReplace(bin("hoge"), 0, bin(100));
		assertArrayEquals(bin(100), db.get(bin("hoge"), 0));
	}

	public void testDeleteIgnore() throws Exception {
		final HongoDBM db = createDBM();
		db.put(bin("hoge"), 0, bin(10));
		db.deleteIgnore(bin("fuga"), 0);
		assertArrayEquals(bin(10), db.get(bin("hoge"), 0));
		db.deleteIgnore(bin("hoge"), 0);
		assertNull(db.get(bin("hoge"), 0));
	}

	public void testPutCollision() throws Exception {
		final int N = 1000;
		final int BUCKETS = 10;
		final HongoDBM db = createDBM(BUCKETS);
		final byte[] hoge = BinaryUtils.encodeString("hoge");
		final byte[] one = BinaryUtils.encodeInt(1);

		db.put(hoge, 0, one);
		db.put(one, 0, hoge);

		assertThrows(IllegalArgumentException.class, db, "put", hoge, 0, one);

		assertArrayEquals(hoge, db.get(one, 0));
		assertNull(db.get(one, 1));
		assertArrayEquals(one, db.get(hoge, 0));
		assertNull(db.get(hoge, 1));

		for (int i = 0; i < N; i++) {
			final String key = "key-" + i;
			db.put(BinaryUtils.encodeString(key), key.hashCode(), BinaryUtils
				.encodeInt(i));
		}

		for (int i = 0; i < N; i++) {
			final String key = "key-" + i;
			assertArrayEquals(BinaryUtils.encodeInt(i), db.get(BinaryUtils
				.encodeString(key), key.hashCode()));
		}
	}

	public void testDelete() throws Exception {
		{
			final HongoDBM db = createDBM();

			assertThrows(
				IllegalArgumentException.class,
				db,
				"delete",
				bin("key"),
				0);

			db.put(bin("key"), 0, bin(100));
			db.put(bin("key2"), 0, bin(200));
			assertArrayEquals(bin(100), db.get(bin("key"), 0));
			assertArrayEquals(bin(200), db.get(bin("key2"), 0));

			// 他のエントリからつながってる要素、子なし
			db.delete(bin("key2"), 0);
			assertArrayEquals(bin(100), db.get(bin("key"), 0));
			assertNull(db.get(bin("key2"), 0));

			// バケットから直接つながってる要素、子なし
			db.delete(bin("key"), 0);
			assertNull(db.get(bin("key"), 0));

			// aaa => key1 => {key0,key2}
			db.put(bin("aaa"), 0, bin(-1));
			db.put(bin("aa"), 0, bin(-10));
			db.put(bin("key1"), 0, bin(100));
			db.put(bin("key0"), 0, bin(0));
			db.put(bin("key2"), 0, bin(200));

			db.delete(bin("key0"), 0);
			assertNull(db.get(bin("key0"), 0));
			assertArrayEquals(bin(100), db.get(bin("key1"), 0));
			assertArrayEquals(bin(200), db.get(bin("key2"), 0));
			assertArrayEquals(bin(-1), db.get(bin("aaa"), 0));
			assertArrayEquals(bin(-10), db.get(bin("aa"), 0));
		}
		{
			final HongoDBM db = createDBM();
			// 0 => {,1}
			db.put(ba(0), 0, bin(0));
			db.put(ba(1), 0, bin(1));
			assertArrayEquals(bin(0), db.get(ba(0), 0));
			assertArrayEquals(bin(1), db.get(ba(1), 0));
			db.delete(ba(0), 0);
			assertNull(db.get(ba(0), 0));
			assertArrayEquals(bin(1), db.get(ba(1), 0));
		}
		{
			final HongoDBM db = createDBM();
			// 1 => {0,}
			db.put(ba(1), 0, bin(1));
			db.put(ba(0), 0, bin(0));
			assertArrayEquals(bin(0), db.get(ba(0), 0));
			assertArrayEquals(bin(1), db.get(ba(1), 0));
			db.delete(ba(1), 0);
			assertArrayEquals(bin(0), db.get(ba(0), 0));
			assertNull(db.get(ba(1), 1));
		}

	}

	private static HongoDBM createDBM() throws IOException {
		return createDBM(10);
	}

	private static HongoDBM createDBM(int buckets) throws IOException {
		final Storage storage = new ArrayStorage(0);
		return HongoDBM.create(storage, new HongoDBM.Params(16, buckets));
	}
}
