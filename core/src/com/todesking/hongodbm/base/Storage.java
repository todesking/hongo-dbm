/**
 * 
 */
package com.todesking.hongodbm.base;

import com.todesking.hongodbm.BinaryUtils;

public abstract class Storage {
	public abstract void flush();

	public abstract void write(long offset, byte[] data);

	public abstract byte[] read(long offset, int length);

	public abstract void fill(long offset, long length, byte value);

	public abstract long size();

	public abstract void close();

	public void writeInt(long offset, int value) {
		write(offset, BinaryUtils.encodeInt(value));
	}

	public void writeLong(long offset, long value) {
		write(offset, BinaryUtils.encodeLong(value));
	}

	public int readInt(long offset) {
		return BinaryUtils.decodeInt(read(offset, 4));
	}

	public long readLong(long offset) {
		return BinaryUtils.decodeLong(read(offset, 8));
	}
}