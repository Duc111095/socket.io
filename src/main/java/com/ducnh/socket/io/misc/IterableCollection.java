package com.ducnh.socket.io.misc;

import java.util.AbstractCollection;
import java.util.Iterator;


public class IterableCollection<T> extends AbstractCollection<T> {

	private final CompositeIterable<T> iterable;
	
	public IterableCollection(Iterable<T> iterable) {
		this(new CompositeIterable(iterable));
	}
	
	public IterableCollection(CompositeIterable<T> iterable) {
		this.iterable = iterable;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new CompositeIterable<T>(iterable).iterator();
	}
	
	@Override
	public int size() {
		Iterator<T> iterator = new CompositeIterable<T>(iterable).iterator();
		int count = 0;
		while (iterator.hasNext()) {
			iterator.next();
			count++;
		}
		return count;
	}
}
