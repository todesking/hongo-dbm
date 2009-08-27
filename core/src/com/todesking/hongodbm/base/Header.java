/**
 * 
 */
package com.todesking.hongodbm.base;

public class Header extends Section {
	public Header(Storage storage, long offset) {
		super(storage, offset);
	}

	private final int I_SIGNATURE = 0;
	private final int I_BUCKETS_SIZE = I_SIGNATURE + 4;
	private final int I_BLOCK_SIZE = I_BUCKETS_SIZE + 4;
	private final int I_TAIL = I_BLOCK_SIZE + 4;

	@Override
	public long size() {
		return I_TAIL;
	}

	public void initialize(int blockSize, int bucketSize) {
		storage().write(
			offset() + I_SIGNATURE,
			new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xAF });
		storage().writeInt(offset() + I_BLOCK_SIZE, blockSize);
		storage().writeInt(offset() + I_BUCKETS_SIZE, bucketSize);
	}

	public int bucketsSize() {
		return storage().readInt(offset() + I_BUCKETS_SIZE);
	}

	public int blockSize() {
		return storage().readInt(offset() + I_BLOCK_SIZE);
	}

	public boolean isValid() {
		return offset() + size() <= storage().size()
			&& 0 < bucketsSize()
			&& 0 < blockSize();
	}
}