/**
 * 
 */
package com.todesking.hongodbm.base;

public class Entry extends Section {
	public Entry(Storage storage, long offset) {
		super(storage, offset);
	}

	public byte[] key() {
		return storage().read(offset() + I_BODY, keySize());
	}

	public byte[] value() {
		return storage().read(offset() + I_BODY + keySize(), valueSize());
	}

	public int keySize() {
		return storage().readInt(offset() + I_KEYSIZE);
	}

	public int valueSize() {
		return storage().readInt(offset() + I_VALUESIZE);
	}

	public int blocks() {
		return storage().readInt(offset() + I_BLOCKS);
	}

	public long left() {
		return storage().readLong(offset() + I_LEFT);
	}

	public void setLeft(long value) {
		storage().writeLong(offset() + I_LEFT, value);
	}

	public long right() {
		return storage().readLong(offset() + I_RIGHT);
	}

	public void setRight(long value) {
		storage().writeLong(offset() + I_RIGHT, value);
	}

	public long getBrahch(boolean left) {
		if (left)
			return left();
		else
			return right();
	}

	public void setBrahch(boolean left, long value) {
		if (left)
			setLeft(value);
		else
			setRight(value);
	}

	public void write(int blockSize, byte[] key, byte[] value) {
		final int blocks =
			(int) Math.ceil((HEADER_SIZE + key.length + value.length)
				/ (double) blockSize);
		storage().writeInt(offset() + I_KEYSIZE, key.length);
		storage().writeInt(offset() + I_VALUESIZE, value.length);
		storage().writeInt(offset() + I_BLOCKS, blocks);
		storage().writeLong(offset() + I_LEFT, -1);
		storage().writeLong(offset() + I_RIGHT, -1);
		storage().write(offset() + I_BODY, key);
		storage().write(offset() + I_BODY + key.length, value);
		final int fillerSize =
			blockSize * blocks - (I_BODY + key.length + value.length);
		storage().fill(
			offset() + I_BODY + key.length + value.length,
			fillerSize,
			(byte) 0);
	}

	public long size() {
		return HEADER_SIZE + keySize() + valueSize();
	}

	private static int I_KEYSIZE = 0;
	private static int I_VALUESIZE = I_KEYSIZE + 4;
	private static int I_BLOCKS = I_VALUESIZE + 4;
	private static int I_LEFT = I_BLOCKS + 4;
	private static int I_RIGHT = I_LEFT + 8;
	private static int I_BODY = I_RIGHT + 8;
	public static int HEADER_SIZE = I_BODY;
}