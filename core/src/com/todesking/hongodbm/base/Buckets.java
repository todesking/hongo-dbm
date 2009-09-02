/**
 * 
 */
package com.todesking.hongodbm.base;

public class Buckets extends Section {
	private static final int BUCKET_SIZE = 8;
	private static final int HEADER_SIZE = 0;

	private final int buckets;

	public Buckets(Storage storage, long offset, int bucketSize) {
		super(storage, offset);
		this.buckets = bucketSize;
	}

	public void initialize() {
		storage().fill(
			offset() + HEADER_SIZE,
			bucketsSize() * BUCKET_SIZE,
			(byte) 0xFF);
	}

	public long get(int index) {
		if (index < 0 || bucketsSize() <= index)
			throw new IndexOutOfBoundsException("bucketSize="
				+ bucketsSize()
				+ " but index="
				+ index);
		return storage().readLong(offset() + HEADER_SIZE + index * BUCKET_SIZE);
	}

	public void set(int index, long value) {
		if (index < 0 || bucketsSize() <= index)
			throw new IndexOutOfBoundsException();
		storage()
			.writeLong(offset() + HEADER_SIZE + index * BUCKET_SIZE, value);
	}

	public int bucketsSize() {
		return buckets;
	}

	@Override
	public long size() {
		return HEADER_SIZE + bucketsSize() * BUCKET_SIZE;
	}

	public boolean isValid() {
		return size() <= storage().size();
	}
}