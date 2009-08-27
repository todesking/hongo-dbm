/**
 * 
 */
package com.todesking.hongodbm;

import java.util.Arrays;

import com.todesking.hongodbm.base.Storage;

public class ArrayStorage extends Storage {
	private byte[] data;

	public ArrayStorage(int size) {
		data = new byte[size];
	}

	@Override
	public void close() {
		data = null;
	}

	private void extend(long size) {
		if (size != ((int) size))
			throw new IndexOutOfBoundsException("size too large");
		extend((int) size);
	}

	private void extend(int size) {
		if (data.length < size) {
			final byte[] newData = new byte[Math.max(data.length * 2, size)];
			assert data.length < newData.length;
			for (int i = 0; i < data.length; i++)
				newData[i] = data[i];
			data = newData;
		}
	}

	@Override
	public void fill(long offset, long length, byte value) {
		extend(offset + length);
		Arrays.fill(data, (int) offset, (int) (offset + length), value);
	}

	@Override
	public byte[] read(long offset, int length) {
		if (offset < 0)
			throw new IndexOutOfBoundsException("offset=" + offset);
		if (length < 0 || data.length < offset + length)
			throw new IndexOutOfBoundsException("size="
				+ data.length
				+ " but length="
				+ length);
		final byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = data[(int) offset + i];
		return result;
	}

	@Override
	public void write(long offset, byte[] value) {
		extend(offset + value.length);
		for (int i = 0; i < value.length; i++)
			data[(int) offset + i] = value[i];
	}

	@Override
	public void flush() {
	}

	@Override
	public long size() {
		return data.length;
	}
}