package com.todesking.hongodbm;

import org.apache.commons.lang.ArrayUtils;

import com.todesking.hongodbm.base.Buckets;
import com.todesking.hongodbm.base.Data;
import com.todesking.hongodbm.base.Entry;
import com.todesking.hongodbm.base.Header;
import com.todesking.hongodbm.base.Storage;

public class HongoDBM {
	private final Header header;
	private final Buckets buckets;
	private final Data data;

	private HongoDBM(Storage storage) {
		header = new Header(storage, 0L);
		if (!header.isValid())
			throw new IllegalArgumentException("header is invalid");
		buckets = new Buckets(storage, header.tail(), header.bucketsSize());
		if (!buckets.isValid())
			throw new IllegalArgumentException("buckets is invalid");
		data = new Data(storage, buckets.tail(), header.blockSize(), false);
	}

	private HongoDBM(Storage storage, Params params) {
		header = new Header(storage, 0L);
		header.initialize(params.blockSize, params.bucketsSize);
		buckets = new Buckets(storage, header.tail(), params.bucketsSize);
		buckets.initialize();
		data = new Data(storage, buckets.tail(), params.blockSize, true);
	}

	public static HongoDBM open(Storage storage) {
		return new HongoDBM(storage);
	}

	public static HongoDBM create(Storage storage, Params params) {
		return new HongoDBM(storage, params);
	}

	public static class Params {
		public Params(int blociSize, int bucketsSize) {
			this.bucketsSize = bucketsSize;
			this.blockSize = blociSize;
		}

		public final int bucketsSize;
		public final int blockSize;
	}

	public byte[] get(byte[] key, int hash) {
		final EntryChain entry = lookupEntry(key, hash);
		if (entry.current == null)
			return null;
		else
			return entry.current.value();
	}

	/**
	 * put key-value pair. error if key exists.
	 * 
	 * @param key
	 * @param hash
	 * @param value
	 * @throws IllegalArgumentException
	 *             if key conflicts
	 */
	public void put(byte[] key, int hash, byte[] value) {
		final EntryChain entry = lookupEntry(key, hash);
		assert entry != null;
		if (entry.current != null)
			throw new IllegalArgumentException("key conflicted");
		putImpl(key, hash, value, entry);
	}

	/**
	 * put key-value pair. do nothing if key exists. put value
	 * 
	 * @param key
	 * @param hash
	 * @param value
	 */
	public void putIgnore(byte[] key, int hash, byte[] value) {
		final EntryChain entry = lookupEntry(key, hash);
		assert entry != null;
		if (entry.current != null)
			return;
		putImpl(key, hash, value, entry);
	}

	/**
	 * put key-value pair. append value if key exists. put value
	 * 
	 * @param key
	 * @param hash
	 * @param value
	 */
	public void putConcat(byte[] key, int hash, byte[] value) {
		EntryChain entry = lookupEntry(key, hash);
		assert entry != null;
		if (entry.current != null) {
			final byte[] prevValue = entry.current.value();
			delete(key, hash);
			entry = lookupEntry(key, hash);
			value = ArrayUtils.addAll(prevValue, value);
			assert entry.current == null;
		}
		putImpl(key, hash, value, entry);
	}

	/**
	 * put key-value pair. replace value if key exists. put value
	 * 
	 * @param key
	 * @param hash
	 * @param value
	 */
	public void putReplace(byte[] key, int hash, byte[] value) {
		EntryChain entry = lookupEntry(key, hash);
		assert entry != null;
		if (entry.current != null) {
			delete(key, hash);
			entry = lookupEntry(key, hash);
			assert entry.current == null;
		}
		putImpl(key, hash, value, entry);
	}

	private void putImpl(byte[] key, int hash, byte[] value, EntryChain entry) {
		updatePointer(entry, hash, data.add(key, value));
	}

	private void updatePointer(EntryChain entry, int hash, long index) {
		if (entry.prev == null) {
			buckets.set(bucketIndex(hash), index);
		} else {
			entry.prev.setBrahch(entry.branchLeft, index);
		}
	}

	/**
	 * delete if exists
	 * 
	 * @param key
	 * @param hash
	 */
	public void deleteIgnore(byte[] key, int hash) {
		final EntryChain entry = lookupEntry(key, hash);
		if (entry.current == null)
			return;

		deleteImpl(entry, hash);
	}

	/**
	 * delete(error if key is not exists)
	 * 
	 * @param key
	 * @param hash
	 */
	public void delete(byte[] key, int hash) {
		final EntryChain entry = lookupEntry(key, hash);
		if (entry.current == null)
			throw new IllegalArgumentException("key not found");

		deleteImpl(entry, hash);
	}

	private void deleteImpl(final EntryChain entry, int hash) {
		final long insertTarget;
		if (entry.current.left() == -1) {
			insertTarget = entry.current.right();
		} else if (entry.current.right() == -1) {
			insertTarget = entry.current.left();
		} else { // both
			// 右の木を左の木の子にする
			final Entry left = data.get(entry.current.left());
			final Entry right = data.get(entry.current.right());
			final EntryChain insertTo = lookupEntry(left, right.key());
			assert insertTo.current == null && insertTo.prev != null;
			insertTo.prev.setBrahch(insertTo.branchLeft, entry.current.right());
			insertTarget = entry.current.left();
		}

		updatePointer(entry, hash, insertTarget);
	}

	private static class EntryChain {
		public final Entry prev;
		public final Entry current;
		public final boolean branchLeft;

		public EntryChain(Entry prev, Entry current, boolean branchLeft) {
			this.prev = prev;
			this.current = current;
			this.branchLeft = branchLeft;
		}

		public static EntryChain Null = new EntryChain(null, null, false);
	}

	private EntryChain lookupEntry(byte[] key, int hash) {
		final int bucketIndex = bucketIndex(hash);
		final long chainStart = buckets.get(bucketIndex);
		if (chainStart == -1) {
			return EntryChain.Null;
		} else {
			return lookupEntry(data.get(chainStart), key);
		}
	}

	private EntryChain lookupEntry(Entry start, byte[] key) {
		Entry prev = null;
		int prevCompare = 1;
		Entry entry = start;
		while (true) {
			final int compare = BinaryUtils.compare(key, entry.key());
			if (compare == 0)
				return new EntryChain(prev, entry, prevCompare == -1);
			final boolean isLeft = compare == -1;

			final long next = entry.getBrahch(isLeft);
			if (next == -1)
				return new EntryChain(entry, null, isLeft);

			prev = entry;
			entry = data.get(next);
			prevCompare = compare;
		}
	}

	private int bucketIndex(int hash) {
		return (0 < hash ? hash : -hash) % buckets.bucketsSize();
	}
}
