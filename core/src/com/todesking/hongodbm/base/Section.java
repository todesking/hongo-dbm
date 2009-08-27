/**
 * 
 */
package com.todesking.hongodbm.base;

public abstract class Section {
	private final long offset;
	private final Storage storage;

	public Section(Storage storage, long offset) {
		this.storage = storage;
		this.offset = offset;
	}

	public long offset() {
		return offset;
	}

	public Storage storage() {
		return storage;
	}

	public abstract long size();

	public long tail() {
		return offset() + size();
	}
}