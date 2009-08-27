package com.todesking.hongodbm;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.todesking.hongodbm.base.Buckets;
import com.todesking.hongodbm.base.Data;
import com.todesking.hongodbm.base.Entry;
import com.todesking.hongodbm.base.Header;
import com.todesking.hongodbm.base.Storage;

import junit.framework.TestCase;

import static com.todesking.hongodbm.TestHelper.*;

public class HongoDBMTest extends TestCase {
	public void testOpen() {
		final Storage storage = new ArrayStorage(0);
		try {
			HongoDBM.open(storage);
			fail("uninitialized storage");
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0, storage.size());

		final HongoDBM db =
			HongoDBM.create(storage, new HongoDBM.Params(1, 100));

		assertTrue(storage.size() > 0);

		for (int i = 0; i < 1000; i++)
			db.put(bin(i), new Integer(i).hashCode(), bin(i * 10));

		for (int i = 0; i < 1000; i++)
			assertArrayEquals(bin(i * 10), db.get(bin(i), new Integer(i).hashCode()));

		final HongoDBM openedDb = HongoDBM.open(storage);
		for (int i = 0; i < 1000; i++)
			assertArrayEquals(bin(i * 10), openedDb.get(bin(i), new Integer(i).hashCode()));
	}
	
	public void testOpenCorruptedBuckets() {
		final Storage storage=new ArrayStorage(0);
		new Header(storage,0L).initialize(100, 100);
		try {
			HongoDBM.open(storage);
			fail("invalid buckets");
		} catch(IllegalArgumentException e) {
		}
	}

	public void testPutSimple() throws Exception {
		final HongoDBM db = createDBM();

		final byte[] hoge = BinaryUtils.encodeString("hoge");
		final byte[] one = BinaryUtils.encodeInt(1);
		assertNull(db.get(hoge, Arrays.hashCode(hoge)));
		db.put(hoge, Arrays.hashCode(hoge), one);
		assertArrayEquals(one, db.get(hoge, Arrays.hashCode(hoge)));

	}

	public void testPutCollision() throws Exception {
		final int N = 1000;
		final int BUCKETS = 10;
		final HongoDBM db = createDBM(BUCKETS);
		final byte[] hoge = BinaryUtils.encodeString("hoge");
		final byte[] one = BinaryUtils.encodeInt(1);

		db.put(hoge, 0, one);
		db.put(one, 0, hoge);

		new AssertThrows(IllegalArgumentException.class) {
			@Override
			protected void proc() throws Exception {
				db.put(hoge, 0, one);
			}
		};

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

	public void testPutStoress() throws Exception {
		final int BUCKETS = 100;
		final HongoDBM db = createDBM(BUCKETS);
		final int N = 1000;

		final long startTime = System.currentTimeMillis();

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

		for (int i = 0; i < N * 10; i++) {
			final int x = (int) Math.random() * N;
			final String key = "key-" + x;
			assertArrayEquals(BinaryUtils.encodeInt(x), db.get(BinaryUtils
				.encodeString(key), key.hashCode()));
		}

		final int gets = N + N * 10;
		final int puts = N;
		final double sec = (System.currentTimeMillis() - startTime) / 1000.0;
		final double qps = (gets + puts) / sec;
		System.out.println("testComposite time: "
			+ sec
			+ "[sec] ("
			+ qps
			+ " [qps])");
		System.out.println("  items: " + N + " buckets:" + BUCKETS);
	}

	public void testDelete() throws Exception {
		final HongoDBM db = createDBM();

		new AssertThrows(IllegalArgumentException.class) {
			@Override
			protected void proc() throws Exception {
				db.delete(bin("key"), 0);
			}
		};

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

	public void testDeleteStress() throws Exception {
		final int BUCKETS = 100;
		final HongoDBM db = createDBM(BUCKETS);
		final int N = 10000;

		for (int i = 0; i < N; i++) {
			final String key = "key-" + i;
			db.put(BinaryUtils.encodeString(key), key.hashCode(), BinaryUtils
				.encodeInt(i));
			assertArrayEquals(bin(i), db.get(bin(key), key.hashCode()));
		}

		final Set<String> deleted = new HashSet<String>();
		for (int i = 0; i < N / 10; i++) {
			String key = "key-" + (int) (Math.random() * N);
			while (deleted.contains(key)) {
				key = "key-" + (int) (Math.random() * N);
			}
			db.delete(bin(key), key.hashCode());
			deleted.add(key);
		}

		for (int i = 0; i < N; i++) {
			final String key = "key-" + i;
			if (deleted.contains(key)) {
				assertNull(db.get(bin(key), key.hashCode()));
			} else {
				assertArrayEquals(bin(i), db.get(bin(key), key.hashCode()));
			}
		}
	}

	private static HongoDBM createDBM() throws IOException {
		return createDBM(10);
	}

	private static HongoDBM createDBM(int buckets) throws IOException {
		final Storage storage = new ArrayStorage(0);
		return HongoDBM.create(storage, new HongoDBM.Params(16, buckets));
	}

	public static class ArrayStorageTest extends TestCase {
		public void test() {
			final ArrayStorage storage = new ArrayStorage(1);
			new AssertThrows(IndexOutOfBoundsException.class) {
				@Override
				protected void proc() throws Exception {
					storage.read(-1, 1);
				}
			};
			try {
				storage.read(0L, 4);
				fail();
			} catch (IndexOutOfBoundsException e) {
			}
			
			new AssertThrows(IndexOutOfBoundsException.class) {
				@Override
				protected void proc() throws Exception {
					storage.write(0x7FFFFFFFFL, ba(10));
				}
			};
			storage.write(0, BinaryUtils.encodeInt(10));
			assertArrayEquals(BinaryUtils.encodeInt(10), storage.read(0L, 4));

			storage.fill(0L, 8, (byte) 0);
			assertArrayEquals(ba(0, 0, 0, 0, 0, 0, 0, 0), storage.read(0L, 8));
		}
		public void testClose() throws Exception {
			final ArrayStorage storage = new ArrayStorage(1);
			storage.close();
			assertThrows(NullPointerException.class,storage,"close");
		}
		
		public void testFlush() {
			final ArrayStorage storage = new ArrayStorage(1);
			storage.flush();
		}

		public void testFillShouldExtendStorage() {
			final ArrayStorage storage = new ArrayStorage(0);
			storage.fill(0L, 10, (byte) 0xFF);
			assertArrayEquals(BinaryUtils.encodeInt(0xFFFFFFFF), storage.read(
				0L,
				4));
		}
	}

	public static class HeaderTest extends TestCase {
		public void testHeader() {
			final ArrayStorage storage = new ArrayStorage(0);
			final Header header = new Header(storage, 0);
			header.initialize(16, 1000);
			assertArrayEquals(BinaryUtils.encodeInt(0xDEADBEAF), storage.read(
				0L,
				4));
			assertEquals(16, header.blockSize());
			assertEquals(1000, header.bucketsSize());
		}
	}

	public static class BucketsTest extends TestCase {
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

	public static class DataTest extends TestCase {
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

	public static class EntryTest extends TestCase {
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
			assertEquals(entry.size(),entry.blocks());
		}
	}
}