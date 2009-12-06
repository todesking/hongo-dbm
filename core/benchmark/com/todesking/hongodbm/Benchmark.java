package com.todesking.hongodbm;

import static com.todesking.hongodbm.TestHelper.assertArrayEquals;
import static com.todesking.hongodbm.TestHelper.bin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.todesking.hongodbm.base.Storage;

import junit.framework.TestCase;

public class Benchmark extends TestCase {
	private static HongoDBM createDBM(int buckets) throws IOException {
		final Storage storage = new ArrayStorage(0);
		return HongoDBM.create(storage, new HongoDBM.Params(16, buckets));
	}

	public void testPutStress() throws Exception {
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
		System.out.println("testComposite time: " + sec + "[sec] (" + qps
				+ " [qps])");
		System.out.println("  items: " + N + " buckets:" + BUCKETS);
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
}
