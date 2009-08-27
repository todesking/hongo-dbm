/**
 * 
 */
package com.todesking.hongodbm.base;

public class Data extends Section {
	private static final int HEADER_SIZE = 8;

	private final int blockSize;

	public Data(Storage storage, long offset, int blockSize, boolean init) {
		super(storage, offset);
		this.blockSize = blockSize;
		if (init)
			storage.writeLong(offset(), 0L); // size
	}

	@Override
	public long size() {
		return storage().readLong(offset());
	}

	public long add(byte[] key, byte[] value) {
		final long index = size();
		final Entry entry =
			new Entry(storage(), offset() + HEADER_SIZE + index);
		entry.write(blockSize, key, value);
		storage().writeLong(offset(), size() + entry.size());
		return index;
	}

	public Entry get(long index) {
		return new Entry(storage(), offset() + HEADER_SIZE + index);
	}
}